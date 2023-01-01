package ru.leonidm.ormm.orm.queries.select;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class RawSelectQuery<T> extends AbstractSelectQuery<RawSelectQuery<T>, T, List<List<Object>>> {

    public RawSelectQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @Override
    @NotNull
    protected Supplier<List<List<Object>>> prepareSupplier() {
        return () -> {
            List<List<Object>> out = new ArrayList<>();

            try (Statement statement = this.table.getDatabase().getConnection().createStatement()) {

                try (ResultSet resultSet = statement.executeQuery(getSQLQuery())) {
                    while (resultSet.next()) {
                        Object[] objects = new Object[this.columns.length];

                        for (int i = 0; i < this.columns.length; i++) {
                            ORMColumn<T, ?> column = this.table.getColumn(this.columns[i]);
                            objects[i] = column.toFieldObject(resultSet.getObject(i + 1));
                        }

                        out.add(List.of(objects));
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return out;
        };
    }

    @NotNull
    public RawSingleSelectQuery<T> single() {
        RawSingleSelectQuery<T> rawSingleSelectQuery = new RawSingleSelectQuery<>(this.table);

        this.copy(rawSingleSelectQuery);
        rawSingleSelectQuery.limit = 1;

        return rawSingleSelectQuery;
    }
}
