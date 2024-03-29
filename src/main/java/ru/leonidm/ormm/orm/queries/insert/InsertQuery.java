package ru.leonidm.ormm.orm.queries.insert;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.utils.ClassUtils;
import ru.leonidm.ormm.utils.ReflectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class InsertQuery<T> extends AbstractInsertQuery<T> {

    public InsertQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @NotNull
    public InsertQuery<T> value(@NotNull String column, @Nullable Object value) {
        if (table.getColumn(column) == null) {
            throw new IllegalArgumentException("Can't find column \"%s\"".formatted(column.toLowerCase()));
        }

        values.put(column.toLowerCase(), value);
        return this;
    }

    @Override
    @NotNull
    protected T getObjectToReturn(@NotNull Statement statement) throws SQLException {
        T t = ReflectionUtils.getNewInstance(table.getEntityClass());

        table.getColumnsStream().forEach(column ->
                column.setValue(t, column.toFieldObject(values.get(column.getName()))));

        ResultSet generatedKeys = statement.getGeneratedKeys();

        ORMColumn<T, ?> keyColumn = table.getKeyColumn();
        if (keyColumn != null && !values.containsKey(keyColumn.getName()) && generatedKeys.next()) {
            ORMColumn<T, ?> column = table.getKeyColumn();

            if (ClassUtils.isInteger(column.getFieldClass())) {
                column.setValue(t, generatedKeys.getInt(1));
            } else if (ClassUtils.isLong(column.getFieldClass())) {
                column.setValue(t, generatedKeys.getLong(1));
            } else {
                throw new IllegalStateException("Got wrong @PrimaryKey with wrong field class");
            }
        }

        return t;
    }
}
