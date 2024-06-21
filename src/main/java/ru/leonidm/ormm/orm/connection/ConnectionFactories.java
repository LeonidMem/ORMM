package ru.leonidm.ormm.orm.connection;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMSettings;

import java.util.function.BiFunction;

public final class ConnectionFactories {

    private static final BiFunction<ORMDriver, ORMSettings, ConnectionFactory> FACTORY_INITIALIZER;

    static {
        FACTORY_INITIALIZER = (driver, ormSettings) -> {
            if (ormSettings.getConnectionPoolSize() > 1) {
                return new PoolConnectionFactory(driver, ormSettings);
            } else {
                return new StaticConnectionFactory(driver, ormSettings);
            }
        };
    }

    private ConnectionFactories() {

    }

    @NotNull
    public static ConnectionFactory create(@NotNull ORMDriver driver, @NotNull ORMSettings settings) {
        return FACTORY_INITIALIZER.apply(driver, settings);
    }
}
