package ru.leonidm.ormm.orm.resolvers.builtin;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.utils.ArrayConverter;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.resolvers.CannotResolveException;
import ru.leonidm.ormm.orm.resolvers.DatabaseResolver;

import java.util.UUID;

public final class UUIDResolver implements DatabaseResolver {

    @Override
    public boolean supportsToType(@NotNull ORMColumn<?, ?> column, @NotNull Object fieldObject) {
        Class<?> databaseClass = column.getDatabaseClass();
        return fieldObject.getClass() == UUID.class && (databaseClass == String.class || databaseClass == byte[].class);
    }

    @Override
    public Object resolveToDatabase(@NotNull ORMColumn<?, ?> column, @NotNull Object fieldObject) throws Exception {
        if (fieldObject instanceof UUID uuid) {
            Class<?> databaseClass = column.getDatabaseClass();
            if (databaseClass == String.class) {
                return uuid.toString();
            } else if (databaseClass == byte[].class) {
                long most = uuid.getMostSignificantBits();
                long least = uuid.getLeastSignificantBits();
                return ArrayConverter.toBytes(new long[]{most, least});
            } else {
                throw new CannotResolveException();
            }
        }

        throw new CannotResolveException();
    }

    @Override
    public boolean supportsFromType(@NotNull ORMColumn<?, ?> column, @NotNull Object databaseObject) {
        Class<?> objectClass = databaseObject.getClass();
        return column.getFieldClass() == UUID.class && (objectClass == String.class || objectClass == byte[].class);
    }

    @Override
    public Object resolveFromDatabase(@NotNull ORMColumn<?, ?> column, @NotNull Object databaseObject) throws Exception {
        if (databaseObject instanceof String string) {
            return UUID.fromString(string);
        } else if (databaseObject instanceof byte[] bytes) {
            long[] longs = ArrayConverter.toLongs(bytes);
            return new UUID(longs[0], longs[1]);
        } else {
            throw new CannotResolveException();
        }
    }
}
