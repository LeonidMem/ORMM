package ru.leonidm.ormm.orm.queries.update;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.clauses.Where;
import ru.leonidm.ormm.orm.exceptions.UnsafeQueryException;
import ru.leonidm.ormm.utils.FormatUtils;

import java.util.function.Supplier;

public final class UpdateQuery<T> extends AbstractUpdateQuery<UpdateQuery<T>, T, Void> {

    private int limit = 0;

    public UpdateQuery(@NotNull ORMTable<T> table) {
        super(table, null);
    }

    @NotNull
    public UpdateQuery<T> set(@NotNull String columnName, @Nullable Object object) {
        ORMColumn<T, ?> column = this.table.getColumn(columnName);
        if (column == null) {
            throw new IllegalArgumentException("Can't find column \"%s\"".formatted(columnName.toLowerCase()));
        }

        this.values.put(column, object);
        return this;
    }

    @NotNull
    public UpdateQuery<T> where(@NotNull Where where) {
        if (this.object != null) {
            throw new IllegalStateException("Where statement can't be specified if object was provided");
        }

        this.where = where;
        return this;
    }

    @NotNull
    public UpdateQuery<T> limit(int limit) {
        if (this.table.getDatabase().getDriver() == ORMDriver.SQLITE) {
            throw new IllegalStateException("Cannot set limit of update query for SQLite");
        }

        this.limit = limit;
        return this;
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        if (this.values.isEmpty()) {
            throw new IllegalArgumentException("Got no values to update");
        }

        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("UPDATE ").append(this.table.getName()).append(" SET");

        this.values.forEach((column, value) -> {
            queryBuilder.append(' ');

            switch (this.table.getDatabase().getDriver()) {
                case ORMDriver.MYSQL -> queryBuilder.append(this.table.getName()).append('.');
                case ORMDriver.SQLITE -> {
                }
            }

            queryBuilder.append(column.getName()).append(" = ")
                    .append(FormatUtils.toStringSQLValue(column.toDatabaseObject(value))).append(",");
        });

        queryBuilder.delete(queryBuilder.length() - 1, queryBuilder.length());

        if (this.where != null) {
            queryBuilder.append(" WHERE ").append(this.where.build(this.table));
        } else {
            if (!this.table.getMeta().allowUnsafeOperations()) {
                throw new UnsafeQueryException("\"WHERE\" is not specified, so the query is unsafe");
            }
        }

        if (this.limit > 0) {
            queryBuilder.append(" LIMIT ").append(this.limit);
        }

        return queryBuilder.toString();
    }

    @Override
    @NotNull
    protected Supplier<Void> prepareSupplier() {
        return getUpdateSupplier();
    }
}
