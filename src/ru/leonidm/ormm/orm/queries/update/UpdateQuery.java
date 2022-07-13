package ru.leonidm.ormm.orm.queries.update;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.clauses.Where;
import ru.leonidm.ormm.orm.queries.AbstractQuery;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

import static ru.leonidm.ormm.utils.FormatUtils.*;

// TODO: SingleUpdateQuery, which takes object
public final class UpdateQuery<T> extends AbstractQuery<T, T> {

    private final T object;

    private final LinkedHashMap<String, Object> values = new LinkedHashMap<>();
    private Where where = null;
    private int limit = 0;

    public UpdateQuery(@NotNull ORMTable<T> table, @Nullable T object) {
        super(table);
        this.object = object;
        if(this.object != null) {
            ORMColumn<T, ?> keyColumn = table.getKeyColumn();
            if(keyColumn == null)
                throw new IllegalArgumentException("Object of table without key column can't be served!");

            where = Where.compare(keyColumn.getName(), "=", keyColumn.getValue(object));
        }
    }

    @NotNull
    public UpdateQuery<T> set(@NotNull String column) {
        ORMColumn<T, ?> ormColumn = this.table.getColumn(column);
        if(ormColumn == null) {
            throw new IllegalArgumentException("Can't find column \"" + column.toLowerCase() + "\"!");
        }

        this.values.put(ormColumn.getName(), ormColumn.getValue(this.object));
        return this;
    }

    @NotNull
    public UpdateQuery<T> set(@NotNull String column, @Nullable Object object) {
        if(this.table.getColumn(column) == null) {
            throw new IllegalArgumentException("Can't find column \"" + column.toLowerCase() + "\"!");
        }

        this.values.put(column.toLowerCase(), object);
        return this;
    }

    @NotNull
    public UpdateQuery<T> where(@NotNull Where where) {
        if(this.object != null)
            throw new IllegalStateException("Where statement can't be specified if object was provided!");

        this.where = where;
        return this;
    }

    @NotNull
    public UpdateQuery<T> limit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("UPDATE ").append(this.table.getName()).append(" SET");

        if(this.values.isEmpty()) {
            this.table.getColumnsStream()
                    .filter(column -> !column.getMeta().primaryKey())
                    .forEachOrdered(column -> this.values.put(column.getName(), column.getValue(this.object)));
        }

        this.values.forEach((key, value) -> {
            ORMColumn<T, ?> column = this.table.getColumn(key);

            if(this.object != null) {
                if(column.getFieldClass().isAssignableFrom(value.getClass())) {
                    column.setValue(this.object, value);
                }
                else {
                    column.setValue(this.object, column.toFieldObject(value));
                }
            }

            queryBuilder.append(' ');

            switch(this.table.getDatabase().getDriver()) {
                // TODO: does it work?
                case MYSQL -> queryBuilder.append(this.table.getName()).append('.');
                case SQLITE -> {}
            }

            queryBuilder.append(key).append(" = ")
                    .append(toStringSQLValue(column.toDatabaseObject(value))).append(",");
        });

        queryBuilder.delete(queryBuilder.length() - 1, queryBuilder.length());

        if(this.where != null) {
            queryBuilder.append(" WHERE ").append(this.where.build(this.table));
        }
        else {
            // TODO: throw unsafe operation exception
        }

        if(this.limit > 0) {
            queryBuilder.append(" LIMIT ").append(this.limit);
        }

        return queryBuilder.toString();
    }

    @Override
    @NotNull
    protected Supplier<T> prepareSupplier() {
        return () -> {
            try(Statement statement = this.table.getDatabase().getConnection().createStatement()) {
                statement.executeUpdate(getSQLQuery());
            } catch(SQLException e) {
                e.printStackTrace();
            }

            return this.object;
        };
    }
}
