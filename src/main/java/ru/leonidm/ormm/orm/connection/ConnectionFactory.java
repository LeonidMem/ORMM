package ru.leonidm.ormm.orm.connection;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public interface ConnectionFactory {

    @NotNull
    OrmConnection getConnection() throws SQLException;

    void releaseConnection(@NotNull OrmConnection connection) throws SQLException;

}
