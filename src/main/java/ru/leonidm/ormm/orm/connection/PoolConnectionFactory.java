package ru.leonidm.ormm.orm.connection;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMSettings;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class PoolConnectionFactory implements ConnectionFactory {

    private final Set<OrmConnection> ormConnections = new HashSet<>();
    private final ORMDriver driver;
    private final ORMSettings settings;
    private final BlockingQueue<Connection> freeConnections;
    private final int size;
    private final int timeout;
    private int totalConnections = 0;

    public PoolConnectionFactory(@NotNull ORMDriver driver, @NotNull ORMSettings settings) {
        this.driver = driver;
        this.settings = settings;
        size = settings.getConnectionPoolSize();
        timeout = settings.getConnectionPoolTimeout();
        freeConnections = new LinkedBlockingDeque<>(size);
    }

    @Override
    @NotNull
    public OrmConnection getConnection() throws SQLException {
        boolean create;

        synchronized (this) {
            if (totalConnections < size) {
                totalConnections++;
                create = true;
            } else {
                create = false;
            }
        }

        Connection connection;
        if (create) {
            connection = driver.getConnection(settings);
        } else {
            try {
                connection = freeConnections.poll(timeout, TimeUnit.MILLISECONDS);
                if (connection == null) {
                    throw new SQLException("Waited too long for free connection");
                }
            } catch (InterruptedException e) {
                throw new SQLException("Waited for free connection was interrupted", e);
            }
        }

        OrmConnection ormConnection = new OrmConnection(this, connection);
        synchronized (this) {
            ormConnections.add(ormConnection);
        }

        return ormConnection;
    }

    @Override
    public synchronized void releaseConnection(@NotNull OrmConnection ormConnection) throws SQLException {
        synchronized (this) {
            if (!ormConnections.remove(ormConnection)) {
                return;
            }
        }

        Connection connection = ormConnection.getConnection();

        if (connection.isClosed()) {
            // TODO: normal logger
            System.err.println("[ORMM] Got closed connection");

            connection = driver.getConnection(settings);
        }

        synchronized (this) {
            freeConnections.add(connection);
        }
    }
}
