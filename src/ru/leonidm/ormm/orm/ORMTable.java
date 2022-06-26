package ru.leonidm.ormm.orm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.Table;
import ru.leonidm.ormm.orm.queries.InsertQuery;
import ru.leonidm.ormm.orm.queries.select.SelectQuery;
import ru.leonidm.ormm.orm.queries.update.UpdateQuery;
import ru.leonidm.ormm.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

public final class ORMTable<T> {

    @NotNull
    public static <T> ORMTable<T> of(@NotNull ORMDatabase database, @NotNull Class<T> originalClass) {
        // TODO: probably cache ORMTable by database and original class

        if(Modifier.isAbstract(originalClass.getModifiers())) {
            throw new IllegalArgumentException("Can't register abstract class as the table!");
        }

        if(Modifier.isInterface(originalClass.getModifiers())) {
            throw new IllegalArgumentException("Can't register interface as the table!");
        }

        Table table = ReflectionUtils.getAnnotation(originalClass, Table.class);
        if(table == null) {
            throw new IllegalArgumentException("Can't register class without @Table annotation as the table!");
        }

        String name;
        if(table.value().isBlank()) {
            name = originalClass.getSimpleName().toLowerCase();
        }
        else {
            name = table.value().toLowerCase();
        }

        LinkedHashMap<String, ORMColumn<T, ?>> columns = new LinkedHashMap<>();

        ORMTable<T> ormTable = new ORMTable<>(database, originalClass, name, table.cacheSize(), columns);

        List<Class<?>> classes = new ArrayList<>();

        Class<?> superClass = originalClass;
        while(superClass != Object.class) {
            classes.add(superClass);
            superClass = superClass.getSuperclass();
        }

        for(int i = classes.size() - 1; i >= 0; i--) {
            for(Field field : classes.get(i).getDeclaredFields()) {
                if(ReflectionUtils.hasAnnotation(field, Column.class)) {
                    ORMColumn<T, ?> ormColumn = ORMColumn.of(ormTable, field);
                    columns.put(ormColumn.getName(), ormColumn);
                }
            }
        }

        if(columns.isEmpty()) {
            throw new IllegalArgumentException("Can't register class with zero @Column fields as the table!");
        }

        ORMColumn<T, ?>[] keyColumns = columns.values().stream()
                .filter(column -> column.getMeta().primaryKey())
                .limit(2)
                .toArray(ORMColumn[]::new);

        if(keyColumns.length == 2) {
            throw new IllegalStateException("There can be only one primary key or autoincrement column!");
        }

        if(keyColumns.length == 1) {
            ormTable.keyColumn = keyColumns[0];
        }

        return ormTable;
    }

    private final ORMDatabase database;
    private final Class<T> originalClass;
    private final String name;
    private final Map<?, T> cache;
    private final LinkedHashMap<String, ORMColumn<T, ?>> columns;
    private ORMColumn<T, ?> keyColumn;

    public ORMTable(@NotNull ORMDatabase database, @NotNull Class<T> originalClass,
                    @NotNull String name, int cacheSize,
                    @NotNull LinkedHashMap<String, ORMColumn<T, ?>> columns) {
        this.database = database;
        this.originalClass = originalClass;
        this.name = name;
        this.cache = new HashMap<>(cacheSize, 1.1f);
        this.columns = columns;
    }

    @NotNull
    public ORMDatabase getDatabase() {
        return this.database;
    }

    @NotNull
    public Class<T> getOriginalClass() {
        return originalClass;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @Nullable
    public ORMColumn<T, ?> getColumn(@NotNull String name) {
        return this.columns.get(name.toLowerCase());
    }

    @NotNull
    public Stream<ORMColumn<T, ?>> getColumnsStream() {
        return this.columns.values().stream();
    }

    @NotNull
    public List<String> getColumnsNames() {
        return new ArrayList<>(this.columns.keySet());
    }

    @Nullable
    public T objectFrom(@NotNull ResultSet resultSet) throws SQLException {
        if(resultSet.isClosed()) {
            throw new IllegalStateException("ResultSet is already closed!");
        }

        T t = ReflectionUtils.getNewInstance(this.originalClass);

        for(ORMColumn<T, ?> column : this.columns.values()) {
            column.setValue(t, column.toFieldObject(resultSet.getObject(column.getName())));
        }

        return t;
    }

    // TODO: probably rename to getPrimaryKeyColumn
    @Nullable
    public ORMColumn<T, ?> getKeyColumn() {
        return this.keyColumn;
    }

    @NotNull
    public String getIdentifier() {
        return "[Table \"" + this.name + "\"]";
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
    public UpdateQuery<T> updateQuery(@Nullable T object) {
        return new UpdateQuery<>(this, object);
    }

    @NotNull
    public UpdateQuery<T> updateQuery() {
        return this.updateQuery(null);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ORMTable<?> ormTable = (ORMTable<?>) o;
        return this.database.equals(ormTable.database) && this.name.equals(ormTable.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.database, this.name);
    }
}
