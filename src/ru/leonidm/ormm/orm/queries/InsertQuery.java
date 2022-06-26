package ru.leonidm.ormm.orm.queries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.utils.ClassUtils;
import ru.leonidm.ormm.utils.FormatUtils;
import ru.leonidm.ormm.utils.ReflectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

public final class InsertQuery<T> extends AbstractQuery<T, T> {

    private boolean ignore = false;
    private final LinkedHashMap<String, Object> values = new LinkedHashMap<>();

    public InsertQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @NotNull
    public InsertQuery<T> ignore(boolean ignore) {
        this.ignore = ignore;
        return this;
    }

    @NotNull
    public InsertQuery<T> value(@NotNull String column, @Nullable Object value) {
        if(this.table.getColumn(column) == null) {
            throw new IllegalArgumentException("Can't find column \"" + column.toLowerCase() + "\"!");
        }

        this.values.put(column.toLowerCase(), value);
        return this;
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        ORMDriver driver = this.table.getDatabase().getDriver();

        queryBuilder.append("INSERT ");

        if(this.ignore) {
            queryBuilder.append(driver.get(ORMDriver.Key.INSERT_IGNORE)).append(' ');
        }

        queryBuilder.append("INTO ").append(this.table.getName()).append(" (");

        List<ORMColumn<T, ?>> columns = this.table.getColumnsStream()
                .filter(column -> !(column.getMeta().autoIncrement()) || this.values.containsKey(column.getName()))
                .toList();

        if(columns.isEmpty()) {
            queryBuilder.append(") VALUES ()");
            return queryBuilder.toString();
        }

        queryBuilder.append(columns.get(0).getName());
        for(int i = 1; i < columns.size(); i++) {
            queryBuilder.append(", ").append(columns.get(i).getName());
        }

        queryBuilder.append(") VALUES (");

        columns.forEach(column -> {
            Object value = this.values.get(column.getName());
            Object finalValue = column.toDatabaseObject(value);
            queryBuilder.append(FormatUtils.toStringSQLValue(finalValue)).append(", ");
        });

        return queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length()).append(")").toString();
    }

    @Override
    @NotNull
    protected Supplier<T> prepareSupplier() {
        return () -> {
            try(Statement statement = this.table.getDatabase().getConnection().createStatement()) {
                int affected = switch(this.table.getDatabase().getDriver()) {
                    case MYSQL -> statement.executeUpdate(getSQLQuery(), Statement.RETURN_GENERATED_KEYS);
                    case SQLITE -> statement.executeUpdate(getSQLQuery());
                };

                if(affected == 0) {
                    return null;
                }

                T t = ReflectionUtils.getNewInstance(this.table.getOriginalClass());

                this.table.getColumnsStream().forEach(column ->
                        column.setValue(t, column.toFieldObject(this.values.get(column.getName()))));

                ResultSet generatedKeys = statement.getGeneratedKeys();

                if(generatedKeys.next()) {
                    ORMColumn<T, ?> column = this.table.getKeyColumn();

                    if(ClassUtils.isInteger(column.getFieldClass())) {
                        column.setValue(t, generatedKeys.getInt(1));
                    }
                    else if(ClassUtils.isLong(column.getFieldClass())) {
                        column.setValue(t, generatedKeys.getLong(1));
                    }
                    else {
                        throw new IllegalStateException("Got wrong @PrimaryKey with wrong field class!");
                    }
                }

                return t;

            } catch(SQLException e) {
                e.printStackTrace();
                return null;
            }
        };
    }
}
