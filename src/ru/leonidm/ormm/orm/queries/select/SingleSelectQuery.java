package ru.leonidm.ormm.orm.queries.select;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

public final class SingleSelectQuery<T> extends AbstractSelectQuery<SingleSelectQuery<T>, T, T> {

    public SingleSelectQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @Override
    @NotNull
    public SingleSelectQuery<T> limit(int limit) {
        throw new IllegalStateException("Can't change limit of SingleSelectQuery!");
    }

    @Override
    @NotNull
    protected Supplier<T> prepareSupplier() {
        return () -> {
            try(Statement statement = this.table.getDatabase().getConnection().createStatement()) {

                try(ResultSet resultSet = statement.executeQuery(getSQLQuery())) {
                    if(resultSet.next()) {
                        return this.table.objectFrom(resultSet);
                    }
                }

            } catch(SQLException e) {
                e.printStackTrace();
            }

            return null;
        };
    }

    @NotNull
    public RawSingleSelectQuery<T> columns(String... columns) {
        checkIfColumnsExist(columns);

        RawSingleSelectQuery<T> rawSelectQuery = new RawSingleSelectQuery<>(this.table);

        this.copy(rawSelectQuery);
        rawSelectQuery.columns = columns;

        return rawSelectQuery;
    }
}
