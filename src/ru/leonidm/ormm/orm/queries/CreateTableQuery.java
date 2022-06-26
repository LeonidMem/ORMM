package ru.leonidm.ormm.orm.queries;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumnMeta;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.utils.QueryUtils;

import java.util.function.Supplier;

public final class CreateTableQuery<T> extends AbstractQuery<T, Void> {

    public CreateTableQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("CREATE TABLE IF NOT EXISTS ").append(this.table.getName()).append(" (");

        ORMDriver driver = this.table.getDatabase().getDriver();

        this.table.getColumnsStream().forEachOrdered(column -> {
            QueryUtils.writeColumnDefinition(queryBuilder, driver, column);
            queryBuilder.append(", ");
        });

        this.table.getColumnsStream().filter(column -> column.getMeta().foreignKey() && column.getMeta().makeReference())
                .forEachOrdered(column -> {
            ORMColumnMeta meta = column.getMeta();
            queryBuilder.append("FOREIGN KEY(").append(column.getName()).append(") REFERENCES ")
                    .append(meta.table()).append('(').append(meta.key()).append("), ");
        });

        return queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length()).append(')').toString();
    }

    @Override
    @NotNull
    protected Supplier<Void> prepareSupplier() {
        return getUpdateSupplier();
    }
}
