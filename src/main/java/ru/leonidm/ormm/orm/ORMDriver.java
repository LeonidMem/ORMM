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
            return DriverManager.getConnection(getJdbcLink(ormSettings));
        }

        @Override
        @NotNull
        public String getJdbcLink(@NotNull ORMSettings ormSettings) {
            String jdbcLink = ormSettings.getJdbcLink();
            if (jdbcLink != null) {
                return jdbcLink;
            } else {
                if (ormSettings.getHost().equals(":memory:")) {
                    return "jdbc:sqlite::memory:";
                }

                return "jdbc:sqlite:" + ormSettings.getHost() + "/?" + ormSettings.getConnectionParameters();
            }
        }
    },
    MYSQL("jdbc:mysql:", "AUTO_INCREMENT", "IGNORE") {
        @Override
        @NotNull
        public Connection getConnection(@NotNull ORMSettings ormSettings) throws SQLException {
            return DriverManager.getConnection(getJdbcLink(ormSettings), ormSettings.getUser(), ormSettings.getPassword());
        }

        @Override
        @NotNull
        public String getJdbcLink(@NotNull ORMSettings ormSettings) {
            String jdbcLink = ormSettings.getJdbcLink();
            if (jdbcLink != null) {
                return jdbcLink;
            } else {
                return "jdbc:mysql://" + ormSettings.getHost() + ":" + ormSettings.getPort()
                                + "/" + ormSettings.getDatabaseName() + "?" + ormSettings.getConnectionParameters();
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

    @NotNull
    public abstract String getJdbcLink(@NotNull ORMSettings ormSettings);

    public enum Key {
        LINK_PREFIX,
        AUTOINCREMENT,
        INSERT_IGNORE
    }
}
