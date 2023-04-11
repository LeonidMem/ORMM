package ru.leonidm.ormm.orm.queries.select;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

                List<T> out = new ArrayList<>();
                JoinsHandler<T, T> joinsHandler = new JoinsHandler<>(table, joins);

                while (resultSet.next()) {
                    T t = table.objectFrom(resultSet);
                    out.add(t);

                    joinsHandler.save(resultSet, t);
                }

                joinsHandler.apply();
                return out;
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    @NotNull
    public SingleSelectQuery<T> single() {
        SingleSelectQuery<T> singleSelectQuery = new SingleSelectQuery<>(table);

        copy(singleSelectQuery);
        singleSelectQuery.limit = 1;

        return singleSelectQuery;
    }

    @NotNull
    public RawSelectQuery<T> columns(String @NotNull ... columns) {
        checkIfColumnsExist(columns);

        RawSelectQuery<T> rawSelectQuery = new RawSelectQuery<>(table);

        copy(rawSelectQuery);
        rawSelectQuery.columns = columns;

        return rawSelectQuery;
    }
}
