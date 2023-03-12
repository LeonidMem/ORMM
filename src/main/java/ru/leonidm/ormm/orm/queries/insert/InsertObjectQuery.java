package ru.leonidm.ormm.orm.queries.insert;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMTable;

import java.sql.SQLException;
import java.sql.Statement;

public final class InsertObjectQuery<T> extends AbstractInsertQuery<T> {

    private final T object;

    public InsertObjectQuery(@NotNull ORMTable<T> table, @NotNull T object) {
        super(table);

        this.object = object;
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        this.table.getColumnsStream().forEach(column -> {
            this.values.computeIfAbsent(column.getName(), k -> column.getValue(object));
        });

        return super.getSQLQuery();
    }

    @Override
    @NotNull
    protected T getObjectToReturn(@NotNull Statement statement) throws SQLException {
        return object;
    }
}
