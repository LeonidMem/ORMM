package ru.leonidm.ormm.orm;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

public enum ORMDriver {
    SQLITE("jdbc:sqlite:", "AUTOINCREMENT", "OR IGNORE"),
    MYSQL("jdbc:mysql:", "AUTO_INCREMENT", "IGNORE");

    private final EnumMap<Key, String> keys = new EnumMap<>(Key.class);

    ORMDriver(@NotNull String linkPrefix, @NotNull String autoincrement, @NotNull String insertIgnore) {
        this.keys.put(Key.LINK_PREFIX, linkPrefix);
        this.keys.put(Key.AUTOINCREMENT, autoincrement);
        this.keys.put(Key.INSERT_IGNORE, insertIgnore);
    }

    @NotNull
    public String get(@NotNull Key key) {
        return this.keys.get(key);
    }

    public enum Key {
        LINK_PREFIX,
        AUTOINCREMENT,
        INSERT_IGNORE
    }
}
