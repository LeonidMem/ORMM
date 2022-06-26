package ru.leonidm.ormm.orm.queries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMTable;
import ru.leonidm.ormm.orm.thread.ORMTask;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractQuery<T, R> {

    protected final ORMTable<T> table;

    public AbstractQuery(@NotNull ORMTable<T> table) {
        this.table = table;
    }

    @NotNull
    public abstract String getSQLQuery();

    @NotNull
    protected abstract Supplier<R> prepareSupplier();

    @NotNull
    protected final Supplier<R> getUpdateSupplier() {
        return () -> {
            try(Statement statement = this.table.getDatabase().getConnection().createStatement()) {
                statement.executeUpdate(getSQLQuery());
            } catch(SQLException e) {
                e.printStackTrace();
            }

            return null;
        };
    }

    // TODO: probably remove
    @NotNull
    private ORMTask<R> prepareTask(@NotNull Consumer<R> consumer) {
        return new ORMTask<>(prepareSupplier(), consumer);
    }

    @NotNull
    public final ORMTask<R> queue(@NotNull Consumer<R> consumer) {
        ORMTask<R> task = prepareTask(consumer);
        task.start();
        return task;
    }

    @NotNull
    public final ORMTask<R> queue() {
        return this.queue(r -> {});
    }

    // TODO: think about name
    @Nullable
    public final R waitQueue() {
        return this.queue().waitForResult().getResult();
    }

    @NotNull
    public String toString() {
        return getSQLQuery();
    }
}
