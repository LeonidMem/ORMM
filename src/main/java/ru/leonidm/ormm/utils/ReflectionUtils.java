package ru.leonidm.ormm.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    @Nullable
    public static Class<?> getClass(@NotNull String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Nullable
    public static Method getDeclaredMethod(@NotNull Class<?> clazz, @NotNull String name,
                                           @NotNull Class<?> @NotNull ... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Nullable
    public static <T> Constructor<T> getEmptyConstructor(Class<T> clazz) {
        try {
            return clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @NotNull
    public static <T> T getNewInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = Objects.requireNonNull(getEmptyConstructor(clazz));
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Class %s must have empty public constructor!".formatted(clazz.getName()), e);
        }
    }
}
