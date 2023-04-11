package ru.leonidm.ormm.orm.resolvers;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;

public interface DatabaseResolver {

    /**
     * @return true if object from field can be converted to database format
     */
    boolean supportsToType(@NotNull ORMColumn<?, ?> column, @NotNull Object fieldObject);

    Object resolveToDatabase(@NotNull ORMColumn<?, ?> column, @NotNull Object fieldObject) throws Exception;

    /**
     * @return true if object from database can be converted to field instance
     */
    boolean supportsFromType(@NotNull ORMColumn<?, ?> column, @NotNull Object databaseObject);

    Object resolveFromDatabase(@NotNull ORMColumn<?, ?> column, @NotNull Object databaseObject) throws Exception;

}
