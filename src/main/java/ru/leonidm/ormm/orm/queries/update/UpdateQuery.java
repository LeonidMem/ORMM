package ru.leonidm.ormm.orm.queries.update;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.clauses.Where;
import ru.leonidm.ormm.orm.exceptions.UnsafeQueryException;
import ru.leonidm.ormm.utils.FormatUtils;
import ru.leonidm.ormm.utils.QueryUtils;

import java.util.function.Supplier;

public final class UpdateQuery<T> extends AbstractUpdateQuery<UpdateQuery<T>, T, Integer> {

    private int limit = 0;

    public UpdateQuery(@NotNull ORMTable<T> table) {
        super(table, null);
    }

    @NotNull
    public UpdateQuery<T> set(@NotNull String columnName, @Nullable Object object) {
        ORMColumn<T, ?> column = table.getColumn(columnName);
        if (column == null) {
            throw new IllegalArgumentException("Can't find column \"%s\"".formatted(columnName.toLowerCase()));
        }

        values.put(column, object);
        return this;
    }

    @NotNull
    public UpdateQuery<T> where(@NotNull Where where) {
        if (object != null) {
            throw new IllegalStateException("Where statement can't be specified if object was provided");
        }

        this.where = where;
        return this;
    }

    @NotNull
    public UpdateQuery<T> limit(int limit) {
        if (table.getDatabase().getDriver() == ORMDriver.SQLITE) {
            throw new IllegalStateException("Cannot set limit of update query for SQLite");
        }

        this.limit = limit;
        return this;
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Got no values to update");
        }

        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("UPDATE ").append(QueryUtils.getTableName(table)).append(" SET");

        values.forEach((column, value) -> {
            queryBuilder.append(' ');

            switch (table.getDatabase().getDriver()) {
                case MYSQL -> queryBuilder.append(QueryUtils.getTableName(table)).append('.');
                case SQLITE -> {
                }
            }

            queryBuilder.append(column.getName()).append(" = ")
                    .append(FormatUtils.toStringSQLValue(column.toDatabaseObject(value))).append(",");
        });

        queryBuilder.delete(queryBuilder.length() - 1, queryBuilder.length());

        if (where != null) {
            queryBuilder.append(" WHERE ").append(where.build(table));
        } else {
            if (!table.getMeta().allowUnsafeOperations()) {
                throw new UnsafeQueryException("\"WHERE\" is not specified, so the query is unsafe");
            }
        }

        if (limit > 0) {
            queryBuilder.append(" LIMIT ").append(limit);
        }

        return queryBuilder.toString();
    }

    @Override
    @NotNull
    protected Supplier<Integer> prepareSupplier() {
        return getUpdateSupplier();
    }
}
