package ru.leonidm.ormm.orm.resolvers.builtin;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.utils.ArrayConverter;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.resolvers.CannotResolveException;
import ru.leonidm.ormm.orm.resolvers.DatabaseResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ArrayResolver implements DatabaseResolver {

    private final Map<Class<?>, Function<byte[], Object>> castFromFunctions = new HashMap<>();
    private final Map<Class<?>, Function<Object, byte[]>> castToFunctions = new HashMap<>();

    public ArrayResolver() {
        castFromFunctions.put(boolean[].class, ArrayConverter::toBooleans);
        castFromFunctions.put(Boolean[].class, bytes -> ArrayConverter.toBoxed(ArrayConverter.toBooleans(bytes)));
        putTo(boolean[].class, ArrayConverter::toBytes);
        putTo(Boolean[].class, ArrayConverter::toBytes);

        castFromFunctions.put(byte[].class, bytes -> bytes);
        castFromFunctions.put(Byte[].class, ArrayConverter::toBoxed);
        putTo(byte[].class, bytes -> bytes);
        putTo(Byte[].class, ArrayConverter::toBytes);

        castFromFunctions.put(char[].class, ArrayConverter::toChars);
        castFromFunctions.put(Character[].class, bytes -> ArrayConverter.toBoxed(ArrayConverter.toChars(bytes)));
        putTo(char[].class, ArrayConverter::toBytes);
        putTo(Character[].class, ArrayConverter::toBytes);

        castFromFunctions.put(short[].class, ArrayConverter::toShorts);
        castFromFunctions.put(Short[].class, bytes -> ArrayConverter.toBoxed(ArrayConverter.toShorts(bytes)));
        putTo(short[].class, ArrayConverter::toBytes);
        putTo(Short[].class, ArrayConverter::toBytes);

        castFromFunctions.put(int[].class, ArrayConverter::toInts);
        castFromFunctions.put(Integer[].class, bytes -> ArrayConverter.toBoxed(ArrayConverter.toInts(bytes)));
        putTo(int[].class, ArrayConverter::toBytes);
        putTo(Integer[].class, ArrayConverter::toBytes);

        castFromFunctions.put(long[].class, ArrayConverter::toLongs);
        castFromFunctions.put(Long[].class, bytes -> ArrayConverter.toBoxed(ArrayConverter.toLongs(bytes)));
        putTo(long[].class, ArrayConverter::toBytes);
        putTo(Long[].class, ArrayConverter::toBytes);

        castFromFunctions.put(float[].class, ArrayConverter::toFloats);
        castFromFunctions.put(Float[].class, bytes -> ArrayConverter.toBoxed(ArrayConverter.toFloats(bytes)));
        putTo(float[].class, ArrayConverter::toBytes);
        putTo(Float[].class, ArrayConverter::toBytes);

        castFromFunctions.put(double[].class, ArrayConverter::toDoubles);
        castFromFunctions.put(Double[].class, bytes -> ArrayConverter.toBoxed(ArrayConverter.toDoubles(bytes)));
        putTo(double[].class, ArrayConverter::toBytes);
        putTo(Double[].class, ArrayConverter::toBytes);
    }

    private <T> void putTo(@NotNull Class<T> clazz, @NotNull Function<T, byte[]> function) {
        castToFunctions.put(clazz, (Function<Object, byte[]>) function);
    }

    @Override
    public boolean supportsToType(@NotNull ORMColumn<?, ?> column, @NotNull Object fieldObject) {
        return column.getDatabaseClass() == byte[].class && castToFunctions.containsKey(fieldObject.getClass());
    }

    @Override
    public Object resolveToDatabase(@NotNull ORMColumn<?, ?> column, @NotNull Object fieldObject) throws Exception {
        Function<Object, byte[]> function = castToFunctions.get(fieldObject.getClass());
        if (function != null) {
            return function.apply(fieldObject);
        }

        throw new CannotResolveException();
    }

    @Override
    public boolean supportsFromType(@NotNull ORMColumn<?, ?> column, @NotNull Object databaseObject) {
        return databaseObject.getClass() == byte[].class && castFromFunctions.containsKey(column.getFieldClass());
    }

    @Override
    public Object resolveFromDatabase(@NotNull ORMColumn<?, ?> column, @NotNull Object databaseObject) throws Exception {
        if (databaseObject instanceof byte[] bytes) {
            return castFromFunctions.get(column.getFieldClass()).apply(bytes);
        } else {
            throw new CannotResolveException();
        }
    }
}
