package ru.leonidm.ormm.orm.connection;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMSettings;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

public class StaticConnectionFactory implements ConnectionFactory {

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
        boolean p = lock.isLocked();
        if (p) {
            System.out.println("[StaticConnectionFactory:26] LOCKED");
        }
        lock.lock();
        if (p) {
            System.out.println("[StaticConnectionFactory:26] UNLOCKED");
        }

        if (ormConnection == null || ormConnection.isClosed()) {
            if (ormConnection != null) {
                // TODO: normal logger
                System.err.println("[ORMM] Got closed connection");
            }

            ormConnection = new OrmConnection(this, driver.getConnection(settings));
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
}
