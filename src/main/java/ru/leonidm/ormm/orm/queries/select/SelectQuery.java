package ru.leonidm.ormm.orm.queries.select;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.utils.QueryUtils;

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
            List<T> out = new ArrayList<>();

            try (Statement statement = this.table.getDatabase().getConnection().createStatement()) {

                try (ResultSet resultSet = statement.executeQuery(getSQLQuery())) {
                    while (resultSet.next()) {
                        T t = this.table.objectFrom(resultSet);
                        out.add(t);

                        for (int i = 0; i < this.joins.size(); i++) {
                            for (var entry : this.joins.get(i).getColumns().entrySet()) {
                                ORMColumn<?, ?> column = entry.getKey();
                                var consumer = entry.getValue();

                                Object databaseObject = resultSet.getObject(QueryUtils.getTableName(column) + '.' + column.getName());
                                Object object = column.toFieldObject(databaseObject);

                                consumer.accept(t, object);
                            }
                        }
                    }
                }

            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }

            return out;
        };
    }

    @NotNull
    public SingleSelectQuery<T> single() {
        SingleSelectQuery<T> singleSelectQuery = new SingleSelectQuery<>(this.table);

        this.copy(singleSelectQuery);
        singleSelectQuery.limit = 1;

        return singleSelectQuery;
    }

    @NotNull
    public RawSelectQuery<T> columns(String @NotNull ... columns) {
        checkIfColumnsExist(columns);

        RawSelectQuery<T> rawSelectQuery = new RawSelectQuery<>(this.table);

        this.copy(rawSelectQuery);
        rawSelectQuery.columns = columns;

        return rawSelectQuery;
    }
}
