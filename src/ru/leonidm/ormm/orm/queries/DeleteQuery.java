package ru.leonidm.ormm.orm.queries;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.clauses.Where;
import ru.leonidm.ormm.orm.exceptions.UnsafeQueryException;

import java.util.function.Supplier;

public final class DeleteQuery<T> extends AbstractQuery<T, Void> {

    private Where where;
    private int limit = 0;

    public DeleteQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @NotNull
    public DeleteQuery<T> where(@NotNull Where where) {
        this.where = where;
        return this;
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("DELETE FROM ").append(this.table.getName());

        if(this.where != null) {
            queryBuilder.append(" WHERE ").append(this.where.build(this.table));
        }
        else {
            if(!this.table.getMeta().allowUnsafeOperations()) {
                throw new UnsafeQueryException("\"WHERE\" is not specified, so the query is unsafe!");
            }
        }

        if(this.limit != 0) {
            queryBuilder.append(" LIMIT ").append(this.limit);
        }

        return queryBuilder.toString();
    }

    @Override
    @NotNull
    protected Supplier<Void> prepareSupplier() {
        return this.getUpdateSupplier();
    }
}
