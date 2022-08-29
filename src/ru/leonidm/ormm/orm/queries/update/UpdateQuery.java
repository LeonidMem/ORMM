package ru.leonidm.ormm.orm.queries.update;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.clauses.Where;
import ru.leonidm.ormm.orm.exceptions.UnsafeQueryException;

import java.util.function.Supplier;

import static ru.leonidm.ormm.utils.FormatUtils.*;

public final class UpdateQuery<T> extends AbstractUpdateQuery<UpdateQuery<T>, T, Void> {

    private int limit = 0;

    public UpdateQuery(@NotNull ORMTable<T> table) {
        super(table, null);
    }

    @NotNull
    public UpdateQuery<T> set(@NotNull String columnName, @Nullable Object object) {
        ORMColumn<T, ?> column = this.table.getColumn(columnName);
        if(column == null) {
            throw new IllegalArgumentException("Can't find column \"" + columnName.toLowerCase() + "\"!");
        }

        this.values.put(column, object);
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
        if(this.values.isEmpty()) {
            throw new IllegalArgumentException("Got no values to update!");
        }

        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("UPDATE ").append(this.table.getName()).append(" SET");

        this.values.forEach((column, value) -> {
            queryBuilder.append(' ');

            switch(this.table.getDatabase().getDriver()) {
                case MYSQL -> queryBuilder.append(this.table.getName()).append('.');
                case SQLITE -> {}
            }

            queryBuilder.append(column).append(" = ")
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
    protected Supplier<Void> prepareSupplier() {
        return getUpdateSupplier();
    }
}
