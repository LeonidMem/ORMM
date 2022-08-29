package ru.leonidm.ormm.orm.queries.update;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.clauses.Where;
import ru.leonidm.ormm.orm.queries.AbstractQuery;

import java.util.LinkedHashMap;

public abstract class AbstractUpdateQuery<O, T, R> extends AbstractQuery<T, R> {

    protected final T object;
    protected final LinkedHashMap<ORMColumn<T, ?>, Object> values = new LinkedHashMap<>();
    protected Where where = null;

    public AbstractUpdateQuery(@NotNull ORMTable<T> table, @Nullable T object) {
        super(table);
        this.object = object;

        if(this.object != null) {
            ORMColumn<T, ?> keyColumn = table.getKeyColumn();
            if(keyColumn == null)
                throw new IllegalArgumentException("Object of table without key column can't be served!");

            this.where = Where.compare(keyColumn.getName(), "=", keyColumn.getValue(object));
        }
    }
}
