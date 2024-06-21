package ru.leonidm.ormm.orm.queries.select;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.connection.OrmConnection;

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
            try (OrmConnection connection = table.getDatabase().getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(getSQLQuery())) {

                T t = null;
                JoinsHandler<T, T> joinsHandler = new JoinsHandler<>(table, joins);

                while (resultSet.next()) {
                    if (t == null) {
                        t = table.objectFrom(resultSet);
                    }

                    if (!joinsHandler.contains(resultSet) && joinsHandler.getObjects().size() > 0) {
                        break;
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
    public RawSingleSelectQuery<T> columns(@NotNull String @NotNull ... columns) {
        checkIfColumnsExist(columns);

        RawSingleSelectQuery<T> rawSelectQuery = new RawSingleSelectQuery<>(table);

        copyTo(rawSelectQuery);
        rawSelectQuery.columns = columns;

        return rawSelectQuery;
    }
}
