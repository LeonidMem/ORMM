package ru.leonidm.ormm.orm.general;

import org.jetbrains.annotations.NotNull;

public final class ColumnData {

    private final String table;
    private final String name;
    private final String type;
    private final int length;

    public ColumnData(@NotNull String table, @NotNull String name, @NotNull String type, int length) {
        this.table = table;
        this.name = name;
        this.type = type;
        this.length = length;
    }

    @NotNull
    public String getTable() {
        return table;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "ColumnData{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", length=" + length +
                '}';
    }
}
