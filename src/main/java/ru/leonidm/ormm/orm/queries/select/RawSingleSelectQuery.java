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

public final class RawSingleSelectQuery<T> extends AbstractSelectQuery<RawSingleSelectQuery<T>, T, List<Object>, List<Object>> {

    public RawSingleSelectQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @Override
    @NotNull
    public RawSingleSelectQuery<T> limit(int limit) {
        throw new IllegalStateException("Can't change limit of RawSingleSelectQuery");
    }

    @Override
    @NotNull
    protected Supplier<List<Object>> prepareSupplier() {
        return () -> {
            try (Statement statement = table.getDatabase().getConnection().createStatement();
                 ResultSet resultSet = statement.executeQuery(getSQLQuery())) {

                List<Object> objectsList = null;
                JoinsHandler<T, List<Object>> joinsHandler = new JoinsHandler<>(table, joins);

                while (resultSet.next()) {
                    if (objectsList == null) {
                        objectsList = new ArrayList<>();
                        for (int i = 0; i < columns.length; i++) {
                            ORMColumn<T, ?> column = Objects.requireNonNull(table.getColumn(columns[i]));
                            objectsList.add(column.toFieldObject(resultSet.getObject(i + 1)));
                        }
                    }

                    if (!joinsHandler.contains(resultSet) && joinsHandler.getObjects().size() > 0) {
                        break;
                    }

                    joinsHandler.save(resultSet, objectsList);
                }

                joinsHandler.apply();
                return objectsList;
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        };
    }
}
