package ru.leonidm.ormm.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;

public final class ReflectionUtils {

    private ReflectionUtils() {}

    @Nullable
    public static <T extends Annotation> T getAnnotation(@NotNull Field field, @NotNull Class<T> annotationClass) {
        try {
            return field.getAnnotationsByType(annotationClass)[0];
        } catch(Exception e) {
            return null;
        }
    }

    public static <T extends Annotation> boolean hasAnnotation(@NotNull Field field, @NotNull Class<T> annotationClass) {
        try {
            return field.getAnnotationsByType(annotationClass).length != 0;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Nullable
    public static <T extends Annotation> T getAnnotation(@NotNull Class<?> clazz, @NotNull Class<T> annotationClass) {
        return clazz.getAnnotation(annotationClass);
    }

    @Nullable
    public static Class<?> getClass(@NotNull String name) {
        try {
            return Class.forName(name);
        } catch(ClassNotFoundException e) {
            return null;
        }
    }

    @Nullable
    public static Method[] getDeclaredMethodsByName(@NotNull Class<?> clazz, @NotNull String name) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getName().equals(name))
                .toArray(Method[]::new);
    }

    @Nullable
    public static Method[] getDeclaredMethodsByName(@NotNull Class<?> clazz, @NotNull String name,
                                                    @NotNull Predicate<Method> filterPredicate) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.getName().equals(name))
                .filter(filterPredicate)
                .toArray(Method[]::new);
    }

    @Nullable
    public static Method getDeclaredMethod(@NotNull Class<?> clazz, @NotNull String name,
                                           @NotNull Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch(NoSuchMethodException e) {
            return null;
        }
    }

    @Nullable
    public static <T> Constructor<T> getEmptyConstructor(Class<T> clazz) {
        try {
            return clazz.getConstructor();
        } catch(NoSuchMethodException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T getNewInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = getEmptyConstructor(clazz);
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch(InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }
}
