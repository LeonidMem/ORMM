package ru.leonidm.ormm.utils;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.general.SQLType;

public final class QueryUtils {

    private QueryUtils() {
    }

    public static void writeColumnDefinition(@NotNull StringBuilder queryBuilder, @NotNull ORMDriver driver,
                                             @NotNull ORMColumn<?, ?> column) {
        SQLType sqlType = column.getSQLType();

        queryBuilder.append(column.getName()).append(' ').append(sqlType);

        if (sqlType.hasLength()) {
            int finalLength = QueryUtils.getLength(column);

            if (finalLength > 0) {
                queryBuilder.append('(').append(finalLength).append(')');
            }
        } else {
            if (column.getMeta().length() > 0) {
                throw new IllegalArgumentException(column.getIdentifier() +
                        " Annotation @Column can't override length of SQL type of this column");
            }
        }

        if (column.getMeta().unique()) {
            queryBuilder.append(" UNIQUE");
        }

        if (column.getMeta().primaryKey()) {
            queryBuilder.append(" PRIMARY KEY");

            if (column.getMeta().autoIncrement()) {
                queryBuilder.append(' ').append(driver.get(ORMDriver.Key.AUTOINCREMENT));
            }
        }

        if (column.getMeta().notNull()) {
            queryBuilder.append(" NOT NULL");
        }
    }

    public static int getLength(@NotNull ORMColumn<?, ?> column) {
        SQLType sqlType = column.getSQLType();

        if (sqlType.hasLength()) {
            int defaultLength = sqlType.getDefaultLength();

            if (column.getMeta().length() <= 0) {
                if (defaultLength <= 0) {
                    throw new IllegalArgumentException(column.getIdentifier() +
                            " Annotation @Column must override length with positive value");
                }

                return defaultLength;
            } else {
                return column.getMeta().length();
            }
        } else {
            if (column.getMeta().length() > 0) {
                throw new IllegalArgumentException(column.getIdentifier() +
                        " Annotation @Column can't override length of SQL type of this column");
            }

            return -1;
        }
    }

    @NotNull
    public static String getTableName(@NotNull ORMColumn<?, ?> column) {
        return getTableName(column.getTable());
    }

    @NotNull
    public static String getTableName(@NotNull ORMTable<?> table) {
        return table.getDatabase().getSettings().getTableNamePrefix() + table.getName();
    }

    @NotNull
    public static String getColumnName(@NotNull ORMColumn<?, ?> column) {
        return getTableName(column.getTable()) + '.' + column.getName();
    }
}
