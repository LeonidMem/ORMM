package ru.leonidm.ormm.orm.queries.update;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.clauses.Where;
import ru.leonidm.ormm.utils.FormatUtils;
import ru.leonidm.ormm.utils.QueryUtils;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

public final class UpdateObjectQuery<T> extends AbstractUpdateQuery<UpdateObjectQuery<T>, T, T> {

    public UpdateObjectQuery(@NotNull ORMTable<T> table, @NotNull T object) {
        super(table, object);

        ORMColumn<T, ?> keyColumn = table.getKeyColumn();
        if (keyColumn == null) {
            throw new IllegalArgumentException("SingleUpdateQuery can be used only in the tables with the primary key");
        }

        this.where = Where.compare(keyColumn.getName(), "=", keyColumn.getValue(object));
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("UPDATE ").append(QueryUtils.getTableName(this.table)).append(" SET");

        this.table.getColumnsStream().forEachOrdered(column -> {
            if (column.getMeta().primaryKey()) {
                return;
            }

            Object value = column.getValue(this.object);

            queryBuilder.append(' ');

            switch (this.table.getDatabase().getDriver()) {
                case MYSQL -> queryBuilder.append(QueryUtils.getTableName(this.table)).append('.');
                case SQLITE -> {
                }
            }

            queryBuilder.append(column.getName()).append(" = ")
                    .append(FormatUtils.toStringSQLValue(column.toDatabaseObject(value))).append(",");
        });

        queryBuilder.delete(queryBuilder.length() - 1, queryBuilder.length());

        queryBuilder.append(" WHERE ").append(this.where.build(this.table));

        switch (this.table.getDatabase().getDriver()) {
            case MYSQL -> {
                queryBuilder.append(" LIMIT ").append(1);
            }
            case SQLITE -> {

            }
        }

        return queryBuilder.toString();
    }

    @Override
    @NotNull
    protected Supplier<T> prepareSupplier() {
        return () -> {
            try (Statement statement = this.table.getDatabase().getConnection().createStatement()) {
                statement.executeUpdate(this.getSQLQuery());
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }

            return this.object;
        };
    }
}
