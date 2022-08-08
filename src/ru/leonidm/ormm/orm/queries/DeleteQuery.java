package ru.leonidm.ormm.orm.queries;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.clauses.Where;

import java.util.function.Supplier;

public final class DeleteQuery<T> extends AbstractQuery<T, Void> {

    private Where where;

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

        return queryBuilder.toString();
    }

    @Override
    @NotNull
    protected Supplier<Void> prepareSupplier() {
        return this.getUpdateSupplier();
    }
}
