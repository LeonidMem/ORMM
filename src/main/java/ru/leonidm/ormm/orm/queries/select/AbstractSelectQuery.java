package ru.leonidm.ormm.orm.queries.select;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.clauses.JoinType;
import ru.leonidm.ormm.orm.clauses.JoinWhere;
import ru.leonidm.ormm.orm.clauses.Order;
import ru.leonidm.ormm.orm.clauses.Where;
import ru.leonidm.ormm.orm.queries.AbstractQuery;
import ru.leonidm.ormm.utils.QueryUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public sealed abstract class AbstractSelectQuery<O extends AbstractSelectQuery<O, T, R, J>, T, R, J> extends AbstractQuery<T, R>
        permits SelectQuery, SingleSelectQuery, RawSelectQuery, RawSingleSelectQuery {

    protected final List<Join<O, T, R, J>> joins = new ArrayList<>();
    protected String[] columns;
    protected Where where = null;
    protected Order order = null;
    protected String group = null;
    protected int limit = 0;

    public AbstractSelectQuery(@NotNull ORMTable<T> table) {
        super(table);

        this.columns = table.getColumnsStream()
                .map(ORMColumn::getName)
                .toArray(String[]::new);
    }

    @NotNull
    public O where(@NotNull Where where) {
        this.where = where;
        return (O) this;
    }

    @NotNull
    public O order(@NotNull Order @NotNull ... orders) {
        this.order = Order.combine(orders);
        return (O) this;
    }

    @NotNull
    public O group(@NotNull String column, @NotNull String @NotNull ... columns) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(column);

        for (String column1 : columns) {
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

    private void validateTable(@Nullable ORMTable<?> ormTable, @NotNull String tableKy) {
        if (ormTable == null) {
            throw new IllegalArgumentException("Cannot find table \"%s\"".formatted(tableKy));
        }

        if (ormTable == this.table) {
            throw new IllegalArgumentException("Cannot join the same table");
        }
    }

    @NotNull
    public JoinBuilder<O, T, R, J> join(@NotNull JoinType joinType, @NotNull String tableName) {
        ORMTable<?> ormTable = this.table.getDatabase().getTable(tableName);
        validateTable(ormTable, tableName);

        return new JoinBuilder<>(joinType, ormTable, (O) this);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> join(@NotNull JoinType joinType, @NotNull Class<?> tableClass) {
        ORMTable<?> ormTable = this.table.getDatabase().getTable(tableClass);
        validateTable(ormTable, tableClass.getName());

        return new JoinBuilder<>(joinType, ormTable, (O) this);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> leftJoin(@NotNull String tableName) {
        return join(JoinType.LEFT, tableName);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> leftJoin(@NotNull Class<?> tableClass) {
        return join(JoinType.LEFT, tableClass);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> rightJoin(@NotNull String tableName) {
        return join(JoinType.RIGHT, tableName);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> rightJoin(@NotNull Class<?> tableClass) {
        return join(JoinType.RIGHT, tableClass);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> innerJoin(@NotNull String tableName) {
        return join(JoinType.INNER, tableName);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> innerJoin(@NotNull Class<?> tableClass) {
        return join(JoinType.INNER, tableClass);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> outerJoin(@NotNull String tableName) {
        return join(JoinType.OUTER, tableName);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> outerJoin(@NotNull Class<?> tableClass) {
        return join(JoinType.OUTER, tableClass);
    }

    @Override
    @NotNull
    public final String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT ");

        String tableName = QueryUtils.getTableName(this.table);

        switch (this.table.getDatabase().getDriver()) {
            case MYSQL -> {
                Arrays.stream(this.columns).forEach(column -> {
                    queryBuilder.append(tableName).append('.').append(column).append(", ");
                });

                this.joins.forEach(join -> {
                    for (ORMColumn<?, ?> column : join.columns.keySet()) {
                        queryBuilder.append(QueryUtils.getTableName(column)).append('.').append(column.getName()).append(", ");
                    }
                });
            }
            case SQLITE -> {
                Arrays.stream(this.columns).forEach(column -> {
                    queryBuilder.append(tableName).append('.').append(column).append(" AS \"")
                            .append(tableName).append('.').append(column).append("\"").append(", ");
                });

                this.joins.forEach(join -> {
                    for (ORMColumn<?, ?> column : join.columns.keySet()) {
                        String tableName1 = QueryUtils.getTableName(column);
                        queryBuilder.append(tableName1).append('.').append(column.getName()).append(" AS \"")
                                .append(tableName1).append('.').append(column.getName()).append("\"").append(", ");
                    }
                });
            }
        }

        queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length());

        queryBuilder.append(" FROM ").append(QueryUtils.getTableName(this.table));

        this.joins.forEach(join -> {
            queryBuilder.append(' ').append(join.joinType).append(" JOIN ").append(QueryUtils.getTableName(join.table))
                    .append(" ON ").append(join.where.build(this.table, join.table));
        });

        if (this.where != null) {
            queryBuilder.append(" WHERE ").append(this.where.build(this.table));
        }

        if (this.order != null) {
            queryBuilder.append(" ORDER BY ").append(this.order);
        }

        if (this.group != null) {
            queryBuilder.append(" GROUP BY ").append(this.group);
        }

        if (this.limit > 0) {
            queryBuilder.append(" LIMIT ").append(this.limit);
        }

        return queryBuilder.toString();
    }

    protected final void copy(@NotNull AbstractSelectQuery<?, T, ?, ?> to) {
        if (!this.joins.isEmpty()) {
            throw new IllegalStateException("Change state of select query (raw/single) before inner join");
        }

        to.columns = this.columns;
        to.where = this.where;
        to.order = this.order;
        to.group = this.group;
        to.limit = this.limit;
    }

    protected void checkIfColumnsExist(@NotNull String[] columns) {
        Set<String> columnsNames = this.table.getColumnsNames();
        if (Arrays.stream(columns).anyMatch(columnName -> !columnsNames.contains(columnName))) {
            throw new IllegalArgumentException("Got columns that don't exist");
        }
    }

    public static final class JoinBuilder<O extends AbstractSelectQuery<O, T, R, J>, T, R, J> {

        private final JoinType joinType;
        private final ORMTable<?> table;
        private final O o;
        private JoinWhere where;
        private final Map<ORMColumn<?, ?>, BiConsumer<J, Object>> columns = new LinkedHashMap<>();

        private JoinBuilder(@NotNull JoinType joinType, @NotNull ORMTable<?> table, @NotNull O o) {
            this.joinType = joinType;
            this.table = table;
            this.o = o;
        }

        @NotNull
        public JoinBuilder<O, T, R, J> on(@NotNull JoinWhere where) {
            this.where = where;
            return this;
        }

        @NotNull
        public JoinBuilder<O, T, R, J> select(@NotNull String columnName, @NotNull BiConsumer<J, Object> consumer) {
            ORMColumn<?, ?> column = this.table.getColumn(columnName);
            if (column == null) {
                throw new IllegalArgumentException("Cannot find column \"%s\" in table \"%s\""
                        .formatted(columnName, QueryUtils.getTableName(this.table)));
            }

            this.columns.put(column, consumer);
            return this;
        }

        @NotNull
        public O finish() {
            if (this.where == null || this.columns.isEmpty()) {
                throw new NullPointerException("Not all parameters were given");
            }

            o.joins.add(new Join<>(joinType, table, where, Collections.unmodifiableMap(columns)));
            return o;
        }
    }

    @AllArgsConstructor
    static final class Join<O extends AbstractSelectQuery<O, T, R, J>, T, R, J> {
        private final JoinType joinType;
        private final ORMTable<?> table;
        private final JoinWhere where;
        private final Map<ORMColumn<?, ?>, BiConsumer<J, Object>> columns;

        @NotNull
        @Unmodifiable
        public Map<ORMColumn<?, ?>, BiConsumer<J, Object>> getColumns() {
            return columns;
        }
    }
}
