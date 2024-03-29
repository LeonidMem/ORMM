package ru.leonidm.ormm.orm;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.EnumMap;

public enum ORMDriver {

    SQLITE("jdbc:sqlite:", "AUTOINCREMENT", "OR IGNORE") {
        @Override
        @NotNull
        public Connection getConnection(@NotNull ORMSettings ormSettings) throws SQLException {
            String jdbcLink = ormSettings.getJdbcLink();
            if (jdbcLink != null) {
                return DriverManager.getConnection(jdbcLink);
            } else {
                if (ormSettings.getHost().equals(":memory:")) {
                    return DriverManager.getConnection("jdbc:sqlite::memory:");
                }

                return DriverManager.getConnection("jdbc:sqlite:" + ormSettings.getHost() + "/?" + ormSettings.getConnectionParameters());
            }
        }
    },
    MYSQL("jdbc:mysql:", "AUTO_INCREMENT", "IGNORE") {
        @Override
        @NotNull
        public Connection getConnection(@NotNull ORMSettings ormSettings) throws SQLException {
            String jdbcLink = ormSettings.getJdbcLink();
            if (jdbcLink != null) {
                return DriverManager.getConnection(jdbcLink);
            } else {
                return DriverManager.getConnection("jdbc:mysql://" + ormSettings.getHost() + ":" + ormSettings.getPort()
                                + "/" + ormSettings.getDatabaseName() + "?" + ormSettings.getConnectionParameters(),
                        ormSettings.getUser(), ormSettings.getPassword());
            }
        }
    };

    private final EnumMap<Key, String> keys = new EnumMap<>(Key.class);

    ORMDriver(@NotNull String linkPrefix, @NotNull String autoincrement, @NotNull String insertIgnore) {
        keys.put(Key.LINK_PREFIX, linkPrefix);
        keys.put(Key.AUTOINCREMENT, autoincrement);
        keys.put(Key.INSERT_IGNORE, insertIgnore);
    }

    @NotNull
    public String get(@NotNull Key key) {
        return keys.get(key);
    }

    @NotNull
    public abstract Connection getConnection(@NotNull ORMSettings ormSettings) throws SQLException;

    public enum Key {
        LINK_PREFIX,
        AUTOINCREMENT,
        INSERT_IGNORE
    }
}
