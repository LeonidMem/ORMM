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
        return this.column.name();
    }

    @Override
    public boolean unique() {
        return this.column.unique();
    }

    @Override
    public boolean notNull() {
        return this.column.notNull();
    }

    @Override
    public int length() {
        return this.column.length();
    }

    @Override
    public boolean index() {
        return this.column.index();
    }

    @Override
    public Class<?> databaseClass() {
        return this.column.databaseClass();
    }

    @Override
    public String loadFunction() {
        return this.column.loadFunction();
    }

    @Override
    public String saveFunction() {
        return this.column.saveFunction();
    }

    public boolean primaryKey() {
        return this.primaryKey != null;
    }

    @Override
    public boolean autoIncrement() {
        return this.primaryKey != null && this.primaryKey.autoIncrement();
    }

    public boolean foreignKey() {
        return this.foreignKey != null;
    }

    // TODO: probably rename
    @Override
    public String table() {
        if (this.foreignKey == null) {
            throw new IllegalArgumentException("Can't get table() value because @ForeignKey is null!");
        }

        return this.foreignKey.table();
    }

    @Override
    public String key() {
        if (this.foreignKey == null) {
            throw new IllegalArgumentException("Can't get key() value because @ForeignKey is null!");
        }

        return this.foreignKey.key();
    }

    @Override
    public boolean makeReference() {
        if (this.foreignKey == null) {
            throw new IllegalArgumentException("Can't get makeReference() value because @ForeignKey is null!");
        }

        return this.foreignKey.makeReference();
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
