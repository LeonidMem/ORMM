package ru.leonidm.ormm.orm.clauses;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.commons.functions.TriFunction;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.queries.select.AbstractSelectQuery;
import ru.leonidm.ormm.utils.ClassUtils;
import ru.leonidm.ormm.utils.FormatUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Where {

    private static final Set<String> OPERANDS = new HashSet<>(Arrays.asList("<", "<=", "=", ">=", ">", "<>", "!="));

    @NotNull
    public static Where compare(@NotNull String column, @NotNull String operand, @Nullable Object value) {
        return compare(null, column, operand, value);
    }

    @NotNull
    public static Where compare(@Nullable Class<?> entityClass, @NotNull String column, @NotNull String operand, @Nullable Object value) {
        if (!OPERANDS.contains(operand)) {
            throw new IllegalArgumentException("Unknown operand \"" + operand + "\"");
        }

        return new Where(Type.COMPARE, entityClass, column, operand, value);
    }

    @NotNull
    public static Where like(@NotNull String column, @NotNull String value) {
        return like(null, column, value);
    }

    @NotNull
    public static Where like(@Nullable Class<?> entityClass, @NotNull String column, @NotNull String value) {
        return new Where(Type.LIKE, entityClass, column, value);
    }

    @NotNull
    public static Where in(@NotNull String column, @NotNull Object @NotNull ... objects) {
        return in(null, column, objects);
    }

    @NotNull
    public static Where in(@Nullable Class<?> entityClass, @NotNull String column, @NotNull Object @NotNull ... objects) {
        if (objects.length < 2) {
            throw new IllegalArgumentException("Wrong amounts of the objects! Must be two or more");
        }

        return new Where(Type.IN, entityClass, column, objects);
    }

    @NotNull
    public static Where isNull(@NotNull String column) {
        return isNull(null, column);
    }

    @NotNull
    public static Where isNull(@Nullable Class<?> entityClass, @NotNull String column) {
        return new Where(Type.IS_NULL, entityClass, column);
    }

    @NotNull
    public static Where isNotNull(@NotNull String column) {
        return isNotNull(null, column);
    }

    @NotNull
    public static Where isNotNull(@Nullable Class<?> entityClass, @NotNull String column) {
        return new Where(Type.IS_NOT_NULL, entityClass, column);
    }

    @Contract("_ -> new")
    @NotNull
    public static Where and(@NotNull Where @NotNull ... wheres) {
        if (wheres.length < 2) {
            throw new IllegalArgumentException("Wrong amounts of the \"Where\" clauses! Must be two or more");
        }

        return new Where(Type.AND, null, null, (Object[]) wheres);
    }

    @Contract("_ -> new")
    @NotNull
    public static Where or(@NotNull Where @NotNull ... wheres) {
        if (wheres.length < 2) {
            throw new IllegalArgumentException("Wrong amounts of the \"Where\" clauses! Must be two or more");
        }

        return new Where(Type.OR, null, null, (Object[]) wheres);
    }

    @NotNull
    public static Where not(@NotNull Where where) {
        return new Where(Type.NOT, null, null, where);
    }

    private final Type type;
    private final Class<?> entityClass;
    private final String column;
    private final Object[] args;

    private Where(@NotNull Type type, @Nullable Class<?> entityClass, @Nullable String column,
                  @Nullable Object @NotNull ... args) {
        this.type = type;
        this.entityClass = entityClass;
        this.column = column;
        this.args = args;
    }

    @Override
    @NotNull
    public String toString() {
        throw new IllegalStateException();
    }

    @NotNull
    public String build(@NotNull ORMTable<?> table) {
        return type.build(table, entityClass, column, args);
    }

    private enum Type {
        COMPARE(2, (table, column, args) -> {
            return FormatUtils.writeColumnFullName(column).append(' ').append(args[0]).append(' ')
                    .append(toStringSQLValue(column, args[1])).toString();
        }),

        LIKE(1, (table, column, args) -> {
            return FormatUtils.writeColumnFullName(column)
                    .append(" LIKE ").append(toStringSQLValue(column, args[0])).toString();
        }),

        IN(-1, (table, column, args) -> {
            StringBuilder stringBuilder = FormatUtils.writeColumnFullName(column).append(" IN (");

            Arrays.stream(args).forEach(arg -> stringBuilder.append(toStringSQLValue(column, arg)).append(','));

            return stringBuilder.deleteCharAt(stringBuilder.length() - 1).append(')').toString();
        }),

        IS_NULL(0, (table, column, args) -> {
            return FormatUtils.writeColumnFullName(column).append(" IS NULL").toString();
        }),

        IS_NOT_NULL(0, (table, column, args) -> {
            return FormatUtils.writeColumnFullName(column).append(" IS NOT NULL").toString();
        }),

        AND(-1, (table, column, args) -> {
            StringBuilder stringBuilder = new StringBuilder("(");

            Arrays.stream(args).forEach(arg -> {
                if (!(arg instanceof Where where)) {
                    throw new IllegalArgumentException("At least one of the arguments isn't \"Where\" clause");
                }

                stringBuilder.append(where.build(table)).append(") AND (");
            });

            return stringBuilder.delete(stringBuilder.length() - 5, stringBuilder.length()).toString();
        }),

        OR(-1, (table, column, args) -> {
            StringBuilder stringBuilder = new StringBuilder("(");

            Arrays.stream(args).forEach(arg -> {
                if (!(arg instanceof Where where)) {
                    throw new IllegalArgumentException("At least one of the arguments isn't \"Where\" clause");
                }

                stringBuilder.append(where.build(table)).append(") OR (");
            });

            return stringBuilder.delete(stringBuilder.length() - 5, stringBuilder.length()).toString();
        }),

        NOT(1, (table, column, args) -> {
            if (!(args[0] instanceof Where where)) {
                throw new IllegalArgumentException("At least one of the arguments isn't \"Where\" clause");
            }

            return "NOT (" + where.build(table) + ")";
        });

        @NotNull
        private static String toStringSQLValue(@NotNull ORMColumn<?, ?> column, @Nullable Object arg) {
            if (arg == null) {
                return FormatUtils.toStringSQLValue(null);
            }

            if (ClassUtils.isBuiltIn(arg.getClass()) || arg instanceof AbstractSelectQuery<?, ?, ?, ?>) {
                return FormatUtils.toStringSQLValue(arg);
            }

            return FormatUtils.toStringSQLValue(column.toDatabaseObject(arg));
        }

        private final int argsAmount;
        private final TriFunction<ORMTable<?>, ORMColumn<?, ?>, Object[], String> function;

        Type(int argsAmount, TriFunction<ORMTable<?>, ORMColumn<?, ?>, Object[], String> function) {
            this.argsAmount = argsAmount;
            this.function = function;
        }

        @NotNull
        private String build(@NotNull ORMTable<?> table, @Nullable Class<?> entityClass, @Nullable String columnName,
                             @NotNull Object @NotNull ... args) {
            if (argsAmount >= 0 && argsAmount != args.length) {
                throw new IllegalArgumentException("Provided arguments has wrong amount");
            }

            ORMColumn<?, ?> column;
            if (columnName != null) {
                if (entityClass != null) {
                    table = table.getDatabase().getTable(entityClass);
                    if (table == null) {
                        throw new IllegalArgumentException("Unknown table \"" + entityClass + "\"");
                    }
                }

                column = table.getColumn(columnName);
                if (column == null) {
                    throw new IllegalArgumentException(table.getIdentifier() + " Unknown column \"" + columnName + "\"");
                }
            } else {
                column = null;
            }

            return function.apply(table, column, args);
        }
    }
}
