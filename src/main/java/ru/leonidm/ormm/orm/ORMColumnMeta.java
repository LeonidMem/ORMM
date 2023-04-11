package ru.leonidm.ormm.orm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.annotations.Column;
import ru.leonidm.ormm.annotations.ForeignKey;
import ru.leonidm.ormm.annotations.PrimaryKey;

import java.lang.annotation.Annotation;

public final class ORMColumnMeta implements Column, PrimaryKey, ForeignKey {

    private final Column column;
    private final PrimaryKey primaryKey;
    private final ForeignKey foreignKey;

    public ORMColumnMeta(@NotNull Column column, @Nullable PrimaryKey primaryKey,
                         @Nullable ForeignKey foreignKey) {
        this.column = column;

        this.primaryKey = primaryKey;
        this.foreignKey = foreignKey;
    }

    @Override
    public String name() {
        return column.name();
    }

    @Override
    public boolean unique() {
        return column.unique();
    }

    @Override
    public boolean notNull() {
        return column.notNull();
    }

    @Override
    public int length() {
        return column.length();
    }

    @Override
    public boolean index() {
        return column.index();
    }

    @Override
    public Class<?> databaseClass() {
        return column.databaseClass();
    }

    public boolean primaryKey() {
        return primaryKey != null;
    }

    @Override
    public boolean autoIncrement() {
        return primaryKey != null && primaryKey.autoIncrement();
    }

    public boolean foreignKey() {
        return foreignKey != null;
    }

    @Override
    public String table() {
        if (foreignKey == null) {
            throw new IllegalArgumentException("Can't get table() value because @ForeignKey is null");
        }

        return foreignKey.table();
    }

    @Override
    public String key() {
        if (foreignKey == null) {
            throw new IllegalArgumentException("Can't get key() value because @ForeignKey is null");
        }

        return foreignKey.key();
    }

    @Override
    public boolean makeReference() {
        if (foreignKey == null) {
            throw new IllegalArgumentException("Can't get makeReference() value because @ForeignKey is null");
        }

        return foreignKey.makeReference();
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
