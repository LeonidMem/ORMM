package ru.leonidm.ormm.orm.queries.update;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.clauses.Where;

import java.util.function.Supplier;

import static ru.leonidm.ormm.utils.FormatUtils.toStringSQLValue;

public final class SingleUpdateQuery<T> extends AbstractUpdateQuery<SingleUpdateQuery<T>, T> {

    private final T object;

    public SingleUpdateQuery(@NotNull ORMTable<T> table, @NotNull T object) {
        super(table);

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
            this.values.put(column.getName(), column.getValue(this.object));
        });

        this.values.forEach((key, value) -> {
            ORMColumn<T, ?> column = this.table.getColumn(key);

            queryBuilder.append(' ').append(key).append(" = ")
                    .append(toStringSQLValue(column.toDatabaseObject(value)));
        });

        queryBuilder.append(" WHERE ").append(this.where.build(this.table));
        queryBuilder.append(" LIMIT ").append(this.limit);

        return queryBuilder.toString();
    }

    @Override
    @NotNull
    protected Supplier<Void> prepareSupplier() {
        return () -> {
            return null;
        };
    }
}
