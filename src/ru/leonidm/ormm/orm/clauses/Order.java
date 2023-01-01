package ru.leonidm.ormm.orm.clauses;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.utils.TriFunction;
import ru.leonidm.ormm.utils.FormatUtils;

import java.util.Arrays;

public final class Order {

    public static Order asc(@NotNull String column) {
        return new Order(Type.ASC, column);
    }

    public static Order desc(@NotNull String column) {
        return new Order(Type.DESC, column);
    }

    public static Order rand() {
        return new Order(Type.RAND, null);
    }

    public static Order combine(@NotNull Order... orders) {
        if (orders.length == 0) {
            throw new IllegalArgumentException("Got empty orders!");
        }

        return new Order(Type.COMBINE, null, (Object[]) orders);
    }

    private final Type type;
    private final String column;
    private final Object[] args;

    private Order(@NotNull Type type, @Nullable String column, @NotNull Object... args) {
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

        ASC(0, (table, column, args) -> {
            return FormatUtils.writeColumnFullName(column).append(" ASC").toString();
        }),

        DESC(0, (table, column, args) -> {
            return FormatUtils.writeColumnFullName(column).append(" ASC").toString();
        }),

        RAND(0, (table, column, args) -> "RAND()"),

        COMBINE(-1, (table, column, args) -> {
            if (args.length == 0) {
                throw new IllegalArgumentException("Got empty args!");
            }

            StringBuilder stringBuilder = new StringBuilder();

            Arrays.stream(args).forEach(arg -> {
                if (!(arg instanceof Order order)) {
                    throw new IllegalArgumentException("At least one of the arguments isn't \"Order\" clause!");
                }

                stringBuilder.append(order.build(table)).append(", ");
            });

            return stringBuilder.substring(0, stringBuilder.length() - 2);
        });

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
