package ru.leonidm.ormm.orm.queries.select;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Supplier;

public final class RawSingleSelectQuery<T> extends AbstractSelectQuery<RawSingleSelectQuery<T>, T, List<Object>> {

    public RawSingleSelectQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @Override
    @NotNull
    public RawSingleSelectQuery<T> limit(int limit) {
        throw new IllegalStateException("Can't change limit of RawSingleSelectQuery!");
    }

    @Override
    @NotNull
    protected Supplier<List<Object>> prepareSupplier() {
        return () -> {
            try (Statement statement = this.table.getDatabase().getConnection().createStatement()) {

                try (ResultSet resultSet = statement.executeQuery(getSQLQuery())) {
                    if (resultSet.next()) {
                        Object[] objects = new Object[this.columns.length];

                        for (int i = 0; i < this.columns.length; i++) {
                            ORMColumn<T, ?> column = this.table.getColumn(this.columns[i]);
                            objects[i] = column.toFieldObject(resultSet.getObject(i + 1));
                        }

                        return List.of(objects);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        };
    }
}
