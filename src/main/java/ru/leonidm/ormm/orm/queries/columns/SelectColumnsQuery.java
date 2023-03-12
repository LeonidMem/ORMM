package ru.leonidm.ormm.orm.queries.columns;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.general.ColumnData;
import ru.leonidm.ormm.orm.queries.AbstractQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class SelectColumnsQuery<T> extends AbstractQuery<T, List<ColumnData>> {

    public SelectColumnsQuery(@NotNull ORMTable<T> table) {
        super(table);
    }

    @Override
    @NotNull
    public String getSQLQuery() {
        return switch (this.table.getDatabase().getDriver()) {
            case MYSQL -> "SELECT column_name, data_type, character_maximum_length " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = DATABASE() " +
                    "AND table_name = \"" + this.table.getName() + "\" " +
                    "ORDER BY ordinal_position";
            case SQLITE -> "PRAGMA table_info(\"" + this.table.getName() + "\")";
        };
    }

    @Override
    @NotNull
    protected Supplier<List<ColumnData>> prepareSupplier() {
        return () -> {
            List<ColumnData> out = new ArrayList<>();

            try (Statement statement = this.table.getDatabase().getConnection().createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(getSQLQuery())) {
                    while (resultSet.next()) {
                        String columnName, columnType;
                        int length;

                        switch (this.table.getDatabase().getDriver()) {
                            case MYSQL -> {
                                columnName = resultSet.getString("column_name").toLowerCase();
                                columnType = resultSet.getString("data_type").toUpperCase();
                                if (columnType.equals("INT")) {
                                    columnType = "INTEGER";
                                }

                                length = resultSet.getInt("character_maximum_length");
                            }
                            case SQLITE -> {
                                columnName = resultSet.getString("name").toLowerCase();
                                columnType = resultSet.getString("type").toUpperCase();

                                int index = columnType.indexOf('(');
                                if (index != -1) {
                                    length = Integer.parseInt(columnType.substring(index + 1, columnType.length() - 1));
                                    columnType = columnType.substring(0, index);
                                } else {
                                    length = 0;
                                }
                            }
                            default -> {
                                throw new IllegalStateException("Unexpected driver: %s".formatted(this.table.getDatabase().getDriver()));
                            }
                        }

                        out.add(new ColumnData(this.table.getName(), columnName, columnType, length));
                    }
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }

            return out;
        };
    }
}
