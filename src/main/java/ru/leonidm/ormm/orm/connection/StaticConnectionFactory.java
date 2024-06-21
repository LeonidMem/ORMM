package ru.leonidm.ormm.orm.connection;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMSettings;

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
        lock.lock();

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
    public void releaseConnection(@NotNull OrmConnection ormConnection) {
        if (this.ormConnection != ormConnection) {
            return;
        }

        lock.unlock();
    }
}
