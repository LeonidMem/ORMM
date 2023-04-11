package ru.leonidm.ormm.orm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.annotations.Table;
import ru.leonidm.ormm.orm.queries.DeleteQuery;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.orm.queries.insert.InsertObjectQuery;
import ru.leonidm.ormm.orm.queries.insert.InsertQuery;
import ru.leonidm.ormm.orm.queries.select.SelectQuery;
import ru.leonidm.ormm.orm.queries.update.UpdateObjectQuery;
import ru.leonidm.ormm.orm.queries.update.UpdateQuery;
import ru.leonidm.ormm.utils.QueryUtils;
import ru.leonidm.ormm.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public final class ORMTable<T> {

    private static final Map<ORMDatabase, Map<Class<?>, ORMTable<?>>> TABLES_CACHE = new HashMap<>();

    @NotNull
    public static <T> ORMTable<T> of(@NotNull ORMDatabase database, @NotNull Class<T> originalClass) {
        Map<Class<?>, ORMTable<?>> databaseCache = TABLES_CACHE.get(database);
        if (databaseCache != null) {
            ORMTable<T> table = (ORMTable<T>) databaseCache.get(originalClass);
            if (table != null) {
                return table;
            }
        }

        if (Modifier.isAbstract(originalClass.getModifiers())) {
            throw new IllegalArgumentException("Can't register abstract class as the table");
        }

        if (Modifier.isInterface(originalClass.getModifiers())) {
            throw new IllegalArgumentException("Can't register interface as the table");
        }

        Table table = originalClass.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalArgumentException("Can't register class without @Table annotation as the table");
        }

        String name;
        if (table.value().isBlank()) {
            name = originalClass.getSimpleName().toLowerCase();
        } else {
            name = table.value().toLowerCase();
        }

        LinkedHashMap<String, ORMColumn<T, ?>> columns = new LinkedHashMap<>();

        ORMTable<T> ormTable = new ORMTable<>(database, originalClass, name, table, columns);

        List<Class<?>> classes = new ArrayList<>();

        Class<?> superClass = originalClass;
        while (superClass != Object.class) {
            classes.add(superClass);
            superClass = superClass.getSuperclass();
        }

        for (int i = classes.size() - 1; i >= 0; i--) {
            for (Field field : classes.get(i).getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    ORMColumn<T, ?> ormColumn = ORMColumn.of(ormTable, field);
                    columns.put(ormColumn.getName(), ormColumn);
                }
            }
        }

        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Can't register class with zero @Column fields as the table");
        }

        ORMColumn<T, ?>[] keyColumns = columns.values().stream()
                .filter(column -> column.getMeta().primaryKey())
                .limit(2)
                .toArray(ORMColumn[]::new);

        if (keyColumns.length == 2) {
            throw new IllegalStateException("There can be only one primary key or autoincrement column");
        }

        if (keyColumns.length == 1) {
            ormTable.keyColumn = keyColumns[0];
        }

        return ormTable;
    }

    private final ORMDatabase database;
    private final Class<T> originalClass;
    private final String name;
    private final Table meta;
    private final LinkedHashMap<String, ORMColumn<T, ?>> columns;
    private ORMColumn<T, ?> keyColumn;
    private final Map<?, T> cache;

    public ORMTable(@NotNull ORMDatabase database, @NotNull Class<T> originalClass,
                    @NotNull String name, @NotNull Table meta,
                    @NotNull LinkedHashMap<String, ORMColumn<T, ?>> columns) {
        this.database = database;
        this.originalClass = originalClass;
        this.name = name;
        this.meta = meta;
        this.columns = columns;
        this.cache = new HashMap<>(meta.cacheSize(), 1.1f);
    }

    @NotNull
    public ORMDatabase getDatabase() {
        return database;
    }

    @NotNull
    public Class<T> getOriginalClass() {
        return originalClass;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Table getMeta() {
        return meta;
    }

    @Nullable
    public ORMColumn<T, ?> getColumn(@NotNull String name) {
        return columns.get(name.toLowerCase());
    }

    @NotNull
    public Stream<ORMColumn<T, ?>> getColumnsStream() {
        return columns.values().stream();
    }

    @NotNull
    public Set<String> getColumnsNames() {
        return Collections.unmodifiableSet(columns.keySet());
    }

    @NotNull
    public T objectFrom(@NotNull ResultSet resultSet) throws SQLException {
        if (resultSet.isClosed()) {
            throw new IllegalStateException("ResultSet is already closed");
        }

        T t = ReflectionUtils.getNewInstance(originalClass);

        for (ORMColumn<T, ?> column : columns.values()) {
            column.setValue(t, column.toFieldObject(resultSet.getObject(QueryUtils.getColumnName(column))));
        }

        return t;
    }

    @Nullable
    public ORMColumn<T, ?> getKeyColumn() {
        return keyColumn;
    }

    @NotNull
    public String getIdentifier() {
        return "[Table \"" + name + "\"]";
    }

    @NotNull
    public SelectQuery<T> selectQuery() {
        return new SelectQuery<>(this);
    }

    @NotNull
    public InsertQuery<T> insertQuery() {
        return new InsertQuery<>(this);
    }

    @NotNull
    public InsertObjectQuery<T> insertQuery(@NotNull T object) {
        return new InsertObjectQuery<>(this, object);
    }

    @NotNull
    public UpdateQuery<T> updateQuery() {
        return new UpdateQuery<>(this);
    }

    @NotNull
    public UpdateObjectQuery<T> updateQuery(@NotNull T object) {
        return new UpdateObjectQuery<>(this, object);
    }

    @NotNull
    public DeleteQuery<T> deleteQuery() {
        return new DeleteQuery<>(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ORMTable<?> ormTable = (ORMTable<?>) o;
        return database.equals(ormTable.database) && name.equals(ormTable.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(database, name);
    }
}
