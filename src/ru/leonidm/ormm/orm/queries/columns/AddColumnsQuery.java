package ru.leonidm.ormm.orm.queries.columns;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.collections.Pair;
import ru.leonidm.ormm.orm.queries.AbstractQuery;
import ru.leonidm.ormm.utils.QueryUtils;

import java.util.List;
import java.util.function.Supplier;

public final class AddColumnsQuery<T> extends AbstractQuery<T, Void> {

    private final List<Pair<ORMColumn<T, ?>, ORMColumn<T, ?>>> columns;

    public AddColumnsQuery(@NotNull ORMTable<T> table, @NotNull List<Pair<ORMColumn<T, ?>, ORMColumn<T, ?>>> columns) {
        super(table);

        if(columns.isEmpty()) {
            throw new IllegalArgumentException("Got empty list of the columns!");
        }

        columns.forEach(pair -> {
            ORMTable<T> leftTable = pair.getLeft().getTable();
            ORMColumn<T, ?> afterColumn = pair.getRight();
            if(afterColumn != null && leftTable != afterColumn.getTable() || leftTable != table) {
                throw new IllegalArgumentException("There are columns with different tables!");
            }
        });

        this.columns = columns;
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        ORMDriver driver = this.table.getDatabase().getDriver();

        StringBuilder queryBuilder = new StringBuilder();

        return switch(driver) {
            case MYSQL -> {
                queryBuilder.append("ALTER TABLE ").append(this.table.getName()).append(' ');

                this.columns.forEach(pair -> {
                    queryBuilder.append("ADD ");
                    ORMColumn<T, ?> column = pair.getLeft();
                    ORMColumn<T, ?> after = pair.getRight();

                    // TODO: implement foreign keys
                    QueryUtils.writeColumnDefinition(queryBuilder, driver, column);

                    if(after == null) {
                        queryBuilder.append(" FIRST");
                    }
                    else {
                        queryBuilder.append(" AFTER ").append(after.getName());
                    }

                    queryBuilder.append(", ");
                });

                yield queryBuilder.substring(0, queryBuilder.length() - 2);
            }
            // TODO: implement AddColumnsQuery for SQLITE
            case SQLITE -> throw new IllegalStateException("Not implemented yet!");
        };
    }

    @Override
    @NotNull
    protected Supplier<Void> prepareSupplier() {
        return getUpdateSupplier();
    }
}
