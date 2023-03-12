package ru.leonidm.ormm.orm.queries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.thread.ORMTask;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractQuery<T, R> {

    protected final ORMTable<T> table;

    protected AbstractQuery(@NotNull ORMTable<T> table) {
        this.table = table;
    }

    @NotNull
    public abstract String getSQLQuery();

    @NotNull
    protected abstract Supplier<R> prepareSupplier();

    @NotNull
    protected final Supplier<R> getUpdateSupplier() {
        return () -> {
            try (Statement statement = this.table.getDatabase().getConnection().createStatement()) {
                statement.executeUpdate(this.getSQLQuery());
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }

            return null;
        };
    }

    @NotNull
    public final ORMTask<R> queue(@NotNull Consumer<R> consumer, @Nullable Lock lock) {
        ORMTask<R> task = new ORMTask<>(this.table.getDatabase(), this.prepareSupplier(), consumer,
                lock, this.getSQLQuery());
        task.start();
        return task;
    }

    @NotNull
    public final ORMTask<R> queue(@NotNull Consumer<R> consumer) {
        return this.queue(consumer, null);
    }

    @NotNull
    public final ORMTask<R> queue(@NotNull Lock lock) {
        return this.queue(r -> {

        }, lock);
    }

    @NotNull
    public final ORMTask<R> queue() {
        return this.queue(r -> {

        });
    }

    @Nullable
    public final R complete() {
        return this.prepareSupplier().get();
    }

    @NotNull
    public String toString() {
        return this.getSQLQuery();
    }
}
