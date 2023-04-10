package ru.leonidm.ormm.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.queries.select.AbstractSelectQuery;

import java.nio.charset.StandardCharsets;

public final class FormatUtils {

    private FormatUtils() {
    }

    @NotNull
    public static StringBuilder writeColumnFullName(@NotNull ORMColumn<?, ?> column) {
        return writeColumnFullName(new StringBuilder(), column);
    }

    @NotNull
    public static StringBuilder writeColumnFullName(@NotNull StringBuilder stringBuilder,
                                                    @NotNull ORMColumn<?, ?> column) {
        return stringBuilder.append(QueryUtils.getTableName(column)).append('.').append(column.getName());
    }

    @NotNull
    public static String toStringSQLValue(@Nullable Object object) {
        if (object == null) {
            return "NULL";
        }

        if (object instanceof Byte || object instanceof Short
                || object instanceof Integer || object instanceof Long
                || object instanceof Float || object instanceof Double) {
            return object.toString();
        }

        if (object instanceof Character) {
            return Integer.toString((char) object);
        }

        if (object instanceof Boolean bool) {
            return bool ? "1" : "0";
        }

        if (object instanceof boolean[] booleans) {
            return toString(ArrayConverter.toBytes(booleans));
        }

        if (object instanceof Boolean[] booleans) {
            return toString(ArrayConverter.toBytes(booleans));
        }

        if (object instanceof byte[] bytes) {
            return toString(bytes);
        }

        if (object instanceof Byte[] bytes) {
            return toString(bytes);
        }

        if (object instanceof short[] shorts) {
            return toString(ArrayConverter.toBytes(shorts));
        }

        if (object instanceof Short[] shorts) {
            return toString(ArrayConverter.toBytes(shorts));
        }

        if (object instanceof int[] ints) {
            return toString(ArrayConverter.toBytes(ints));
        }

        if (object instanceof Integer[] ints) {
            return toString(ArrayConverter.toBytes(ints));
        }

        if (object instanceof long[] longs) {
            return toString(ArrayConverter.toBytes(longs));
        }

        if (object instanceof Long[] longs) {
            return toString(ArrayConverter.toBytes(longs));
        }

        if (object instanceof float[] floats) {
            return toString(ArrayConverter.toBytes(floats));
        }

        if (object instanceof Float[] floats) {
            return toString(ArrayConverter.toBytes(floats));
        }

        if (object instanceof double[] doubles) {
            return toString(ArrayConverter.toBytes(doubles));
        }

        if (object instanceof Double[] doubles) {
            return toString(ArrayConverter.toBytes(doubles));
        }

        if (object instanceof char[] chars) {
            return toString(ArrayConverter.toBytes(chars));
        }

        if (object instanceof Character[] chars) {
            return toString(ArrayConverter.toBytes(chars));
        }

        if (object instanceof AbstractSelectQuery<?, ?, ?> selectQuery) {
            return '(' + selectQuery.getSQLQuery() + ')';
        }

        return "\"" + object.toString().replace("\"", "\"\"") + "\"";
    }

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    public static String toString(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2 + 3];
        hexChars[0] = 'X';
        hexChars[1] = '\'';
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2 + 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 3] = HEX_ARRAY[v & 0x0F];
        }
        hexChars[hexChars.length - 1] = '\'';
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    public static String toString(Byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2 + 3];
        hexChars[0] = 'X';
        hexChars[1] = '\'';
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2 + 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 3] = HEX_ARRAY[v & 0x0F];
        }
        hexChars[hexChars.length - 1] = '\'';
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
