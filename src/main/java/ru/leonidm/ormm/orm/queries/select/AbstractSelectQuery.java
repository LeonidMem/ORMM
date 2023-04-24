package ru.leonidm.ormm.orm.queries.select;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public sealed abstract class AbstractSelectQuery<O extends AbstractSelectQuery<O, T, R, J>, T, R, J> extends AbstractQuery<T, R>
        permits SelectQuery, SingleSelectQuery, RawSelectQuery, RawSingleSelectQuery {

    protected final List<Join<J>> joins = new ArrayList<>();
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

    private void validateTable(@Nullable ORMTable<?> ormTable, @Nullable ORMTable<?> joinedTable, @NotNull String tableKey,
                               @NotNull String joinedTableKey) {
        if (ormTable == null) {
            throw new IllegalArgumentException("Cannot find table \"%s\"".formatted(tableKey));
        }

        if (joinedTable == null) {
            throw new IllegalArgumentException("Cannot find table \"%s\"".formatted(joinedTableKey));
        }

        if (joinedTable == ormTable) {
            throw new IllegalArgumentException("Cannot join the same table");
        }
    }

    @NotNull
    public JoinBuilder<O, T, R, J> join(@NotNull JoinType joinType, @NotNull Class<?> joinedTableClass) {
        return join(joinType, table.getEntityClass(), joinedTableClass);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> join(@NotNull JoinType joinType, @NotNull Class<?> tableClass, @NotNull Class<?> joinedTableClass) {
        ORMTable<?> ormTable = table.getDatabase().getTable(tableClass);
        ORMTable<?> joinedTable = table.getDatabase().getTable(joinedTableClass);
        validateTable(ormTable, joinedTable, tableClass.getName(), joinedTableClass.getName());

        if (table.getKeyColumn() == null) {
            throw new IllegalStateException("Cannot join to table without primary key column");
        }

        return new JoinBuilder<>(joinType, ormTable, joinedTable, (O) this);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> leftJoin(@NotNull Class<?> joinedTableClass) {
        return join(JoinType.LEFT, joinedTableClass);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> leftJoin(@NotNull Class<?> tableClass, @NotNull Class<?> joinedTableClass) {
        return join(JoinType.LEFT, tableClass, joinedTableClass);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> rightJoin(@NotNull Class<?> joinedTableClass) {
        return join(JoinType.RIGHT, joinedTableClass);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> rightJoin(@NotNull Class<?> tableClass, @NotNull Class<?> joinedTableClass) {
        return join(JoinType.RIGHT, tableClass, joinedTableClass);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> innerJoin(@NotNull Class<?> joinedTableClass) {
        return join(JoinType.INNER, joinedTableClass);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> innerJoin(@NotNull Class<?> tableClass, @NotNull Class<?> joinedTableClass) {
        return join(JoinType.INNER, tableClass, joinedTableClass);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> outerJoin(@NotNull Class<?> joinedTableClass) {
        return join(JoinType.OUTER, joinedTableClass);
    }

    @NotNull
    public JoinBuilder<O, T, R, J> outerJoin(@NotNull Class<?> tableClass, @NotNull Class<?> joinedTableClass) {
        return join(JoinType.OUTER, tableClass, joinedTableClass);
    }

    @Override
    @NotNull
    public final String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT ");

        String tableName = QueryUtils.getTableName(table);

        switch (table.getDatabase().getDriver()) {
            case MYSQL -> {
                Arrays.stream(columns).forEach(column -> {
                    queryBuilder.append(tableName).append('.').append(column).append(", ");
                });

                joins.forEach(join -> {
                    for (ORMColumn<?, ?> column : join.columns.keySet()) {
                        queryBuilder.append(QueryUtils.getTableName(column)).append('.').append(column.getName()).append(", ");
                    }
                });
            }
            case SQLITE -> {
                Arrays.stream(columns).forEach(column -> {
                    queryBuilder.append(tableName).append('.').append(column).append(" AS \"")
                            .append(tableName).append('.').append(column).append("\"").append(", ");
                });

                joins.forEach(join -> {
                    for (ORMColumn<?, ?> column : join.columns.keySet()) {
                        String tableName1 = QueryUtils.getTableName(column);
                        queryBuilder.append(tableName1).append('.').append(column.getName()).append(" AS \"")
                                .append(tableName1).append('.').append(column.getName()).append("\"").append(", ");
                    }
                });
            }
        }

        queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length());

        queryBuilder.append(" FROM ").append(QueryUtils.getTableName(table));

        joins.forEach(join -> {
            queryBuilder.append(' ').append(join.joinType).append(" JOIN ").append(QueryUtils.getTableName(join.joinedTable))
                    .append(" ON ").append(join.where.build(join.table, join.joinedTable));
        });

        if (where != null) {
            queryBuilder.append(" WHERE ").append(where.build(table));
        }

        if (order != null) {
            queryBuilder.append(" ORDER BY ").append(order.build(table));
        }

        if (group != null) {
            queryBuilder.append(" GROUP BY ").append(group);
        }

        if (limit > 0 && joins.stream()
                .map(Join::getColumns)
                .map(Map::values)
                .flatMap(Collection::stream)
                .allMatch(joinMeta -> joinMeta.one)) {
            queryBuilder.append(" LIMIT ").append(limit);
        }

        return queryBuilder.toString();
    }

    protected final void copy(@NotNull AbstractSelectQuery<?, T, ?, ?> to) {
        if (!joins.isEmpty()) {
            throw new IllegalStateException("Change state of select query (raw/single) before inner join");
        }

        to.columns = columns;
        to.where = where;
        to.order = order;
        to.group = group;
        to.limit = limit;
    }

    protected void checkIfColumnsExist(@NotNull String[] columns) {
        Set<String> columnsNames = table.getColumnsNames();
        if (Arrays.stream(columns).anyMatch(columnName -> !columnsNames.contains(columnName))) {
            throw new IllegalArgumentException("Got columns that don't exist");
        }
    }

    public static final class JoinBuilder<O extends AbstractSelectQuery<O, T, R, J>, T, R, J> {

        private final JoinType joinType;
        private final ORMTable<?> table;
        private final ORMTable<?> joinedTable;
        private final O o;
        private JoinWhere where;
        private final Map<ORMColumn<?, ?>, JoinMeta<J>> columns = new LinkedHashMap<>();

        private JoinBuilder(@NotNull JoinType joinType, @NotNull ORMTable<?> table, @NotNull ORMTable<?> joinedTable, @NotNull O o) {
            this.joinType = joinType;
            this.table = table;
            this.joinedTable = joinedTable;
            this.o = o;
        }

        @NotNull
        public JoinBuilder<O, T, R, J> on(@NotNull JoinWhere where) {
            this.where = where;
            return this;
        }

        @NotNull
        public JoinBuilder<O, T, R, J> selectOne(@NotNull String columnName, @NotNull BiConsumer<J, Object> consumer) {
            ORMColumn<?, ?> column = joinedTable.getColumn(columnName);
            if (column == null) {
                throw new IllegalArgumentException("Cannot find column \"%s\" in table \"%s\""
                        .formatted(columnName, QueryUtils.getTableName(joinedTable)));
            }

            columns.put(column, new JoinMeta<>(true, consumer));
            return this;
        }

        @NotNull
        public JoinBuilder<O, T, R, J> selectMany(@NotNull String columnName, @NotNull BiConsumer<J, List<Object>> consumer) {
            ORMColumn<?, ?> column = joinedTable.getColumn(columnName);
            if (column == null) {
                throw new IllegalArgumentException("Cannot find column \"%s\" in table \"%s\""
                        .formatted(columnName, QueryUtils.getTableName(joinedTable)));
            }

            columns.put(column, new JoinMeta<>(false, (BiConsumer) consumer));
            return this;
        }

        @NotNull
        public O finish() {
            if (where == null) {
                throw new NullPointerException("Where parameter is null");
            }

            o.joins.add(new Join<>(joinType, table, joinedTable, where, Collections.unmodifiableMap(columns)));
            return o;
        }
    }

    @AllArgsConstructor
    static final class Join<J> {
        private final JoinType joinType;
        private final ORMTable<?> table;
        private final ORMTable<?> joinedTable;
        private final JoinWhere where;
        private final Map<ORMColumn<?, ?>, JoinMeta<J>> columns;

        @NotNull
        @Unmodifiable
        public Map<ORMColumn<?, ?>, JoinMeta<J>> getColumns() {
            return columns;
        }
    }

    @AllArgsConstructor
    @Getter
    public static class JoinMeta<J> {

        private final boolean one;
        private final BiConsumer<J, Object> consumer;

    }
}
