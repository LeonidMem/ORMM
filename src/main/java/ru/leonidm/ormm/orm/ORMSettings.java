package ru.leonidm.ormm.orm;

import lombok.Builder;
import lombok.Data;
import ru.leonidm.ormm.Constant;

import java.util.Objects;

@Builder(builderClassName = "Builder", setterPrefix = "set")
@Data
public final class ORMSettings {

    private final String host;
    private final int port;
    private final String databaseName;
    @lombok.Builder.Default
    private final String tableNamePrefix = "";
    @lombok.Builder.Default
    private final String connectionParameters = "";
    private final String jdbcLink;
    private final String user;
    private final String password;
    @lombok.Builder.Default
    private final int threadPoolSize = Constant.ORMM_THREAD_POOL;
    @lombok.Builder.Default
    private final int connectionPoolSize = Constant.ORMM_CONNECTION_POOL;
    private final int connectionPoolTimeout = Constant.ORMM_CONNECTION_POOL_TIMEOUT;
    @lombok.Builder.Default
    private final boolean logQueries = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ORMSettings that = (ORMSettings) o;
        return port == that.port
                && Objects.equals(host, that.host)
                && Objects.equals(databaseName, that.databaseName)
                && Objects.equals(jdbcLink, that.jdbcLink)
                && Objects.equals(user, that.user)
                && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                host,
                port,
                databaseName,
                jdbcLink,
                user,
                password
        );
    }
}
