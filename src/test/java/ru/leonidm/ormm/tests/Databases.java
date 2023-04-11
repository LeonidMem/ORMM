package ru.leonidm.ormm.tests;

import ru.leonidm.ormm.orm.ORMDatabase;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMSettings;

import java.util.Objects;

public final class Databases {

    public static final ORMDatabase MYSQL = new ORMDatabase(ORMDriver.MYSQL, ORMSettings.builder()
            .setHost(Objects.requireNonNullElse(System.getenv("mysql.host"), "localhost"))
            .setPort(Integer.parseInt(Objects.requireNonNullElse(System.getenv("mysql.port"), "3306")))
            .setDatabaseName(Objects.requireNonNullElse(System.getenv("mysql.databaseName"), "ormm"))
            .setUser(Objects.requireNonNullElse(System.getenv("mysql.user"), "ormm"))
            .setPassword(Objects.requireNonNullElse(System.getenv("mysql.host"), "ormm"))
            .setConnectionParameters("createDatabaseIfNotExist=true&autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8")
            .setLogQueries(true)
            .build());

    public static final ORMDatabase SQLITE = new ORMDatabase(ORMDriver.SQLITE, ORMSettings.builder()
            .setHost("test.db")
            .setLogQueries(true)
            .build());

    private Databases() {

    }
}
