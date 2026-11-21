package ru.leonidm.ormm.orm.connection;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMSettings;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

public class StaticConnectionFactory implements ConnectionFactory {

    private static final int VALIDATION_TIMEOUT_SECONDS = 2;

    private final ReentrantLock lock = new ReentrantLock();
    private final ORMDriver driver;
    private final ORMSettings settings;
    private OrmConnection ormConnection;

    public StaticConnectionFactory(@NotNull ORMDriver driver, @NotNull ORMSettings settings) {
        this.driver = driver;
        this.settings = settings;
    }

    @Override
    @NotNull
    public OrmConnection getConnection() throws SQLException {
        lock.lock();

        try {
            if (ormConnection == null || !ormConnection.getConnection().isValid(VALIDATION_TIMEOUT_SECONDS)) {
                if (ormConnection != null) {
                    // TODO: normal logger
                    System.err.println("[ORMM] Got invalid connection, reconnecting");
                    closeQuietly(ormConnection.getConnection());
                }

                ormConnection = new OrmConnection(this, driver.getConnection(settings));
            }
        } catch (SQLException | RuntimeException e) {
            lock.unlock();
            throw e;
        }

        return ormConnection;
    }

    @Override
    public void releaseConnection(@NotNull OrmConnection ormConnection) throws SQLException {
        if (this.ormConnection != ormConnection) {
            return;
        }

        try {
            Connection connection = ormConnection.getConnection();
            if (!connection.getAutoCommit()) {
                connection.commit();
                connection.setAutoCommit(true);
            }
        } finally {
            lock.unlock();
        }
    }

    private static void closeQuietly(@NotNull Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
