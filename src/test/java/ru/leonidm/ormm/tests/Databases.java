package ru.leonidm.ormm.tests;

import ru.leonidm.ormm.orm.ORMDatabase;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMSettings;
import ru.leonidm.ormm.orm.connection.HikariConnectionFactory;
import ru.leonidm.ormm.orm.connection.OrmConnection;
import ru.leonidm.ormm.orm.connection.PoolConnectionFactory;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public final class Databases {

    public static final ORMDatabase MYSQL;
    public static final ORMDatabase MYSQL_POOL;
    public static final ORMDatabase MYSQL_HIKARI;
    public static final ORMDatabase SQLITE;

    static {
        MYSQL = new ORMDatabase(ORMDriver.MYSQL, ORMSettings.builder()
                .setHost(Objects.requireNonNullElse(System.getenv("mysql.host"), "localhost"))
                .setPort(Integer.parseInt(Objects.requireNonNullElse(System.getenv("mysql.port"), "3306")))
                .setDatabaseName(Objects.requireNonNullElse(System.getenv("mysql.databaseName"), "ormm"))
                .setUser(Objects.requireNonNullElse(System.getenv("mysql.user"), "ormm"))
                .setPassword(Objects.requireNonNullElse(System.getenv("mysql.host"), "ormm"))
                .setConnectionParameters("createDatabaseIfNotExist=true&autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8")
                .setLogQueries(true)
                .build());

        try (OrmConnection connection = MYSQL.getConnection();
             Statement statement = connection.createStatement()) {
            String databaseName = Objects.requireNonNullElse(System.getenv("mysql.databaseName"), "ormm");
            statement.executeUpdate("DROP DATABASE " + databaseName);
            statement.executeUpdate("CREATE DATABASE " + databaseName);
            statement.executeUpdate("USE " + databaseName);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

        MYSQL_POOL = new ORMDatabase(ORMDriver.MYSQL, ORMSettings.builder()
                .setHost(Objects.requireNonNullElse(System.getenv("mysql.host"), "localhost"))
                .setPort(Integer.parseInt(Objects.requireNonNullElse(System.getenv("mysql.port"), "3306")))
                .setDatabaseName(Objects.requireNonNullElse(System.getenv("mysql.databaseName"), "ormm"))
                .setUser(Objects.requireNonNullElse(System.getenv("mysql.user"), "ormm"))
                .setPassword(Objects.requireNonNullElse(System.getenv("mysql.host"), "ormm"))
                .setConnectionParameters("createDatabaseIfNotExist=true&autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8")
                .setTableNamePrefix("pool_")
                .setLogQueries(true)
                .setConnectionPoolSize(4)
                .setConnectionFactoryInitializer(PoolConnectionFactory::new)
                .build());

        MYSQL_HIKARI = new ORMDatabase(ORMDriver.MYSQL, ORMSettings.builder()
                .setHost(Objects.requireNonNullElse(System.getenv("mysql.host"), "localhost"))
                .setPort(Integer.parseInt(Objects.requireNonNullElse(System.getenv("mysql.port"), "3306")))
                .setDatabaseName(Objects.requireNonNullElse(System.getenv("mysql.databaseName"), "ormm"))
                .setUser(Objects.requireNonNullElse(System.getenv("mysql.user"), "ormm"))
                .setPassword(Objects.requireNonNullElse(System.getenv("mysql.host"), "ormm"))
                .setConnectionParameters("createDatabaseIfNotExist=true&autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8")
                .setTableNamePrefix("hikari_")
                .setLogQueries(true)
                .setConnectionPoolSize(4)
                .setConnectionFactoryInitializer(HikariConnectionFactory::new)
                .build());

        new File("test.db").delete();

        SQLITE = new ORMDatabase(ORMDriver.SQLITE, ORMSettings.builder()
                .setHost("test.db")
                .setLogQueries(true)
                .build());
    }

    private Databases() {

    }
}
