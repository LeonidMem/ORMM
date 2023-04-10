package ru.leonidm.ormm.orm.resolvers.builtin;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.resolvers.CannotResolveException;
import ru.leonidm.ormm.orm.resolvers.DatabaseResolver;
import ru.leonidm.ormm.utils.ClassUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EnumResolver implements DatabaseResolver {

    private static final Map<Class<? extends Enum>, Enum<?>[]> CACHE = new ConcurrentHashMap<>();

    @Override
    public boolean supportsToType(@NotNull ORMColumn<?, ?> column, @NotNull Object fieldObject) {
        Class<?> databaseClass = column.getDatabaseClass();
        return (ClassUtils.isInteger(databaseClass) || databaseClass == String.class) && Enum.class.isAssignableFrom(column.getFieldClass());
    }

    @Override
    public Object resolveToDatabase(@NotNull ORMColumn<?, ?> column, @NotNull Object fieldObject) throws Exception {
        if (!(fieldObject instanceof Enum<?> enumeration)) {
            throw new CannotResolveException();
        }

        Class<?> databaseClass = column.getDatabaseClass();
        if (databaseClass == String.class) {
            return enumeration.name();
        } else if (ClassUtils.isInteger(databaseClass)) {
            return enumeration.ordinal();
        } else {
            throw new CannotResolveException();
        }
    }

    @Override
    public boolean supportsFromType(@NotNull ORMColumn<?, ?> column, @NotNull Object databaseObject) {
        Class<?> objectClass = databaseObject.getClass();
        return (objectClass == Integer.class || objectClass == String.class) && Enum.class.isAssignableFrom(column.getFieldClass());
    }

    @Override
    public Object resolveFromDatabase(@NotNull ORMColumn<?, ?> column, @NotNull Object databaseObject) throws Exception {
        Class<? extends Enum> enumClass = column.getFieldClass().asSubclass(Enum.class);

        Class<?> databaseClass = column.getDatabaseClass();
        if (databaseClass == String.class) {
            return Enum.valueOf(enumClass, (String) databaseObject);
        } else if (databaseClass == int.class) {
            return getEnums(enumClass)[(Integer) databaseObject];
        } else {
            throw new CannotResolveException();
        }
    }

    private Enum<?>[] getEnums(@NotNull Class<? extends Enum> enumClass) {
        return CACHE.computeIfAbsent(enumClass, (k) -> {
            try {
                Method method = enumClass.getMethod("values");
                method.setAccessible(true);
                return (Enum<?>[]) method.invoke(null);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
