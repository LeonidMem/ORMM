package ru.leonidm.ormm.orm.queries.select;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.clauses.Order;
import ru.leonidm.ormm.orm.clauses.Where;
import ru.leonidm.ormm.orm.queries.AbstractQuery;

import java.util.Arrays;
import java.util.Set;

public abstract class AbstractSelectQuery<O extends AbstractSelectQuery<O, T, R>, T, R> extends AbstractQuery<T, R> {

    protected String[] columns = {"*"};
    protected Where where = null;
    protected Order order = null;
    // TODO: Group group
    protected String group = null;
    protected int limit = 0;

    public AbstractSelectQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @NotNull
    public O where(@NotNull Where where) {
        this.where = where;
        return (O) this;
    }

    @NotNull
    public O order(@NotNull Order... orders) {
        this.order = Order.combine(orders);
        return (O) this;
    }

    @NotNull
    public O group(@NotNull String column, @NotNull String... columns) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(column);

        for(String column1 : columns) {
            stringBuilder.append(", ").append(column1);
        }

        this.group = stringBuilder.toString();
        return (O) this;
    }

    @NotNull
    public O limit(int limit) {
        this.limit = limit;
        return (O) this;
    }

    @Override
    @NotNull
    public final String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT ");

        Arrays.stream(this.columns).forEach(column -> {
            queryBuilder.append(this.table.getName()).append('.').append(column).append(", ");
        });

        queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length());

        queryBuilder.append(" FROM ").append(this.table.getName());

        if(this.where != null) {
            queryBuilder.append(" WHERE ").append(this.where.build(this.table));
        }

        if(this.order != null) {
            queryBuilder.append(" ORDER BY ").append(this.order);
        }

        if(this.group != null) {
            queryBuilder.append(" GROUP BY ").append(this.group);
        }

        if(this.limit > 0) {
            queryBuilder.append(" LIMIT ").append(this.limit);
        }

        return queryBuilder.toString();
    }

    protected final void copy(AbstractSelectQuery<?, T, ?> to) {
        to.columns = this.columns;
        to.where = this.where;
        to.order = this.order;
        to.group = this.group;
        to.limit = this.limit;
    }

    protected void checkIfColumnsExist(@NotNull String[] columns) {
        Set<String> columnsNames = this.table.getColumnsNames();
        if(Arrays.stream(columns).anyMatch(columnName -> !columnsNames.contains(columnName))) {
            throw new IllegalArgumentException("Got columns that don't exist!");
        }
    }
}
