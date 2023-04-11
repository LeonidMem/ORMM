package ru.leonidm.ormm.orm.queries.select;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class RawSelectQuery<T> extends AbstractSelectQuery<RawSelectQuery<T>, T, List<List<Object>>, List<Object>> {

    public RawSelectQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @Override
    @NotNull
    protected Supplier<List<List<Object>>> prepareSupplier() {
        return () -> {

            try (Statement statement = table.getDatabase().getConnection().createStatement();
                 ResultSet resultSet = statement.executeQuery(getSQLQuery())) {

                JoinsHandler<T, List<Object>> joinsHandler = new JoinsHandler<>(table, joins);

                while (resultSet.next()) {
                    List<Object> objectsList = new ArrayList<>(columns.length);

                    for (int i = 0; i < columns.length; i++) {
                        ORMColumn<T, ?> column = Objects.requireNonNull(table.getColumn(columns[i]));
                        objectsList.add(column.toFieldObject(resultSet.getObject(i + 1)));
                    }

                    if (limit > 0 && !joinsHandler.contains(resultSet) && joinsHandler.getObjects().size() >= limit) {
                        break;
                    }

                    joinsHandler.save(resultSet, objectsList);
                }

                joinsHandler.apply();
                return List.copyOf(joinsHandler.getObjects());
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    @NotNull
    public RawSingleSelectQuery<T> single() {
        RawSingleSelectQuery<T> rawSingleSelectQuery = new RawSingleSelectQuery<>(table);

        copy(rawSingleSelectQuery);
        rawSingleSelectQuery.limit = 1;

        return rawSingleSelectQuery;
    }
}
