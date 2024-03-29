package ru.leonidm.ormm.utils;

import org.jetbrains.annotations.NotNull;

public final class ClassUtils {

    private ClassUtils() {
    }

    public static boolean areTheSame(@NotNull Class<?> clazz1, @NotNull Class<?> clazz2) {
        if (isBoolean(clazz1) && isBoolean(clazz2)) {
            return true;
        }
        if (isByte(clazz1) && isByte(clazz2)) {
            return true;
        }
        if (isShort(clazz1) && isShort(clazz2)) {
            return true;
        }
        if (isInteger(clazz1) && isInteger(clazz2)) {
            return true;
        }
        if (isLong(clazz1) && isLong(clazz2)) {
            return true;
        }
        if (isFloat(clazz1) && isFloat(clazz2)) {
            return true;
        }
        if (isDouble(clazz1) && isDouble(clazz2)) {
            return true;
        }
        if (isChar(clazz1) && isChar(clazz2)) {
            return true;
        }

        return clazz1 == clazz2;
    }

    public static boolean isBuiltIn(@NotNull Class<?> clazz) {
        return isBoolean(clazz) || isByte(clazz) || isShort(clazz) || isInteger(clazz) || isLong(clazz)
                || isFloat(clazz) || isDouble(clazz) || isChar(clazz) || clazz == String.class;
    }

    public static boolean isBoolean(@NotNull Class<?> clazz) {
        return clazz == boolean.class || clazz == Boolean.class;
    }

    public static boolean isByte(@NotNull Class<?> clazz) {
        return clazz == byte.class || clazz == Byte.class;
    }

    public static boolean isShort(@NotNull Class<?> clazz) {
        return clazz == short.class || clazz == Short.class;
    }

    public static boolean isInteger(@NotNull Class<?> clazz) {
        return clazz == int.class || clazz == Integer.class;
    }

    public static boolean isLong(@NotNull Class<?> clazz) {
        return clazz == long.class || clazz == Long.class;
    }

    public static boolean isFloat(@NotNull Class<?> clazz) {
        return clazz == float.class || clazz == Float.class;
    }

    public static boolean isDouble(@NotNull Class<?> clazz) {
        return clazz == double.class || clazz == Double.class;
    }

    public static boolean isChar(@NotNull Class<?> clazz) {
        return clazz == char.class || clazz == Character.class;
    }
}
