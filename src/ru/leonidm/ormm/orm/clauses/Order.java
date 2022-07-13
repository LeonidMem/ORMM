package ru.leonidm.ormm.orm.clauses;

import org.jetbrains.annotations.NotNull;

// TODO: refactor as Where
public final class Order {

    public static Order asc(@NotNull String column) {
        return new Order(column + " ASC");
    }

    public static Order desc(@NotNull String column) {
        return new Order(column + " DESC");
    }

    public static Order rand() {
        return new Order("RAND()");
    }

    public static Order combine(@NotNull Order order, @NotNull Order... orders) {
        if(orders.length == 0) return order;

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(order);

        for(Order order1 : orders) {
            stringBuilder.append(", ").append(order1);
        }

        return new Order(stringBuilder.toString());
    }

    private final String formatted;

    private Order(@NotNull String formatted) {
        this.formatted = formatted;
    }

    @Override
    @NotNull
    public String toString() {
        return this.formatted;
    }
}
