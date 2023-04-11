package ru.leonidm.ormm.orm.queries.select;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.utils.QueryUtils;

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
            try (Statement statement = this.table.getDatabase().getConnection().createStatement()) {

                try (ResultSet resultSet = statement.executeQuery(getSQLQuery())) {
                    if (resultSet.next()) {
                        T t = this.table.objectFrom(resultSet);

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

            return null;
        };
    }

    @NotNull
    public RawSingleSelectQuery<T> columns(String @NotNull ... columns) {
        checkIfColumnsExist(columns);

        RawSingleSelectQuery<T> rawSelectQuery = new RawSingleSelectQuery<>(this.table);

        this.copy(rawSelectQuery);
        rawSelectQuery.columns = columns;

        return rawSelectQuery;
    }
}
