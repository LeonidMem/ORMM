package ru.leonidm.ormm.orm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.commons.collections.Pair;
import ru.leonidm.ormm.annotations.CompositeIndex;
import ru.leonidm.ormm.orm.connection.ConnectionFactories;
import ru.leonidm.ormm.orm.connection.ConnectionFactory;
import ru.leonidm.ormm.orm.connection.OrmConnection;
import ru.leonidm.ormm.orm.general.ColumnData;
import ru.leonidm.ormm.orm.general.SQLType;
import ru.leonidm.ormm.orm.queries.CreateTableQuery;
import ru.leonidm.ormm.orm.queries.DeleteQuery;
import ru.leonidm.ormm.orm.queries.columns.AddColumnsQuery;
import ru.leonidm.ormm.orm.queries.columns.DropColumnsQuery;
import ru.leonidm.ormm.orm.queries.columns.SelectColumnsQuery;
import ru.leonidm.ormm.orm.queries.indexes.CreateIndexesQuery;
import ru.leonidm.ormm.orm.queries.insert.InsertObjectQuery;
import ru.leonidm.ormm.orm.queries.insert.InsertQuery;
import ru.leonidm.ormm.orm.queries.select.SelectQuery;
import ru.leonidm.ormm.orm.queries.update.UpdateObjectQuery;
import ru.leonidm.ormm.orm.queries.update.UpdateQuery;
import ru.leonidm.ormm.utils.QueryUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class ORMDatabase {

    private final ORMDriver driver;
    private final ConnectionFactory connectionFactory;
    private final ORMSettings ormSettings;
    private final int ormSettingsHash;
    private final Map<String, ORMTable<?>> tablesByName = new HashMap<>();
    private final Map<Class<?>, ORMTable<?>> tablesByClass = new HashMap<>();
    private final Executor executor;

    public ORMDatabase(@NotNull ORMDriver driver, @NotNull ORMSettings ormSettings) {
        this.driver = driver;

        this.connectionFactory = ConnectionFactories.create(driver, ormSettings);

        this.ormSettings = ormSettings;
        this.ormSettingsHash = Objects.hash(ormSettings.getHost(), ormSettings.getPort(), ormSettings.getDatabaseName(),
                ormSettings.getUser());
        this.executor = Executors.newFixedThreadPool(ormSettings.getThreadPoolSize());
    }

    @NotNull
    public ORMDriver getDriver() {
        return driver;
    }

    @NotNull
    public OrmConnection getConnection() throws SQLException {
        return connectionFactory.getConnection();
    }

    @NotNull
    public ORMSettings getSettings() {
        return ormSettings;
    }

    @NotNull
    public Executor getTaskExecutor() {
        return executor;
    }

    public <T> void addTable(@NotNull ORMTable<T> table) {
        ORMTable<?> checkTable = getTable(table.getEntityClass());
        if (checkTable != null) {
            throw new IllegalArgumentException("Table with class \"%s\" was already registered"
                    .formatted(table.getEntityClass()));
        }

        checkTable = getTable(QueryUtils.getTableName(table));
        if (checkTable != null) {
            throw new IllegalArgumentException("Table with name \"%s\" was already registered"
                    .formatted(QueryUtils.getTableName(table)));
        }

        CreateTableQuery<T> createTableQuery = new CreateTableQuery<>(table);
        createTableQuery.complete();

        SelectColumnsQuery<T> selectColumnsQuery = new SelectColumnsQuery<>(table);
        List<ColumnData> existingColumns = selectColumnsQuery.complete();

        List<ColumnData> columnsToDrop = new ArrayList<>();

        existingColumns.forEach(columnData -> {
            ORMColumn<T, ?> column = table.getColumn(columnData.getName());
            if (column == null) {
                columnsToDrop.add(columnData);
                return;
            }

            SQLType sqlType1 = column.getSQLType();
            SQLType sqlType2 = SQLType.of(columnData.getType());
            if (sqlType1 != sqlType2) {
                // TODO: change type of the column via ALTER TABLE
                //ALTER TABLE contacts
                //  MODIFY last_name varchar(55) NULL
                //    AFTER contact_type,
                //  MODIFY first_name varchar(30) NOT NULL;
                return;
            }
        });

        List<Pair<ORMColumn<T, ?>, ORMColumn<T, ?>>> columnsToAdd = new ArrayList<>();

        ORMColumn<T, ?>[] tableColumns = table.getColumnsStream().toArray(ORMColumn[]::new);
        for (int i = 0; i < tableColumns.length; i++) {
            ORMColumn<T, ?> column = tableColumns[i];

            if (existingColumns.stream().noneMatch(columnData -> columnData.getName().equals(column.getName()))) {
                columnsToAdd.add(new Pair<>(column, i > 0 ? tableColumns[i - 1] : null));
            }
        }

        if (!columnsToDrop.isEmpty()) {
            DropColumnsQuery<T> dropColumnsQuery = new DropColumnsQuery<>(table, columnsToDrop);
            dropColumnsQuery.complete();
        }

        if (!columnsToAdd.isEmpty()) {
            AddColumnsQuery<T> addColumnsQuery = new AddColumnsQuery<>(table, columnsToAdd);
            addColumnsQuery.complete();
        }

        // TODO: DeleteIndexQuery
        List<Pair<List<ORMColumn<T, ?>>, Boolean>> columnsIndexesToAdd = table.getColumnsStream()
                .filter(column -> column.getMeta().index() && !column.getMeta().unique()
                        && !column.getMeta().primaryKey())
                .map(column -> {
                    List<ORMColumn<T, ?>> list = new ArrayList<>();
                    list.add(column);
                    return Pair.of(list, column.getMeta().unique() || column.getMeta().primaryKey());
                })
                .collect(Collectors.toList());

        Class<T> entityClass = table.getEntityClass();
        CompositeIndex[] indexes = entityClass.getAnnotationsByType(CompositeIndex.class);
        for (CompositeIndex compositeIndex : indexes) {
            List<ORMColumn<T, ?>> list = new ArrayList<>();

            for (String columnName : compositeIndex.value()) {
                ORMColumn<T, ?> column = table.getColumn(columnName);
                if (column == null) {
                    throw new IllegalArgumentException("%s Found unknown column \"%s\" in one of the composite indexes"
                            .formatted(table.getIdentifier(), columnName));
                }

                list.add(column);
            }

            columnsIndexesToAdd.add(Pair.of(list, compositeIndex.unique()));
        }

        if (!columnsIndexesToAdd.isEmpty()) {
            CreateIndexesQuery<T> createIndexesQuery = new CreateIndexesQuery<>(table, Collections.unmodifiableList(columnsIndexesToAdd));
            createIndexesQuery.complete();
        }

        tablesByName.put(QueryUtils.getTableName(table), table);
        tablesByClass.put(table.getEntityClass(), table);
    }

    @NotNull
    public <T> ORMTable<T> addTable(@NotNull Class<T> clazz) {
        ORMTable<T> table = ORMTable.of(this, clazz);

        addTable(table);

        return table;
    }

    @Nullable
    public ORMTable<?> getTable(@NotNull String name) {
        return tablesByName.get(name);
    }

    @Nullable
    public <T> ORMTable<T> getTable(@NotNull Class<T> clazz) {
        return (ORMTable<T>) tablesByClass.get(clazz);
    }

    @NotNull
    public <T> SelectQuery<T> selectQuery(@NotNull Class<T> clazz) {
        ORMTable<T> table = getTable(clazz);
        if (table == null) {
            throw new IllegalArgumentException("Given class \"%s\" wasn't registered as table".formatted(clazz));
        }

        return new SelectQuery<>(table);
    }

    @NotNull
    public <T> InsertQuery<T> insertQuery(@NotNull Class<T> clazz) {
        ORMTable<T> table = getTable(clazz);
        if (table == null) {
            throw new IllegalArgumentException("Given class \"%s\" wasn't registered as table".formatted(clazz));
        }

        return new InsertQuery<>(table);
    }

    @NotNull
    public <T> InsertObjectQuery<T> insertQuery(@NotNull Class<T> clazz, @NotNull T object) {
        ORMTable<T> table = getTable(clazz);
        if (table == null) {
            throw new IllegalArgumentException("Given class \"%s\" wasn't registered as table".formatted(clazz));
        }

        return new InsertObjectQuery<>(table, object);
    }

    @NotNull
    public <T> UpdateQuery<T> updateQuery(@NotNull Class<T> clazz) {
        ORMTable<T> table = getTable(clazz);
        if (table == null) {
            throw new IllegalArgumentException("Given class \"%s\" wasn't registered as table".formatted(clazz));
        }

        return new UpdateQuery<>(table);
    }

    @NotNull
    public <T> UpdateObjectQuery<T> updateQuery(@NotNull Class<T> clazz, @NotNull T object) {
        ORMTable<T> table = getTable(clazz);
        if (table == null) {
            throw new IllegalArgumentException("Given class \"%s\" wasn't registered as table".formatted(clazz));
        }

        return new UpdateObjectQuery<>(table, object);
    }

    @NotNull
    public <T> DeleteQuery<T> deleteQuery(@NotNull Class<T> clazz) {
        ORMTable<T> table = getTable(clazz);
        if (table == null) {
            throw new IllegalArgumentException("Given class \"%s\" wasn't registered as table".formatted(clazz));
        }

        return new DeleteQuery<>(table);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ORMDatabase database = (ORMDatabase) o;
        return ormSettings.equals(database.ormSettings) && driver == database.driver;
    }

    @Override
    public int hashCode() {
        return Objects.hash(driver, ormSettingsHash);
    }
}
