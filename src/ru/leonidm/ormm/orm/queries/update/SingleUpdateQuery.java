package ru.leonidm.ormm.orm.queries.update;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.clauses.Where;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

import static ru.leonidm.ormm.utils.FormatUtils.toStringSQLValue;

public final class SingleUpdateQuery<T> extends AbstractUpdateQuery<SingleUpdateQuery<T>, T, T> {

    private final T object;

    public SingleUpdateQuery(@NotNull ORMTable<T> table, @NotNull T object) {
        super(table, object);

        ORMColumn<T, ?> keyColumn = table.getKeyColumn();
        if(keyColumn == null) {
            throw new IllegalArgumentException("SingleUpdateQuery can be used only in the tables with the primary key!");
        }

        this.where = Where.compare(keyColumn.getName(), "=", keyColumn.getValue(object));
        this.object = object;
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("UPDATE ").append(this.table.getName()).append(" SET");

        this.table.getColumnsStream().forEachOrdered(column -> {
            Object value = column.getValue(this.object);

            // TODO: add table's name and dot
            queryBuilder.append(' ').append(column.getName()).append(" = ")
                    .append(toStringSQLValue(column.toDatabaseObject(value)));
        });

        queryBuilder.append(" WHERE ").append(this.where.build(this.table));
        queryBuilder.append(" LIMIT ").append(1);

        return queryBuilder.toString();
    }

    @Override
    @NotNull
    protected Supplier<T> prepareSupplier() {
        return () -> {
            try(Statement statement = this.table.getDatabase().getConnection().createStatement()) {
                statement.executeUpdate(this.getSQLQuery());
            } catch(SQLException e) {
                e.printStackTrace();
            }

            return this.object;
        };
    }
}
