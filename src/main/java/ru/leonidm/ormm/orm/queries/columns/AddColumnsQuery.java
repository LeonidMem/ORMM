package ru.leonidm.ormm.orm.queries.columns;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.queries.AbstractQuery;
import ru.leonidm.ormm.utils.Pair;
import ru.leonidm.ormm.utils.QueryUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class AddColumnsQuery<T> extends AbstractQuery<T, Void> {

    private final List<Pair<ORMColumn<T, ?>, ORMColumn<T, ?>>> columns;

    public AddColumnsQuery(@NotNull ORMTable<T> table, @NotNull List<Pair<ORMColumn<T, ?>, ORMColumn<T, ?>>> columns) {
        super(table);

        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Got empty list of the columns");
        }

        columns.forEach(pair -> {
            ORMTable<T> leftTable = Objects.requireNonNull(pair.getLeft()).getTable();
            ORMColumn<T, ?> afterColumn = pair.getRight();
            if (afterColumn != null && leftTable != afterColumn.getTable() || leftTable != table) {
                throw new IllegalArgumentException("There are columns with different tables");
            }
        });

        this.columns = columns;
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        ORMDriver driver = this.table.getDatabase().getDriver();

        StringBuilder queryBuilder = new StringBuilder();

        return switch (driver) {
            case MYSQL -> {

                queryBuilder.append("ALTER TABLE ").append(QueryUtils.getTableName(this.table)).append(' ');

                this.columns.forEach(pair -> {
                    queryBuilder.append("ADD ");
                    ORMColumn<T, ?> column = Objects.requireNonNull(pair.getLeft());
                    ORMColumn<T, ?> after = pair.getRight();

                    // TODO: implement foreign keys
                    QueryUtils.writeColumnDefinition(queryBuilder, driver, column);

                    if (after == null) {
                        queryBuilder.append(" FIRST");
                    } else {
                        queryBuilder.append(" AFTER ").append(after.getName());
                    }

                    queryBuilder.append(", ");
                });

                yield queryBuilder.substring(0, queryBuilder.length() - 2);
            }
            case SQLITE -> {
                this.columns.forEach(pair -> {
                    queryBuilder.append("ALTER TABLE ").append(QueryUtils.getTableName(this.table)).append(" ADD COLUMN ");

                    ORMColumn<T, ?> column = Objects.requireNonNull(pair.getLeft());

                    QueryUtils.writeColumnDefinition(queryBuilder, driver, column);

                    queryBuilder.append(';');
                });

                yield queryBuilder.toString();
            }
        };
    }

    @Override
    @NotNull
    protected Supplier<Void> prepareSupplier() {
        return this.getUpdateSupplier();
    }
}
