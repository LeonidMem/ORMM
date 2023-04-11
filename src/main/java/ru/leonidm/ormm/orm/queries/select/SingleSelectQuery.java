package ru.leonidm.ormm.orm.queries.select;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

public final class SingleSelectQuery<T> extends AbstractSelectQuery<SingleSelectQuery<T>, T, T, T> {

    public SingleSelectQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @Override
    @NotNull
    public SingleSelectQuery<T> limit(int limit) {
        throw new IllegalStateException("Can't change limit of SingleSelectQuery");
    }

    @Override
    @NotNull
    protected Supplier<T> prepareSupplier() {
        return () -> {
            try (Statement statement = table.getDatabase().getConnection().createStatement();
                 ResultSet resultSet = statement.executeQuery(getSQLQuery())) {

                T t = null;
                JoinsHandler<T, T> joinsHandler = new JoinsHandler<>(table, joins);

                while (resultSet.next()) {
                    if (t == null) {
                        t = table.objectFrom(resultSet);
                    }

                    joinsHandler.save(resultSet, t);
                }

                joinsHandler.apply();
                return t;
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    @NotNull
    public RawSingleSelectQuery<T> columns(String @NotNull ... columns) {
        checkIfColumnsExist(columns);

        RawSingleSelectQuery<T> rawSelectQuery = new RawSingleSelectQuery<>(table);

        copy(rawSelectQuery);
        rawSelectQuery.columns = columns;

        return rawSelectQuery;
    }
}
