package ru.leonidm.ormm.orm.connection;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class OrmConnection implements AutoCloseable {

    private final ConnectionFactory connectionFactory;
    private final Connection connection;

    public OrmConnection(@NotNull ConnectionFactory connectionFactory, @NotNull Connection connection) {
        this.connectionFactory = connectionFactory;
        this.connection = connection;
    }

    @NotNull
    public Connection getConnection() {
        return connection;
    }

    public boolean isClosed() throws SQLException {
        return connection.isClosed();
    }

    @NotNull
    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    @Override
    public void close() throws SQLException {
        connectionFactory.releaseConnection(this);
    }
}
