package ru.leonidm.ormm.orm.queries.indexes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.general.SQLType;
import ru.leonidm.ormm.orm.queries.AbstractQuery;
import ru.leonidm.ormm.utils.QueryUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Supplier;

public final class CreateIndexesQuery<T> extends AbstractQuery<T, Void> {

    private final List<ORMColumn<T, ?>> columns;

    public CreateIndexesQuery(@NotNull ORMTable<T> table, @NotNull List<ORMColumn<T, ?>> columns) {
        super(table);

        columns.forEach(column -> {
            SQLType sqlType = column.getSQLType();
            if (!sqlType.isIndexable(column.getTable().getDatabase().getDriver())) {
                throw new IllegalArgumentException(column.getIdentifier() +
                        " This type isn't indexable");
            }
        });

        this.columns = columns;
    }

    @Nullable
    public String getSQLCheck(@NotNull ORMColumn<T, ?> column) {
        return switch (this.table.getDatabase().getDriver()) {
            case MYSQL -> "SELECT COUNT(1) FROM information_schema.statistics " +
                    "WHERE table_schema = DATABASE() AND table_name = \"" +
                    QueryUtils.getTableName(this.table) + "\" " +
                    "AND index_name = " +
                    '"' + column.getName() + "_ormm_idx\"";
            case SQLITE -> null;
        };
    }

    @NotNull
    public String getSQLQuery(@NotNull ORMColumn<T, ?> column) {
        return switch (this.table.getDatabase().getDriver()) {
            case MYSQL -> {
                StringBuilder queryBuilder = new StringBuilder();

                queryBuilder.append("CREATE INDEX ").append(column.getName()).append("_ormm_idx ON ")
                        .append(QueryUtils.getTableName(this.table)).append('(').append(column.getName());

                SQLType sqlType = column.getSQLType();
                if (sqlType == SQLType.TEXT) {
                    queryBuilder.append("(256)");
                } else if (sqlType.hasLength()) {
                    queryBuilder.append('(').append(QueryUtils.getLength(column)).append(')');
                }

                yield queryBuilder.append(')').toString();
            }
            case SQLITE -> throw new IllegalStateException("Bad");
        };
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        switch (this.table.getDatabase().getDriver()) {
            case MYSQL -> {
                this.columns.forEach(column -> {
                    queryBuilder.append("CREATE INDEX ").append(column.getName()).append("_ormm_idx ON ")
                            .append(QueryUtils.getTableName(this.table)).append('(').append(column.getName());

                    SQLType sqlType = column.getSQLType();
                    if (sqlType == SQLType.TEXT) {
                        queryBuilder.append("(256)");
                    } else if (sqlType.hasLength()) {
                        queryBuilder.append('(').append(QueryUtils.getLength(column)).append(')');
                    }

                    queryBuilder.append("); ");
                });
            }
            case SQLITE -> {
                this.columns.forEach(column -> {
                    queryBuilder.append("CREATE INDEX IF NOT EXISTS ").append(column.getName()).append("_ormm_idx ON ")
                            .append(QueryUtils.getTableName(this.table)).append('(').append(column.getName()).append(");");
                });
            }
        }

        return queryBuilder.toString();
    }

    @Override
    @NotNull
    protected Supplier<Void> prepareSupplier() {
        return () -> {
            try (Statement statement = this.table.getDatabase().getConnection().createStatement()) {

                switch (this.table.getDatabase().getDriver()) {
                    case MYSQL -> {
                        this.columns.forEach((column) -> {
                            try {
                                try (ResultSet resultSet = statement.executeQuery(this.getSQLCheck(column))) {
                                    resultSet.next();

                                    int count = resultSet.getInt(1);
                                    if (count != 0) {
                                        return;
                                    }
                                }

                                statement.executeUpdate(this.getSQLQuery(column));
                            } catch (SQLException e) {
                                throw new IllegalStateException(e);
                            }
                        });
                    }
                    case SQLITE -> {
                        statement.executeUpdate(this.getSQLQuery());
                    }
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }

            return null;
        };
    }

}
