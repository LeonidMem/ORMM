package ru.leonidm.ormm.orm.clauses;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.queries.select.AbstractSelectQuery;
import ru.leonidm.ormm.orm.utils.TriFunction;
import ru.leonidm.ormm.utils.ClassUtils;
import ru.leonidm.ormm.utils.FormatUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Where {

    private static final Set<String> OPERANDS = new HashSet<>(Arrays.asList("<", "<=", "=", ">=", ">", "<>", "!="));

    public static Where compare(@NotNull String column, @NotNull String operand, @Nullable Object value) {
        if (!OPERANDS.contains(operand)) {
            throw new IllegalArgumentException("Unknown operand \"" + operand + "\"!");
        }

        return new Where(Type.COMPARE, column, operand, value);
    }

    public static Where like(@NotNull String column, @NotNull String value) {
        return new Where(Type.LIKE, column, value);
    }

    public static Where in(@NotNull String column, @NotNull Object... objects) {
        if (objects.length < 2) {
            throw new IllegalArgumentException("Wrong amounts of the objects! Must be two or more!");
        }

        return new Where(Type.LIKE, column, objects);
    }

    public static Where isNull(@NotNull String column) {
        return new Where(Type.IS_NULL, column);
    }

    public static Where isNotNull(@NotNull String column) {
        return new Where(Type.IS_NOT_NULL, column);
    }

    public static Where and(@NotNull Where... wheres) {
        if (wheres.length < 2) {
            throw new IllegalArgumentException("Wrong amounts of the \"Where\" clauses! Must be two or more!");
        }

        return new Where(Type.AND, null, (Object[]) wheres);
    }

    public static Where or(@NotNull Where... wheres) {
        if (wheres.length < 2) {
            throw new IllegalArgumentException("Wrong amounts of the \"Where\" clauses! Must be two or more!");
        }

        return new Where(Type.OR, null, (Object[]) wheres);
    }

    public static Where not(@NotNull Where where) {
        return new Where(Type.NOT, null, where);
    }

    private final Type type;
    private final String column;
    private final Object[] args;

    private Where(@NotNull Type type, @Nullable String column, @Nullable Object... args) {
        this.type = type;
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
        return this.type.build(table, this.column, this.args);
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
                    throw new IllegalArgumentException("At least one of the arguments isn't \"Where\" clause!");
                }

                stringBuilder.append(where.build(table)).append(") AND (");
            });

            return stringBuilder.delete(stringBuilder.length() - 5, stringBuilder.length()).toString();
        }),

        OR(-1, (table, column, args) -> {
            StringBuilder stringBuilder = new StringBuilder("(");

            Arrays.stream(args).forEach(arg -> {
                if (!(arg instanceof Where where)) {
                    throw new IllegalArgumentException("At least one of the arguments isn't \"Where\" clause!");
                }

                stringBuilder.append(where.build(table)).append(") OR (");
            });

            return stringBuilder.delete(stringBuilder.length() - 5, stringBuilder.length()).toString();
        }),

        NOT(1, (table, column, args) -> {
            if (!(args[0] instanceof Where where)) {
                throw new IllegalArgumentException("At least one of the arguments isn't \"Where\" clause!");
            }

            return "NOT (" + where.build(table) + ")";
        });

        @NotNull
        private static String toStringSQLValue(@NotNull ORMColumn<?, ?> column, @Nullable Object arg) {
            if (arg == null) {
                return FormatUtils.toStringSQLValue(null);
            }

            if (ClassUtils.isBuiltIn(arg.getClass()) || arg instanceof AbstractSelectQuery<?, ?, ?>) {
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
        private String build(@NotNull ORMTable<?> table, @Nullable String columnName, @NotNull Object... args) {
            if (this.argsAmount >= 0) {
                if (this.argsAmount != args.length) {
                    throw new IllegalArgumentException("Provided arguments has wrong amount!");
                }
            }

            ORMColumn<?, ?> column;
            if (columnName != null) {
                column = table.getColumn(columnName);
                if (column == null) {
                    throw new IllegalArgumentException(table.getIdentifier() + " Unknown column \"" + columnName + "\"!");
                }
            } else {
                column = null;
            }

            return this.function.apply(table, column, args);
        }
    }
}
