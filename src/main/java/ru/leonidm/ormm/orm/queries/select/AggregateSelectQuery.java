package ru.leonidm.ormm.orm.queries.select;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.utils.QueryUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

public final class AggregateSelectQuery<T, R> extends AbstractSelectQuery<AggregateSelectQuery<T, R>, T, R, R> {

    private final String function;
    private final Class<R> rClass;

    public AggregateSelectQuery(@NotNull ORMTable<T> table, @NotNull String function, @NotNull Class<R> rClass) {
        super(table);

        this.function = function;
        this.rClass = rClass;
    }

    @Override
    @NotNull
    protected StringBuilder writeColumn(@NotNull StringBuilder queryBuilder, @NotNull String column) {
        if (column.equals("*")) {
            return queryBuilder.append(function).append("(*)");
        } else {
            return queryBuilder.append(function).append('(').append(QueryUtils.getTableName(table)).append('.').append(column).append(')');
        }
    }

    @Override
    @NotNull
    protected Supplier<R> prepareSupplier() {
        if (joins.stream().anyMatch(join -> !join.getColumns().values().isEmpty())) {
            throw new IllegalStateException("Consumers in joins are not supported in aggregate queries");
        }

        return () -> {
            try (Statement statement = table.getDatabase().getConnection().createStatement();
                 ResultSet resultSet = statement.executeQuery(getSQLQuery())) {

                if (resultSet.next()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    writeColumn(stringBuilder, columns[0]);
                    String column = stringBuilder.toString();

                    Object object = resultSet.getObject(column);
                    if (rClass == Long.class && object.getClass() == Integer.class) {
                        object = (long) (int) object;
                    }

                    return (R) object;
                }

                return null;
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        };
    }
}
