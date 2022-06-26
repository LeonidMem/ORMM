package ru.leonidm.ormm.orm.queries.indexes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.general.SQLType;
import ru.leonidm.ormm.orm.queries.AbstractQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Supplier;

public final class CreateIndexesQuery<T> extends AbstractQuery<T, Void> {

    private final List<ORMColumn<T, ?>> columns;

    // TODO: transform to CreateIndexesQuery
    public CreateIndexesQuery(@NotNull ORMTable<T> table, @NotNull List<ORMColumn<T, ?>> columns) {
        super(table);

        columns.forEach(column -> {
            SQLType sqlType = column.getSQLType();
            if(!sqlType.isIndexable(column.getTable().getDatabase().getDriver())) {
                throw new IllegalArgumentException(column.getIdentifier() +
                        " This type isn't indexable!");
            }
        });

        this.columns = columns;
    }

    // TODO: index TEXT and BLOB with prefix length

    @Nullable
    public String getSQLCheck() {
        return switch(this.table.getDatabase().getDriver()) {
            case MYSQL -> {
                StringBuilder queryBuilder = new StringBuilder();

                queryBuilder.append("SELECT COUNT(1) FROM information_schema.statistics " +
                        "WHERE table_schema = DATABASE() AND table_name = \"")
                        .append(this.table.getName()).append("\" ")
                        .append("AND index_name IN (");

                this.columns.forEach(column -> {
                    queryBuilder.append('"').append(column.getName()).append("_ormm_idx\", ");
                });

                yield queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length()).append(")").toString();
            }
            case SQLITE -> null;
        };
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("CREATE INDEX ");

        if(this.table.getDatabase().getDriver() == ORMDriver.SQLITE) {
            queryBuilder.append("IF NOT EXISTS ");
        }

        this.columns.forEach(column -> {
            queryBuilder.append(column.getName()).append("_ormm_idx, ");
        });

        queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length())
                .append(" ON ").append(this.table.getName()).append('(');

        this.columns.forEach(column -> {
            queryBuilder.append(column.getName()).append(", ");
        });

        return queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length()).append(')').toString();
    }

    @Override
    @NotNull
    protected Supplier<Void> prepareSupplier() {
        return () -> {
            try(Statement statement = this.table.getDatabase().getConnection().createStatement()) {

                String sqlCheck = getSQLCheck();

                if(sqlCheck != null) {
                    try(ResultSet resultSet = statement.executeQuery(sqlCheck)) {
                        resultSet.next();

                        int count = resultSet.getInt(1);
                        if(count != 0) return null;
                    }
                }

                statement.executeUpdate(getSQLQuery());
            } catch(SQLException e) {
                e.printStackTrace();
            }

            return null;
        };
    }

}
