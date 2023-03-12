package ru.leonidm.ormm.orm.queries.select;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class SelectQuery<T> extends AbstractSelectQuery<SelectQuery<T>, T, List<T>> {

    public SelectQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @Override
    @NotNull
    protected Supplier<List<T>> prepareSupplier() {
        return () -> {
            List<T> out = new ArrayList<>();

            try (Statement statement = this.table.getDatabase().getConnection().createStatement()) {

                try (ResultSet resultSet = statement.executeQuery(getSQLQuery())) {
                    while (resultSet.next()) {
                        out.add(this.table.objectFrom(resultSet));
                    }
                }

            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }

            return out;
        };
    }

    @NotNull
    public SingleSelectQuery<T> single() {
        SingleSelectQuery<T> singleSelectQuery = new SingleSelectQuery<>(this.table);

        this.copy(singleSelectQuery);
        singleSelectQuery.limit = 1;

        return singleSelectQuery;
    }

    @NotNull
    public RawSelectQuery<T> columns(String @NotNull ... columns) {
        checkIfColumnsExist(columns);

        RawSelectQuery<T> rawSelectQuery = new RawSelectQuery<>(this.table);

        this.copy(rawSelectQuery);
        rawSelectQuery.columns = columns;

        return rawSelectQuery;
    }
}
