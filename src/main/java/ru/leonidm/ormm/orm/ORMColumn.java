package ru.leonidm.ormm.orm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.commons.text.TextCase;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.ForeignKey;
import ru.leonidm.ormm.annotations.PrimaryKey;
import ru.leonidm.ormm.orm.clauses.Where;
import ru.leonidm.ormm.orm.general.SQLType;
import ru.leonidm.ormm.orm.resolvers.CannotResolveException;
import ru.leonidm.ormm.orm.resolvers.ORMResolverRegistry;
import ru.leonidm.ormm.utils.ClassUtils;
import ru.leonidm.ormm.utils.QueryUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public final class ORMColumn<T, F> {

    private final ORMTable<T> table;

    private final String name;
    private final ORMColumnMeta meta;
    private final ORMColumn<?, ?> joinColumn;
    private final SQLType sqlType;
    private final Field field;
    private final Class<F> fieldClass;
    private final Class<?> databaseClass;

    private ORMColumn(@NotNull ORMTable<T> table, @NotNull String name,
                      @NotNull ORMColumnMeta meta, @Nullable ORMColumn<?, ?> joinColumn,
                      @NotNull Field field, @NotNull Class<F> fieldClass,
                      @NotNull Class<?> databaseClass) {

        if (meta.foreignKey() == (joinColumn == null)) {
            throw new IllegalArgumentException("ForeignKey and JoinColumn must be both null or not null");
        }

        // TODO: validate if joinColumn is valid for given foreignKey

        this.table = table;
        this.name = name;
        this.meta = meta;
        this.joinColumn = joinColumn;
        this.field = field;
        this.fieldClass = fieldClass;
        this.databaseClass = databaseClass;
        this.sqlType = SQLType.of(this);
        if (this.sqlType == null) {
            throw new IllegalArgumentException("%s Can't get SQL type of this column".formatted(getIdentifier()));
        }
    }

    @NotNull
    @SuppressWarnings("java:S3011")
    public static <T> ORMColumn<T, ?> of(@NotNull ORMTable<T> table, @NotNull Field field) {
        // Подавление варнинга из-за рефлексии
        field.setAccessible(true);

        Column column = field.getAnnotation(Column.class);
        if (column == null) {
            throw new IllegalArgumentException("Can't register field without @Column annotation as the column");
        }

        Class<?> fieldClass = field.getType();
        Class<?> databaseClass;
        if (column.databaseClass() == Void.class) {
            if (Enum.class.isAssignableFrom(fieldClass) || fieldClass == UUID.class) {
                databaseClass = String.class;
            } else {
                databaseClass = fieldClass;
            }

            // TODO: check for allowed classes
        } else {
            databaseClass = column.databaseClass();
        }

        String name;
        if (column.name().isBlank()) {
            String finalName = field.getName();

            TextCase textCase = TextCase.from(finalName);
            if (textCase != null) {
                finalName = TextCase.SNAKE.wordsTo(textCase.wordsFrom(finalName));
            }

            name = finalName.toLowerCase();
        } else {
            name = column.name().toLowerCase();
        }

        PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
        ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);

        if (primaryKey != null) {
            if (foreignKey != null) {
                throw new IllegalArgumentException("%s Column can't be foreign and primary key at the same time"
                        .formatted(getColumnIdentifier(table, name)));
            }

            if (primaryKey.autoIncrement() && !ClassUtils.isInteger(databaseClass) && !ClassUtils.isLong(databaseClass)) {
                throw new IllegalArgumentException("%s AutoIncrement column's field class must be integer or long"
                        .formatted(getColumnIdentifier(table, name)));
            }
        }

        ORMColumnMeta meta = new ORMColumnMeta(column, primaryKey, foreignKey);

        ORMColumn<?, ?> joinColumn;
        if (foreignKey != null) {
            if (foreignKey.table().isBlank() || foreignKey.key().isBlank()) {
                throw new IllegalArgumentException("%s At least one of the parameters of @InnerJoin is blank"
                        .formatted(getColumnIdentifier(table, name)));
            }

            if (foreignKey.table().equals(QueryUtils.getTableName(table))) {
                throw new IllegalArgumentException("%s Column can't be foreign key for the same table"
                        .formatted(getColumnIdentifier(table, name)));
            }

            ORMTable<?> joinTable = table.getDatabase().getTable(foreignKey.table());
            if (joinTable == null) {
                throw new IllegalArgumentException("%s Can't find foreign table \"%s\""
                        .formatted(getColumnIdentifier(table, name), foreignKey.table()));
            }

            joinColumn = joinTable.getColumn(foreignKey.key());
            if (joinColumn == null) {
                throw new IllegalArgumentException("%s Can't find foreign column \"%s\" in \"%s\" table"
                        .formatted(getColumnIdentifier(table, name), foreignKey.key(), foreignKey.table()));
            }

            if (!joinColumn.getMeta().primaryKey()) {
                throw new IllegalArgumentException("%s Foreign column \"%s\" in \"%s\" isn't a key"
                        .formatted(getColumnIdentifier(table, name), foreignKey.key(), foreignKey.table()));
            }

            if (!ClassUtils.areTheSame(databaseClass, joinColumn.fieldClass)) {
                throw new IllegalArgumentException("%s Database class must be the same as the foreign column's class \"%s\""
                        .formatted(getColumnIdentifier(table, name), joinColumn.fieldClass));
            }
        } else {
            joinColumn = null;
        }

        return new ORMColumn<>(table, name, meta, joinColumn, field, fieldClass, databaseClass);
    }

    @NotNull
    private static String getColumnIdentifier(@NotNull ORMTable<?> table, @NotNull String name) {
        return "[Column \"%s\" | Table \"%s\"]".formatted(name, QueryUtils.getTableName(table));
    }

    @NotNull
    public ORMTable<T> getTable() {
        return table;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public ORMColumnMeta getMeta() {
        return meta;
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

    @Nullable
    public F get(@NotNull ResultSet result) throws SQLException {
        Object object = result.getObject(meta.name(), databaseClass);

        if (!fieldClass.isAssignableFrom(object.getClass())) {
            try {
                object = ORMResolverRegistry.resolveFromDatabase(this, object);
            } catch (CannotResolveException e) {
                throw new IllegalStateException(e);
            }
        }

        return fieldClass.cast(object);
    }

    @Nullable
    public F getValue(@NotNull Object object) {
        if (!object.getClass().equals(table.getOriginalClass())) {
            throw new IllegalArgumentException("Wrong object \"%s\" provided! It's class must be \"%s\""
                    .formatted(object, table.getOriginalClass()));
        }

        try {
            return (F) field.get(object);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @NotNull
    public String getIdentifier() {
        return getColumnIdentifier(table, name);
    }

    @SuppressWarnings("java:S3011")
    public void setValue(@NotNull T t, @Nullable Object object) {
        try {
            Class<?> objectClass = object != null ? object.getClass() : null;

            // Подавление многочисленных варнингов на рефлексию
            if (fieldClass == boolean.class && (objectClass == null || objectClass == Boolean.class)) {
                field.setBoolean(t, (boolean) (object != null ? object : false));
            } else if (fieldClass == byte.class && (objectClass == null || objectClass == Byte.class)) {
                field.setByte(t, (byte) (object != null ? object : (byte) 0));
            } else if (fieldClass == char.class && (objectClass == null || objectClass == Character.class)) {
                field.setChar(t, (char) (object != null ? object : (char) 0));
            } else if (fieldClass == short.class && (objectClass == null || objectClass == Short.class)) {
                field.setShort(t, (short) (object != null ? object : (short) 0));
            } else if (fieldClass == int.class && (objectClass == null || objectClass == Integer.class)) {
                field.setInt(t, (int) (object != null ? object : 0));
            } else if (fieldClass == long.class && (objectClass == null || objectClass == Long.class
                    || objectClass == Integer.class)) {
                field.setLong(t, (long) (object != null ? object : 0L));
            } else if (fieldClass == float.class && (objectClass == null || objectClass == Float.class)) {
                field.setFloat(t, (float) (object != null ? object : 0F));
            } else if (fieldClass == double.class && (objectClass == null || objectClass == Double.class
                    || objectClass == Float.class)) {
                field.setDouble(t, (double) (object != null ? object : 0D));
            } else {
                if (objectClass != null && !fieldClass.isAssignableFrom(objectClass)) {
                    throw new IllegalArgumentException("Given object \"%s\" has wrong class \"%s\" (must be %s)"
                            .formatted(object, objectClass, fieldClass));
                }

                field.set(t, object);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nullable
    public Object toDatabaseObject(@Nullable Object object) {
        if (object == null) {
            return null;
        }

        if (meta.foreignKey()) {
            return joinColumn.getValue(object);
        }

        Class<?> objectClass = object.getClass();

        if (ClassUtils.areTheSame(databaseClass, objectClass)) {
            return object;
        }

        if (object instanceof Integer obj && ClassUtils.isLong(databaseClass)) {
            return (long) (obj);
        }
        if (object instanceof Float obj && ClassUtils.isDouble(databaseClass)) {
            return (double) (obj);
        }

        try {
            return ORMResolverRegistry.resolveToDatabase(this, object);
        } catch (CannotResolveException e) {
            throw new IllegalArgumentException(getIdentifier() +
                    " Object \"%s\" can't be converted to the database format".formatted(object), e);
        }
    }

    @Nullable
    public Object toFieldObject(@Nullable Object object) {
        if (object == null) {
            return null;
        }

        if (meta.foreignKey()) {
            if (ClassUtils.areTheSame(fieldClass, object.getClass()) || fieldClass.isAssignableFrom(object.getClass())) {
                return object;
            }

            if (ClassUtils.areTheSame(joinColumn.databaseClass, object.getClass())) {
                return joinColumn.table.selectQuery()
                        .where(Where.compare(joinColumn.name, "=", object))
                        .single()
                        .complete();
            }

            throw new IllegalArgumentException(getIdentifier() +
                    " Object \"%s\" can't be converted from the database format".formatted(object));
        }

        Class<?> objectClass = object.getClass();

        if (ClassUtils.areTheSame(fieldClass, objectClass) || fieldClass.isAssignableFrom(objectClass)) {
            return object;
        }

        if (object instanceof Integer obj) {
            if (ClassUtils.isByte(databaseClass)) {
                return (byte) (int) (obj);
            } else if (ClassUtils.isChar(databaseClass)) {
                return (char) (int) (obj);
            } else if (ClassUtils.isShort(databaseClass)) {
                return (short) (int) (obj);
            } else if (ClassUtils.isLong(databaseClass)) {
                return (long) (int) (obj);
            }
        }

        if (object instanceof Float obj && ClassUtils.isDouble(databaseClass)) {
            return (double) (float) (obj);
        }

        if (object instanceof Double obj && ClassUtils.isFloat(databaseClass)) {
            return (float) (double) (obj);
        }

        try {
            return ORMResolverRegistry.resolveFromDatabase(this, object);
        } catch (Exception e) {
            throw new IllegalArgumentException(getIdentifier() +
                    " Object \"%s\" can't be converted from the database format".formatted(object), e);
        }
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
        return table.equals(ormColumn.table) && name.equals(ormColumn.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, name);
    }
}
