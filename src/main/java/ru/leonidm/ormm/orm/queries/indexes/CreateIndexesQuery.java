package ru.leonidm.ormm.orm.queries.indexes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.commons.collections.Pair;
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

    private final List<Pair<List<ORMColumn<T, ?>>, Boolean>> columns;

    public CreateIndexesQuery(@NotNull ORMTable<T> table, @NotNull List<Pair<List<ORMColumn<T, ?>>, Boolean>> columns) {
        super(table);

        columns.forEach(pair -> {
            var list = pair.getLeft();
            if (list.isEmpty()) {
                throw new IllegalArgumentException(table.getIdentifier() + " Cannot create index with zero columns");
            }

            list.forEach(column -> {
                SQLType sqlType = column.getSQLType();
                if (!sqlType.isIndexable(column.getTable().getDatabase().getDriver())) {
                    throw new IllegalArgumentException(column.getIdentifier() +
                            " This type isn't indexable");
                }
            });
        });

        this.columns = columns;
    }

    @Nullable
    public String getSQLCheck(@NotNull List<ORMColumn<T, ?>> list) {
        return switch (table.getDatabase().getDriver()) {
            case MYSQL -> {
                StringBuilder queryBuilder = new StringBuilder();

                queryBuilder.append("SELECT COUNT(1) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = \"")
                        .append(QueryUtils.getTableName(table)).append("\" ").append("AND index_name = ").append('"');

                list.forEach(column -> {
                    queryBuilder.append(column.getName()).append('_');
                });

                queryBuilder.append("ormm_idx\"");

                yield queryBuilder.toString();
            }
            case SQLITE -> null;
        };
    }

    @NotNull
    public String getSQLQuery(@NotNull Pair<List<ORMColumn<T, ?>>, Boolean> pair) {
        var list = pair.getLeft();
        boolean unique = pair.getRight();

        return switch (table.getDatabase().getDriver()) {
            case MYSQL -> {
                StringBuilder queryBuilder = new StringBuilder();

                queryBuilder.append("CREATE ");
                if (unique) {
                    queryBuilder.append("UNIQUE ");
                }
                queryBuilder.append("INDEX ");

                list.forEach(column -> {
                    queryBuilder.append(column.getName()).append('_');
                });

                queryBuilder.append("ormm_idx ON ")
                        .append(QueryUtils.getTableName(table)).append('(');

                list.forEach(column -> {
                    queryBuilder.append(column.getName());

                    SQLType sqlType = column.getSQLType();
                    if (sqlType == SQLType.TEXT) {
                        queryBuilder.append("(256)");
                    } else if (sqlType.hasLength()) {
                        queryBuilder.append('(').append(QueryUtils.getLength(column)).append(')');
                    }

                    queryBuilder.append(", ");
                });

                queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length());

                yield queryBuilder.append(')').toString();
            }
            case SQLITE -> throw new IllegalStateException("Bad");
        };
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        switch (table.getDatabase().getDriver()) {
            case MYSQL -> {
                StringBuilder stringBuilder = new StringBuilder();
                columns.forEach(pair -> {
                    stringBuilder.append(getSQLQuery(pair)).append("; ");
                });
            }
            case SQLITE -> {
                columns.forEach(pair -> {
                    boolean unique = pair.getRight();

                    queryBuilder.append("CREATE ");
                    if (unique) {
                        queryBuilder.append("UNIQUE ");
                    }
                    queryBuilder.append("INDEX IF NOT EXISTS ");

                    var list = pair.getLeft();
                    list.forEach(column -> {
                        queryBuilder.append(column.getName()).append('_');
                    });

                    queryBuilder.append("ormm_idx ON ")
                            .append(QueryUtils.getTableName(table)).append('(');

                    list.forEach(column -> {
                        queryBuilder.append(column.getName()).append(", ");
                    });

                    queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length());

                    queryBuilder.append("); ");
                });
            }
        }

        return queryBuilder.toString();
    }

    @Override
    @NotNull
    protected Supplier<Void> prepareSupplier() {
        return () -> {
            try (Statement statement = table.getDatabase().getConnection().createStatement()) {

                switch (table.getDatabase().getDriver()) {
                    case MYSQL -> {
                        columns.forEach(pair -> {
                            try {
                                try (ResultSet resultSet = statement.executeQuery(getSQLCheck(pair.getLeft()))) {
                                    resultSet.next();

                                    int count = resultSet.getInt(1);
                                    if (count != 0) {
                                        return;
                                    }
                                }

                                statement.executeUpdate(getSQLQuery(pair));
                            } catch (SQLException e) {
                                throw new IllegalStateException(e);
                            }
                        });
                    }
                    case SQLITE -> {
                        statement.executeUpdate(getSQLQuery());
                    }
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }

            return null;
        };
    }

}
