package ru.leonidm.ormm.orm.queries.update;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.clauses.Where;
import ru.leonidm.ormm.orm.queries.AbstractQuery;

import java.util.LinkedHashMap;

public abstract class AbstractUpdateQuery<O, T> extends AbstractQuery<T, Void> {

    protected final LinkedHashMap<String, Object> values = new LinkedHashMap<>();
    protected Where where = null;
    protected int limit = 0;

    public AbstractUpdateQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @NotNull
    public O set(@NotNull String column, @Nullable Object object) {
        if(this.table.getColumn(column) == null) {
            throw new IllegalArgumentException("Can't find column \"" + column.toLowerCase() + "\"!");
        }

        this.values.put(column.toLowerCase(), object);
        return (O) this;
    }
}
