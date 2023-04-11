package ru.leonidm.ormm.orm.queries.select;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.utils.QueryUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
class JoinsHandler<T, J> {

    private final ORMTable<T> table;
    private final List<AbstractSelectQuery.Join<J>> joins;
    private final Map<Object, J> keyToJ = new HashMap<>();
    private final Map<Object, Map<AbstractSelectQuery.JoinMeta<J>, List<Object>>> keyToObjects = new HashMap<>();

    public void save(@NotNull ResultSet resultSet, @NotNull J j) throws SQLException {
        ORMColumn<T, ?> keyColumn = table.getKeyColumn();
        Object key = keyColumn != null ? resultSet.getObject(QueryUtils.getColumnName(keyColumn)) : null;

        keyToJ.putIfAbsent(key, j);
        var map = keyToObjects.computeIfAbsent(key, k -> new HashMap<>());

        for (AbstractSelectQuery.Join<J> join : joins) {
            for (var entry : join.getColumns().entrySet()) {
                ORMColumn<?, ?> column = entry.getKey();
                AbstractSelectQuery.JoinMeta<J> joinMeta = entry.getValue();

                var list = map.computeIfAbsent(joinMeta, k -> new ArrayList<>());

                Object databaseObject = resultSet.getObject(QueryUtils.getColumnName(column));
                Object object = column.toFieldObject(databaseObject);

                list.add(object);
            }
        }
    }

    public void apply() {
        keyToObjects.forEach((object, map) -> {
            map.forEach((joinMeta, list) -> {
                J j = keyToJ.get(object);
                var consumer = joinMeta.getConsumer();

                if (joinMeta.isOne()) {
                    if (list.size() != 1) {
                        throw new IllegalStateException("Joined " + list.size() + " values, but asked for one to one");
                    }

                    consumer.accept(j, list.get(0));
                } else {
                    if (list.size() == 0) {
                        throw new IllegalStateException("Joined 0 values, but asked for one to many");
                    }

                    consumer.accept(j, list);
                }
            });
        });
    }

    @NotNull
    @UnmodifiableView
    public Collection<J> getObjects() {
        return Collections.unmodifiableCollection(keyToJ.values());
    }
}
