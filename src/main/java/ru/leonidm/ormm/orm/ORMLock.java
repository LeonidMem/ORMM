package ru.leonidm.ormm.orm;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class ORMLock implements AutoCloseable {

    private final Connection connection;
    private final String name;

    private ORMLock(@NotNull Connection connection, @NotNull String name) {
        this.connection = connection;
        this.name = name;
    }

    @NotNull
    public static ORMLock acquire(@NotNull ORMDriver driver, @NotNull ORMSettings settings, @NotNull String name,
                                  int timeoutSeconds) throws SQLException {
        Connection connection = driver.getConnection(settings);
        try (PreparedStatement statement = connection.prepareStatement("SELECT GET_LOCK(?, ?)")) {
            statement.setString(1, name);
            statement.setInt(2, timeoutSeconds);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next() || resultSet.getInt(1) != 1) {
                    throw new SQLException("Could not acquire MySQL lock \"%s\"".formatted(name));
                }
            }
        } catch (Throwable t) {
            try (connection) {
                throw t;
            }
        }

        return new ORMLock(connection, name);
    }

    @Override
    public void close() throws SQLException {
        try (connection; PreparedStatement statement = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            statement.setString(1, name);
            statement.executeQuery().close();
        }
    }
}
