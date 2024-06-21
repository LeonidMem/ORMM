package ru.leonidm.ormm.orm.connection;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMSettings;

import java.util.function.BiFunction;

public final class ConnectionFactories {

    private static final BiFunction<ORMDriver, ORMSettings, ConnectionFactory> FACTORY_INITIALIZER;

    static {
        BiFunction<ORMDriver, ORMSettings, ConnectionFactory> factoryInitializer;
        try {
            Class.forName("com.zaxxer.hikari.pool.HikariPool");

            factoryInitializer = (driver, ormSettings) -> {
                if (ormSettings.getConnectionPoolSize() > 1) {
                    return new HikariConnectionFactory(driver, ormSettings);
                } else {
                    return new StaticConnectionFactory(driver, ormSettings);
                }
            };
        } catch (ClassNotFoundException e) {
            factoryInitializer = (driver, ormSettings) -> {
                if (ormSettings.getConnectionPoolSize() > 1) {
                    // TODO: normal logger
                    System.err.println("[ORMM] It is highly recommended to use Hikari connection pool library");
                    return new PoolConnectionFactory(driver, ormSettings);
                } else {
                    return new StaticConnectionFactory(driver, ormSettings);
                }
            };
        }

        FACTORY_INITIALIZER = factoryInitializer;
    }

    private ConnectionFactories() {

    }

    @NotNull
    public static ConnectionFactory create(@NotNull ORMDriver driver, @NotNull ORMSettings settings) {
        return FACTORY_INITIALIZER.apply(driver, settings);
    }
}
