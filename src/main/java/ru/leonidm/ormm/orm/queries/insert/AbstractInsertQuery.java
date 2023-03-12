package ru.leonidm.ormm.orm.queries.insert;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.queries.AbstractQuery;
import ru.leonidm.ormm.utils.FormatUtils;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public sealed abstract class AbstractInsertQuery<T> extends AbstractQuery<T, T> permits InsertQuery, InsertObjectQuery {

    protected boolean ignore = false;
    protected boolean onDuplicateUpdate = false;
    protected final LinkedHashMap<String, Object> values = new LinkedHashMap<>();

    protected AbstractInsertQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @NotNull
    public AbstractInsertQuery<T> ignore(boolean ignore) {
        this.ignore = ignore;
        return this;
    }

    @NotNull
    public AbstractInsertQuery<T> onDuplicateUpdate(boolean onDuplicateUpdate) {
        this.onDuplicateUpdate = onDuplicateUpdate;
        return this;
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        if (this.ignore && this.onDuplicateUpdate) {
            throw new IllegalArgumentException("Cannot ignore and perform on duplicate at the same time!");
        }

        ORMDriver driver = this.table.getDatabase().getDriver();

        queryBuilder.append("INSERT ");

        if (this.ignore) {
            queryBuilder.append(driver.get(ORMDriver.Key.INSERT_IGNORE)).append(' ');
        } else if (this.onDuplicateUpdate && driver == ORMDriver.SQLITE) {
            queryBuilder.append("OR REPLACE ");
        }

        queryBuilder.append("INTO ").append(this.table.getName()).append(" (");

        List<ORMColumn<T, ?>> columns = this.table.getColumnsStream()
                .filter(column -> !(column.getMeta().autoIncrement()) || this.values.containsKey(column.getName()))
                .toList();

        if (columns.isEmpty()) {
            queryBuilder.append(") VALUES ()");
            return queryBuilder.toString();
        }

        queryBuilder.append(columns.get(0).getName());
        for (int i = 1; i < columns.size(); i++) {
            queryBuilder.append(", ").append(columns.get(i).getName());
        }

        queryBuilder.append(") VALUES (");

        columns.forEach(column -> {
            Object value = this.values.get(column.getName());
            Object finalValue = column.toDatabaseObject(value);
            queryBuilder.append(FormatUtils.toStringSQLValue(finalValue)).append(", ");
        });
        queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length()).append(")");

        if (this.onDuplicateUpdate) {
            List<ORMColumn<T, ?>> conflictColumns = new ArrayList<>();
            List<ORMColumn<T, ?>> updateColumns = new ArrayList<>();

            columns.forEach(column -> {
                if (column.getMeta().primaryKey() || column.getMeta().unique()) {
                    conflictColumns.add(column);
                } else {
                    updateColumns.add(column);
                }
            });

            if (updateColumns.isEmpty()) {
                throw new IllegalArgumentException("Cannot resolve conflict on update with only unique columns");
            }

            if (conflictColumns.isEmpty()) {
                throw new IllegalArgumentException("Cannot resolve conflict on table without unique columns");
            }

            switch (this.table.getDatabase().getDriver()) {
                case ORMDriver.MYSQL -> {
                    queryBuilder.append(" ON DUPLICATE KEY UPDATE ");

                    updateColumns.forEach(column -> appendColumn(column, queryBuilder));
                    queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length());
                }
            }
        }

        return queryBuilder.toString();
    }

    private void appendColumn(@NotNull ORMColumn<T, ?> column, @NotNull StringBuilder queryBuilder) {
        Object value = this.values.get(column.getName());
        Object finalValue = column.toDatabaseObject(value);
        queryBuilder.append(column.getName()).append(" = ").append(FormatUtils.toStringSQLValue(finalValue)).append(", ");
    }

    @Override
    @NotNull
    protected Supplier<T> prepareSupplier() {
        return () -> {
            try (Statement statement = this.table.getDatabase().getConnection().createStatement()) {
                int affected = switch (this.table.getDatabase().getDriver()) {
                    case ORMDriver.MYSQL -> statement.executeUpdate(this.getSQLQuery(), Statement.RETURN_GENERATED_KEYS);
                    case ORMDriver.SQLITE -> statement.executeUpdate(this.getSQLQuery());
                };

                if (affected == 0) {
                    return null;
                }

                return this.getObjectToReturn(statement);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    @NotNull
    protected abstract T getObjectToReturn(@NotNull Statement statement) throws SQLException;
}
