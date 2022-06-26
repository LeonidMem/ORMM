package ru.leonidm.ormm.utils;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.ormm.orm.ORMColumn;
import ru.leonidm.ormm.orm.ORMDriver;
import ru.leonidm.ormm.orm.general.SQLType;

public final class QueryUtils {

    private QueryUtils() {}

    public static void writeColumnDefinition(@NotNull StringBuilder queryBuilder, @NotNull ORMDriver driver,
                                             @NotNull ORMColumn<?, ?> column) {
        SQLType sqlType = column.getSQLType();
        if(sqlType == null) {
            throw new IllegalArgumentException("Can't find SQL type for \"" + column.getDatabaseClass() + "\"!");
        }

        queryBuilder.append(column.getName()).append(' ').append(sqlType);

        if(sqlType.hasLength()) {
            int defaultLength = sqlType.getDefaultLength();
            int finalLength;

            if(column.getMeta().length() <= 0) {
                if(defaultLength <= 0) {
                    throw new IllegalArgumentException(column.getIdentifier() +
                            " Annotation @Column must override length with positive value!");
                }

                finalLength = defaultLength;
            }
            else {
                finalLength = column.getMeta().length();
            }

            queryBuilder.append('(').append(finalLength).append(')');
        }
        else {
            if(column.getMeta().length() > 0) {
                throw new IllegalArgumentException(column.getIdentifier() +
                        " Annotation @Column can't override length of SQL type of this column!");
            }
        }

        if(column.getMeta().unique()) {
            queryBuilder.append(" UNIQUE");
        }

        if(column.getMeta().primaryKey()) {
            queryBuilder.append(" PRIMARY KEY");

            if(column.getMeta().autoIncrement()) {
                queryBuilder.append(' ').append(driver.get(ORMDriver.Key.AUTOINCREMENT));
            }
        }

        if(column.getMeta().notNull()) {
            queryBuilder.append(" NOT NULL");
        }
    }

}
