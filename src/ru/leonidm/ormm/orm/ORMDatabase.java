package ru.leonidm.ormm.orm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.collections.Pair;
import ru.leonidm.ormm.orm.general.ColumnData;
import ru.leonidm.ormm.orm.general.SQLType;
import ru.leonidm.ormm.orm.queries.*;
import ru.leonidm.ormm.orm.queries.columns.AddColumnsQuery;
import ru.leonidm.ormm.orm.queries.columns.DropColumnsQuery;
import ru.leonidm.ormm.orm.queries.columns.SelectColumnsQuery;
import ru.leonidm.ormm.orm.queries.indexes.CreateIndexesQuery;
import ru.leonidm.ormm.orm.queries.select.SelectQuery;
import ru.leonidm.ormm.orm.queries.update.UpdateQuery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public final class ORMDatabase {

    private final ORMDriver driver;
    private final Connection connection;
    private final int linkUserPasswordHash;
    private final Map<String, ORMTable<?>> tablesByName = new HashMap<>();
    private final Map<Class<?>, ORMTable<?>> tablesByClass = new HashMap<>();

    public ORMDatabase(@NotNull ORMDriver driver, @NotNull String link) {
        this.driver = driver;

        try {
            this.connection = DriverManager.getConnection(driver.get(ORMDriver.Key.LINK_PREFIX) + link);
        } catch(SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        this.linkUserPasswordHash = Objects.hash(link, null, null);
    }

    public ORMDatabase(@NotNull ORMDriver driver, @NotNull String link, @NotNull String user, @NotNull String password) {
        this.driver = driver;

        try {
            this.connection = DriverManager.getConnection(driver.get(ORMDriver.Key.LINK_PREFIX) + link,
                    user, password);
        } catch(SQLException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        this.linkUserPasswordHash = Objects.hash(link, user, password);
    }

    @NotNull
    public ORMDriver getDriver() {
        return this.driver;
    }

    @NotNull
    public Connection getConnection() {
        return connection;
    }

    public <T> void addTable(@NotNull ORMTable<T> table) {
        ORMTable<?> checkTable = this.getTable(table.getOriginalClass());
        if(checkTable != null) {
            throw new IllegalArgumentException("Table with class \"" + table.getOriginalClass() + "\" was already " +
                    "registered!");
        }

        checkTable = this.getTable(table.getName());
        if(checkTable != null) {
            throw new IllegalArgumentException("Table with name \"" + table.getName() + "\" was already registered!");
        }

        CreateTableQuery<T> createTableQuery = new CreateTableQuery<>(table);
        createTableQuery.waitQueue();

        SelectColumnsQuery<T> selectColumnsQuery = new SelectColumnsQuery<>(table);
        List<ColumnData> existingColumns = selectColumnsQuery.waitQueue();

        List<ColumnData> columnsToDrop = new ArrayList<>();

        existingColumns.forEach(columnData -> {
            ORMColumn<T, ?> column = table.getColumn(columnData.getName());
            if(column == null) {
                columnsToDrop.add(columnData);
                return;
            }

            SQLType sqlType1 = column.getSQLType();
            SQLType sqlType2 = SQLType.of(columnData.getType());
            if(sqlType1 != sqlType2) {
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
        for(int i = 0; i < tableColumns.length; i++) {
            ORMColumn<T, ?> column = tableColumns[i];

            if(existingColumns.stream().noneMatch(columnData -> columnData.getName().equals(column.getName()))) {
                columnsToAdd.add(new Pair<>(column, i > 0 ? tableColumns[i - 1] : null));
            }
        }

        if(!columnsToDrop.isEmpty()) {
            DropColumnsQuery<T> dropColumnsQuery = new DropColumnsQuery<>(table, columnsToDrop);
            dropColumnsQuery.waitQueue();
        }

        if(!columnsToAdd.isEmpty()) {
            AddColumnsQuery<T> addColumnsQuery = new AddColumnsQuery<>(table, columnsToAdd);
            addColumnsQuery.waitQueue();
        }

        // TODO: DeleteIndexQuery
        List<ORMColumn<T, ?>> columnsIndexesToAdd = table.getColumnsStream()
                .filter(column -> column.getMeta().index() && !column.getMeta().unique()
                        && !column.getMeta().primaryKey())
                .toList();

        if(!columnsIndexesToAdd.isEmpty()) {
            CreateIndexesQuery<T> createIndexesQuery = new CreateIndexesQuery<>(table, columnsIndexesToAdd);
            createIndexesQuery.waitQueue();
        }

        this.tablesByName.put(table.getName(), table);
        this.tablesByClass.put(table.getOriginalClass(), table);
    }

    public void addTable(@NotNull Class<?> clazz) {
        ORMTable<?> table = ORMTable.of(this, clazz);

        this.addTable(table);
    }

    @Nullable
    public ORMTable<?> getTable(@NotNull String name) {
        return this.tablesByName.get(name);
    }

    @Nullable
    public <T> ORMTable<T> getTable(@NotNull Class<T> clazz) {
        return (ORMTable<T>) this.tablesByClass.get(clazz);
    }

    @NotNull
    public <T> SelectQuery<T> selectQuery(@NotNull Class<T> clazz) {
        ORMTable<T> table = this.getTable(clazz);
        if(table == null) {
            throw new IllegalArgumentException("Given class \"" + clazz + "\" wasn't registered as table!");
        }

        return new SelectQuery<>(table);
    }

    @NotNull
    public <T> InsertQuery<T> insertQuery(@NotNull Class<T> clazz) {
        ORMTable<T> table = this.getTable(clazz);
        if(table == null) {
            throw new IllegalArgumentException("Given class \"" + clazz + "\" wasn't registered as table!");
        }

        return new InsertQuery<>(table);
    }

    @NotNull
    public <T> UpdateQuery<T> updateQuery(@NotNull Class<T> clazz, @Nullable T object) {
        ORMTable<T> table = this.getTable(clazz);
        if(table == null) {
            throw new IllegalArgumentException("Given class \"" + clazz + "\" wasn't registered as table!");
        }

        return new UpdateQuery<>(table, object);
    }

    @NotNull
    public <T> UpdateQuery<T> updateQuery(@NotNull Class<T> clazz) {
        return this.updateQuery(clazz, null);
    }

    @NotNull
    public <T> DeleteQuery<T> deleteQuery(@NotNull Class<T> clazz) {
        ORMTable<T> table = this.getTable(clazz);
        if(table == null) {
            throw new IllegalArgumentException("Given class \"" + clazz + "\" wasn't registered as table!");
        }

        return new DeleteQuery<>(table);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ORMDatabase database = (ORMDatabase) o;
        return this.linkUserPasswordHash == database.linkUserPasswordHash && this.driver == database.driver;
    }

    @Override
    public int hashCode() {
        return Objects.hash(driver, linkUserPasswordHash);
    }
}
