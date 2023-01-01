package ru.leonidm.ormm.orm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.ForeignKey;
import ru.leonidm.ormm.annotations.PrimaryKey;
import ru.leonidm.ormm.orm.clauses.Where;
import ru.leonidm.ormm.orm.general.SQLType;
import ru.leonidm.ormm.utils.ArrayConverter;
import ru.leonidm.ormm.utils.ClassUtils;
import ru.leonidm.ormm.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.Objects;

public final class ORMColumn<T, F> {

    @NotNull
    public static <T> ORMColumn<T, ?> of(@NotNull ORMTable<T> table, @NotNull Field field) {
        field.setAccessible(true);

        Column column = ReflectionUtils.getAnnotation(field, Column.class);
        if (column == null) {
            throw new IllegalArgumentException("Can't register field without @Column annotation as the column!");
        }

        Class<?> fieldClass = field.getType();
        // TODO: probably push databaseClass to the meta
        Class<?> databaseClass;
        if (column.databaseClass() == Void.class) {
            databaseClass = fieldClass;
            // TODO: check for allowed classes
        } else {
            databaseClass = column.databaseClass();
        }

        String name;
        if (column.name().isBlank()) {
            name = field.getName().toLowerCase();
        } else {
            name = column.name().toLowerCase();
        }

        PrimaryKey primaryKey = ReflectionUtils.getAnnotation(field, PrimaryKey.class);
        ForeignKey foreignKey = ReflectionUtils.getAnnotation(field, ForeignKey.class);

        if (primaryKey != null) {
            if (foreignKey != null) {
                throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                        " Column can't be foreign and primary key at the same time!");
            }

            if (primaryKey.autoIncrement() && !ClassUtils.isInteger(databaseClass) && !ClassUtils.isLong(databaseClass)) {
                throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                        " AutoIncrement column's field class must be integer or long!");
            }
        }

        ORMColumnMeta meta = new ORMColumnMeta(column, primaryKey, foreignKey);

        ORMColumn<?, ?> joinColumn;
        if (foreignKey != null) {
            if (foreignKey.table().isBlank() || foreignKey.key().isBlank()) {
                throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                        " At least one of the parameters of @InnerJoin is blank!");
            }

            if (foreignKey.table().equals(table.getName())) {
                throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                        " Column can't be foreign key for the same table!");
            }

            ORMTable<?> joinTable = table.getDatabase().getTable(foreignKey.table());
            if (joinTable == null) {
                throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                        " Can't find foreign table \"" + foreignKey.table() + "\"!");
            }

            joinColumn = joinTable.getColumn(foreignKey.key());
            if (joinColumn == null) {
                throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                        " Can't find foreign column \"" + foreignKey.key() + "\" in \"" + foreignKey.table() +
                        "\" table!");
            }

            if (!joinColumn.getMeta().primaryKey()) {
                throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                        " Foreign column \"" + foreignKey.key() + "\" in \"" + foreignKey.table() +
                        "\" isn't a key!");
            }

            if (!ClassUtils.areTheSame(databaseClass, joinColumn.fieldClass)) {
                throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                        " Database class must be the same as the foreign column's class \"" +
                        joinColumn.fieldClass + "!");
            }
        } else {
            joinColumn = null;
        }

        if (column.loadFunction().isBlank() && column.saveFunction().isBlank()) {
            return new ORMColumn<>(table, name, meta, joinColumn, field, fieldClass, databaseClass,
                    null, null);
        }

        try {
            // TODO: catch NPE
            // TODO: handle <init>
            /* => Load function <= */
            int index = column.loadFunction().lastIndexOf('.');
            if (index == -1) {
                throw new IllegalArgumentException("Can't find function \"" + column.loadFunction() + "\"!");
            }

            Class<?> clazz = ReflectionUtils.getClass(column.loadFunction().substring(0, index));
            if (clazz == null) {
                throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                        " Class of the function \"" + column.loadFunction() + "\" can't be found!");
            }

            Method loadFunction = ReflectionUtils.getDeclaredMethod(clazz, column.loadFunction().substring(index + 1),
                    databaseClass);
            if (loadFunction == null) {
                loadFunction = ReflectionUtils.getDeclaredMethod(clazz, column.loadFunction().substring(index + 1));

                if (loadFunction == null) {
                    throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                            " Function \"" + column.loadFunction() + "\" can't be found! Probably, it exists, but " +
                            " it doesn't take only one argument if it's static or zero if not or it's class is not \"" +
                            fieldClass + "\"");
                }

                if (Modifier.isStatic(loadFunction.getModifiers())) {
                    throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                            " Function \"" + column.loadFunction() + "\" is found, but it takes zero arguments " +
                            " and it's static!");
                }
            }

            loadFunction.setAccessible(true);

            if (!fieldClass.isAssignableFrom(loadFunction.getReturnType())) {
                throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                        " Function \"" + column.loadFunction() + "\" must return \"" + fieldClass + "\"!");
            }

            /* => Save function <= */
            index = column.saveFunction().lastIndexOf('.');
            if (index == -1) {
                throw new IllegalArgumentException("Can't find function \"" + column.saveFunction() + "\"!");
            }

            clazz = ReflectionUtils.getClass(column.saveFunction().substring(0, index));
            if (clazz == null) {
                throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                        " Class of the function \"" + column.loadFunction() + "\" can't be found!");
            }

            Method saveFunction = ReflectionUtils.getDeclaredMethod(clazz, column.saveFunction().substring(index + 1),
                    fieldClass);
            if (saveFunction == null) {
                saveFunction = ReflectionUtils.getDeclaredMethod(clazz, column.saveFunction().substring(index + 1));

                if (saveFunction == null) {
                    throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                            " Function \"" + column.saveFunction() + "\" can't be found! Probably, it exists, but " +
                            " it doesn't take only one argument if it's static or zero if not or it's class is not \"" +
                            fieldClass + "\"");
                }

                if (Modifier.isStatic(saveFunction.getModifiers())) {
                    throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                            " Function \"" + column.saveFunction() + "\" is found, but it takes zero arguments " +
                            " and it's static!");
                }
            }

            saveFunction.setAccessible(true);

            if (!databaseClass.isAssignableFrom(saveFunction.getReturnType())) {
                throw new IllegalArgumentException(getColumnIdentifier(table, name) +
                        " Function \"" + column.loadFunction() + "\" must return \"" + databaseClass + "\"!");
            }

            return new ORMColumn<>(table, name, meta, joinColumn, field, fieldClass,
                    databaseClass, loadFunction, saveFunction);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: choose correct exception
            throw new IllegalStateException("Something went wrong!");
        }
    }

    @NotNull
    private static String getColumnIdentifier(@NotNull ORMTable<?> table, @NotNull String name) {
        return "[Column \"" + name + "\" | Table \"" + table.getName() + "\"]";
    }

    private final ORMTable<T> table;
    private final String name;
    private final ORMColumnMeta meta;
    private final ORMColumn<?, ?> joinColumn;
    private final SQLType sqlType;
    private final Field field;
    private final Class<F> fieldClass;
    private final Class<?> databaseClass;
    private final Method loadFunction;
    private final Method saveFunction;

    private ORMColumn(@NotNull ORMTable<T> table, @NotNull String name,
                      @NotNull ORMColumnMeta meta, @Nullable ORMColumn<?, ?> joinColumn,
                      @NotNull Field field, @NotNull Class<F> fieldClass,
                      @NotNull Class<?> databaseClass, @Nullable Method loadFunction,
                      @Nullable Method saveFunction) {

        if (meta.foreignKey() == (joinColumn == null)) {
            throw new IllegalArgumentException("ForeignKey and JoinColumn must be both null or not null!");
        }

        // TODO: validate if joinColumn is valid for given foreignKey

        this.table = table;
        this.name = name;
        this.meta = meta;
        this.joinColumn = joinColumn;
        this.field = field;
        this.fieldClass = fieldClass;
        this.databaseClass = databaseClass;
        this.loadFunction = loadFunction;
        this.saveFunction = saveFunction;
        this.sqlType = SQLType.of(this);
        if (sqlType == null) {
            throw new IllegalArgumentException(getIdentifier() + " Can't get SQL type of this column!");
        }
    }

    @NotNull
    public ORMTable<T> getTable() {
        return table;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public ORMColumnMeta getMeta() {
        return this.meta;
    }

    @NotNull
    public SQLType getSQLType() {
        return sqlType;
    }

    @NotNull
    public Class<F> getFieldClass() {
        return fieldClass;
    }

    @NotNull
    public Class<?> getDatabaseClass() {
        return databaseClass;
    }

    @NotNull
    public F get(@NotNull ResultSet result) throws Exception {
        Object object = result.getObject(this.meta.name(), this.databaseClass);

        if (!this.fieldClass.isAssignableFrom(object.getClass())) {
            if (this.loadFunction == null) {
                throw new IllegalArgumentException("Wrong object provided from the database!");
            }

            object = this.loadFunction.invoke(null, object);
        }

        return this.fieldClass.cast(object);
    }

    // TODO: probably change parameter back to @NotNull T t
    @Nullable
    public F getValue(@NotNull Object object) {
        if (!object.getClass().equals(this.table.getOriginalClass())) {
            throw new IllegalArgumentException("Wrong object \"" + object + "\" provided! It's class must be \"" +
                    this.table.getOriginalClass() + "\"");
        }

        try {
            return (F) this.field.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    public String getIdentifier() {
        return getColumnIdentifier(table, name);
    }

    public void setValue(@NotNull T t, @Nullable Object object) {
        try {
            Class<?> objectClass = object != null ? object.getClass() : null;

            // TODO: cast from Integer to Long

            if (this.fieldClass == boolean.class && (objectClass == null || objectClass == Boolean.class)) {
                this.field.setBoolean(t, (boolean) (object != null ? object : false));
            } else if (this.fieldClass == byte.class && (objectClass == null || objectClass == Byte.class)) {
                this.field.setByte(t, (byte) (object != null ? object : 0));
            } else if (this.fieldClass == short.class && (objectClass == null || objectClass == Short.class)) {
                this.field.setShort(t, (short) (object != null ? object : 0));
            } else if (this.fieldClass == int.class && (objectClass == null || objectClass == Integer.class)) {
                this.field.setInt(t, (int) (object != null ? object : 0));
            } else if (this.fieldClass == long.class && (objectClass == null || objectClass == Long.class
                    || objectClass == Integer.class)) {
                this.field.setLong(t, (long) (object != null ? object : 0L));
            } else if (this.fieldClass == float.class && (objectClass == null || objectClass == Float.class)) {
                this.field.setFloat(t, (float) (object != null ? object : 0F));
            } else if (this.fieldClass == double.class && (objectClass == null || objectClass == Double.class
                    || objectClass == Float.class)) {
                this.field.setDouble(t, (double) (object != null ? object : 0D));
            } else if (this.fieldClass == char.class && (objectClass == null || objectClass == Character.class)) {
                this.field.setChar(t, (char) (object != null ? object : 0));
            } else {
                if (objectClass != null && !this.fieldClass.isAssignableFrom(objectClass)) {
                    throw new IllegalArgumentException("Given object \"" + object + "\" has wrong class \"" +
                            objectClass + "\" (must be " + this.fieldClass + ")!");
                }

                this.field.set(t, object);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public Object toDatabaseObject(@Nullable Object object) {
        if (object == null) {
            return null;
        }

        if (this.meta.foreignKey()) {
            return this.joinColumn.getValue(object);
        }

        Class<?> objectClass = object.getClass();

        if (ClassUtils.areTheSame(this.databaseClass, objectClass)) {
            return object;
        }

        // TODO: add support for bytes and integers
        if (object instanceof Integer obj && ClassUtils.isLong(this.databaseClass)) {
            return (long) (obj);
        }
        if (object instanceof Float obj && ClassUtils.isDouble(this.databaseClass)) {
            return (double) (obj);
        }

        if (this.fieldClass.isAssignableFrom(objectClass) || ClassUtils.areTheSame(this.fieldClass, objectClass)) {
            try {

                if (Modifier.isStatic(this.saveFunction.getModifiers())) {
                    return this.saveFunction.invoke(null, object);
                } else {
                    return this.saveFunction.invoke(object);
                }

            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }

        throw new IllegalArgumentException(getIdentifier() +
                " Object \"" + object + "\" can't be converted to the database format!");
    }

    @Nullable
    public Object toFieldObject(@Nullable Object object) {
        if (object == null) {
            return null;
        }

        if (this.meta.foreignKey()) {
            if (ClassUtils.areTheSame(this.fieldClass, object.getClass())) {
                return object;
            }

            if (ClassUtils.areTheSame(this.joinColumn.databaseClass, object.getClass())) {
                return this.joinColumn.table.selectQuery()
                        .where(Where.compare(this.joinColumn.name, "=", object))
                        .single()
                        .waitQueue();
            }

            throw new IllegalArgumentException(getIdentifier() +
                    " Object \"" + object + "\" can't be converted from the database format!");
        }

        Class<?> objectClass = object.getClass();

        if (ClassUtils.areTheSame(this.fieldClass, objectClass)) {
            return object;
        }

        if (object instanceof Integer obj && ClassUtils.isLong(this.databaseClass)) {
            return (long) (obj);
        }
        if (object instanceof Float obj && ClassUtils.isDouble(this.databaseClass)) {
            return (double) (obj);
        }

        if (this.databaseClass.isAssignableFrom(objectClass) || ClassUtils.areTheSame(this.databaseClass, objectClass)) {
            try {

                if (Modifier.isStatic(this.loadFunction.getModifiers())) {
                    return this.loadFunction.invoke(null, object);
                } else {
                    return this.loadFunction.invoke(object);
                }

            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }

        if (object instanceof byte[] bytes) {
            if (this.fieldClass == boolean[].class) {
                return ArrayConverter.toBooleans(bytes);
            }
            if (this.fieldClass == Boolean[].class) {
                return ArrayConverter.toBoxed(ArrayConverter.toBooleans(bytes));
            }
            if (this.fieldClass == byte[].class) {
                return bytes;
            }
            if (this.fieldClass == Byte[].class) {
                return ArrayConverter.toBoxed(bytes);
            }
            if (this.fieldClass == short[].class) {
                return ArrayConverter.toShorts(bytes);
            }
            if (this.fieldClass == Short[].class) {
                return ArrayConverter.toBoxed(ArrayConverter.toShorts(bytes));
            }
            if (this.fieldClass == int[].class) {
                return ArrayConverter.toInts(bytes);
            }
            if (this.fieldClass == Integer[].class) {
                return ArrayConverter.toBoxed(ArrayConverter.toInts(bytes));
            }
            if (this.fieldClass == long[].class) {
                return ArrayConverter.toLongs(bytes);
            }
            if (this.fieldClass == Long[].class) {
                return ArrayConverter.toBoxed(ArrayConverter.toLongs(bytes));
            }
            if (this.fieldClass == float[].class) {
                return ArrayConverter.toFloats(bytes);
            }
            if (this.fieldClass == Float[].class) {
                return ArrayConverter.toBoxed(ArrayConverter.toFloats(bytes));
            }
            if (this.fieldClass == double[].class) {
                return ArrayConverter.toDoubles(bytes);
            }
            if (this.fieldClass == Double[].class) {
                return ArrayConverter.toBoxed(ArrayConverter.toDoubles(bytes));
            }
            if (this.fieldClass == char[].class) {
                return ArrayConverter.toChars(bytes);
            }
            if (this.fieldClass == Character[].class) {
                return ArrayConverter.toBoxed(ArrayConverter.toChars(bytes));
            }
        }

        throw new IllegalArgumentException(getIdentifier() +
                " Object \"" + object + "\" can't be converted from the database format!");
    }

    @Override
    public String toString() {
        return "ORMColumn{" +
                "name='" + name + '\'' +
                ", column=" + meta +
                ", fieldClass=" + fieldClass +
                ", originalClass=" + databaseClass +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ORMColumn<?, ?> ormColumn = (ORMColumn<?, ?>) o;
        return this.table.equals(ormColumn.table) && this.name.equals(ormColumn.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.table, this.name);
    }
}
