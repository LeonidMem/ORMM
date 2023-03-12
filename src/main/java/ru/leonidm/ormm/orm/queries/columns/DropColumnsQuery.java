package ru.leonidm.ormm.orm.queries.columns;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.general.ColumnData;
import ru.leonidm.ormm.orm.queries.AbstractQuery;

import java.util.List;
import java.util.function.Supplier;

public final class DropColumnsQuery<T> extends AbstractQuery<T, Void> {

    private final List<ColumnData> columns;

    public DropColumnsQuery(@NotNull ORMTable<T> table, @NotNull List<ColumnData> columns) {
        super(table);

        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Got empty list of the columns");
        }

        columns.forEach(column -> {
            if (!column.getTable().equals(table.getName())) {
                throw new IllegalArgumentException("There is at least one column with different table");
            }
        });

        this.columns = columns;
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        StringBuilder queryBuilder = new StringBuilder();

        return switch (this.table.getDatabase().getDriver()) {
            case ORMDriver.MYSQL -> {
                queryBuilder.append("ALTER TABLE ").append(this.table.getName()).append(' ');

                this.columns.forEach(column -> {
                    queryBuilder.append("DROP COLUMN ").append(column.getName()).append(", ");
                });

                yield queryBuilder.substring(0, queryBuilder.length() - 2);
            }
            case ORMDriver.SQLITE -> {
                this.columns.forEach(column -> {
                    queryBuilder.append("ALTER TABLE ").append(this.table.getName()).append(" DROP COLUMN ")
                            .append(column.getName()).append(';');
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
