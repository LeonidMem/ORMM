package ru.leonidm.ormm.orm.resolvers;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;

/**
 * При реализации этого интерфейса или наследования от класса, где он реализован,
 * ORMM попытается создать экземпляр этого класса, если есть публичный пустой
 * конструктор
 */
public interface DatabaseResolver {

    /**
     * @return true, если объект, взятый из поля экземпляра,
     * может быть преобразован в объект для базы данных
     */
    boolean supportsToType(@NotNull ORMColumn<?, ?> column, @NotNull Object fieldObject);

    Object resolveToDatabase(@NotNull ORMColumn<?, ?> column, @NotNull Object fieldObject) throws Exception;

    /**
     * @return true, если объект, взятый из базы данных,
     * может быть преобразован в объект для поля экземпляра
     */
    boolean supportsFromType(@NotNull ORMColumn<?, ?> column, @NotNull Object databaseObject);

    Object resolveFromDatabase(@NotNull ORMColumn<?, ?> column, @NotNull Object databaseObject) throws Exception;

}
