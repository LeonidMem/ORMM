package ru.leonidm.ormm.orm.queries.select;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Supplier;

public final class SelectQuery<T> extends AbstractSelectQuery<SelectQuery<T>, T, List<T>, T> {

    public SelectQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @Override
    @NotNull
    protected Supplier<List<T>> prepareSupplier() {
        return () -> {
            try (Statement statement = table.getDatabase().getConnection().createStatement();
                 ResultSet resultSet = statement.executeQuery(getSQLQuery())) {

                JoinsHandler<T, T> joinsHandler = new JoinsHandler<>(table, joins);

                while (resultSet.next()) {
                    T t = table.objectFrom(resultSet);

                    if (limit > 0 && !joinsHandler.contains(resultSet) && joinsHandler.getObjects().size() >= limit) {
                        break;
                    }

                    joinsHandler.save(resultSet, t);
                }

                joinsHandler.apply();
                return List.copyOf(joinsHandler.getObjects());
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    @NotNull
    public SingleSelectQuery<T> single() {
        SingleSelectQuery<T> singleSelectQuery = new SingleSelectQuery<>(table);

        copyTo(singleSelectQuery);
        singleSelectQuery.limit = 1;

        return singleSelectQuery;
    }

    @NotNull
    public RawSelectQuery<T> columns(@NotNull String @NotNull ... columns) {
        checkIfColumnsExist(columns);

        RawSelectQuery<T> rawSelectQuery = new RawSelectQuery<>(table);

        copyTo(rawSelectQuery);

        return rawSelectQuery;
    }

    @NotNull
    public AggregateSelectQuery<T, ? extends Number> min(@NotNull String column) {
        return aggregateSelectQuery(column, "MIN", true);
    }

    @NotNull
    public AggregateSelectQuery<T, ? extends Number> max(@NotNull String column) {
        return aggregateSelectQuery(column, "MAX", true);
    }

    @NotNull
    public AggregateSelectQuery<T, Long> count(@NotNull String column) {
        return aggregateSelectQuery(column, "COUNT", false);
    }

    @NotNull
    private <R extends Number> AggregateSelectQuery<T, R> aggregateSelectQuery(@NotNull String column, @NotNull String function,
                                                                               boolean dynamicResult) {
        if (columns.length != table.getColumnsNames().size()) {
            throw new IllegalStateException("Columns must not be changed in aggregate queries");
        }

        AggregateSelectQuery<T, R> aggregateSelectQuery;

        if (dynamicResult) {
            ORMColumn<T, ? extends Number> ormColumn = (ORMColumn<T, ? extends Number>) table.getColumn(column);
            if (ormColumn == null) {
                throw new IllegalArgumentException(table.getIdentifier() + " Cannot find column " + column);
            }

            aggregateSelectQuery = new AggregateSelectQuery<>(table, function, (Class<R>) ormColumn.getFieldClass());
        } else {
            aggregateSelectQuery = new AggregateSelectQuery<>(table, function, (Class<R>) Long.class);
        }

        copyTo(aggregateSelectQuery);
        aggregateSelectQuery.columns = new String[]{column};

        return aggregateSelectQuery;
    }
}
