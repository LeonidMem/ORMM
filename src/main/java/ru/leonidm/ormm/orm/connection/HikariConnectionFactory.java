package ru.leonidm.ormm.orm.connection;

import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMSettings;

import java.sql.SQLException;

public class HikariConnectionFactory implements ConnectionFactory {

    private final HikariDataSource dataSource;

    public HikariConnectionFactory(@NotNull ORMDriver driver, @NotNull ORMSettings settings) {
        dataSource = new HikariDataSource();

        dataSource.setJdbcUrl(driver.getJdbcLink(settings));
        dataSource.setUsername(settings.getUser());
        dataSource.setPassword(settings.getPassword());
        dataSource.setMaximumPoolSize(settings.getConnectionPoolSize());
        dataSource.setConnectionTimeout(settings.getConnectionPoolTimeout());
    }

    @Override
    @NotNull
    public OrmConnection getConnection() throws SQLException {
        return new OrmConnection(this, dataSource.getConnection());
    }

    @Override
    public void releaseConnection(@NotNull OrmConnection connection) throws SQLException {
        connection.getConnection().close();
    }
}
