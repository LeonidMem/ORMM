package ru.leonidm.ormm.orm.clauses;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.commons.functions.TriFunction;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.utils.FormatUtils;

import java.util.Arrays;

public final class Order {

    @NotNull
    public static Order asc(@NotNull String column) {
        return new Order(Type.ASC, column);
    }

    @NotNull
    public static Order desc(@NotNull String column) {
        return new Order(Type.DESC, column);
    }

    @NotNull
    public static Order rand() {
        return new Order(Type.RAND, null);
    }

    @Contract("_ -> new")
    @NotNull
    public static Order combine(@NotNull Order @NotNull ... orders) {
        if (orders.length == 0) {
            throw new IllegalArgumentException("Got empty orders");
        }

        return new Order(Type.COMBINE, null, (Object[]) orders);
    }

    private final Type type;
    private final String column;
    private final Object[] args;

    private Order(@NotNull Type type, @Nullable String column, @NotNull Object @NotNull ... args) {
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
        return type.build(table, column, args);
    }

    private enum Type {

        ASC(0, (table, column, args) -> {
            return FormatUtils.writeColumnFullName(column).append(" ASC").toString();
        }),

        DESC(0, (table, column, args) -> {
            return FormatUtils.writeColumnFullName(column).append(" DESC").toString();
        }),

        RAND(0, (table, column, args) -> switch (table.getDatabase().getDriver()) {
            case MYSQL -> "RAND()";
            case SQLITE -> "RANDOM()";
        }),

        COMBINE(-1, (table, column, args) -> {
            if (args.length == 0) {
                throw new IllegalArgumentException("Got empty args");
            }

            StringBuilder stringBuilder = new StringBuilder();

            Arrays.stream(args).forEach(arg -> {
                if (!(arg instanceof Order order)) {
                    throw new IllegalArgumentException("At least one of the arguments isn't \"Order\" clause");
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
        private String build(@NotNull ORMTable<?> table, @Nullable String columnName, @NotNull Object @NotNull ... args) {
            if (argsAmount >= 0 && argsAmount != args.length) {
                throw new IllegalArgumentException("Provided arguments has wrong amount");
            }

            ORMColumn<?, ?> column;
            if (columnName != null) {
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
