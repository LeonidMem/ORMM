package ru.leonidm.ormm.orm.thread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.ormm.orm.ORMDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ORMTask<R> implements Runnable {

    private final ORMDatabase database;
    private final Supplier<R> supplier;
    private final Consumer<R> consumer;
    private final Exception cause;
    private final List<Runnable> onFinally;
    private final Lock lock;
    private final String query;
    private volatile boolean initialized = false;
    private volatile boolean done = false;
    private R result;
    private RuntimeException exception;

    public ORMTask(@NotNull ORMDatabase database, @NotNull Supplier<R> supplier, @NotNull Consumer<R> consumer,
                   @Nullable Lock lock, @NotNull String query) {
        this.database = database;
        this.supplier = supplier;
        this.consumer = consumer;
        this.cause = new Exception();
        this.onFinally = new ArrayList<>(16);
        this.lock = lock;
        this.query = query;
    }

    public static void completeAll(@NotNull ORMTask<?> task1, @NotNull ORMTask<?> task2, @NotNull ORMTask<?> @NotNull ... tasks) {
        task1.complete();
        task2.complete();
        Arrays.stream(tasks).forEach(ORMTask::complete);
    }

    @NotNull
    public ORMTask<R> complete() {
        while (!done) {
            Thread.onSpinWait();
        }

        if (exception != null) {
            throw exception;
        }

        return this;
    }

    public void start() {
        if (initialized) {
            throw new IllegalStateException("Already started");
        }

        initialized = true;
        database.getTaskExecutor().execute(this);
    }

    @Override
    public void run() {
        try {
            if (lock != null) {
                lock.lock();
            }

            if (database.getSettings().isLogQueries()) {
                // TODO: normal logger
                System.out.println("[ORMM] " + query);
            }

            result = supplier.get();

            consumer.accept(result);
            done = true;
        } catch (Exception e) {
            Throwable tempE = e;
            while (tempE.getCause() != null) {
                tempE = tempE.getCause();
            }

            tempE.initCause(cause);
            done = true;

            exception = new IllegalStateException(e);
            // TODO: normal logger
            System.err.printf("Got exception at: %s%n", query);
            throw exception;
        } finally {
            if (lock != null) {
                lock.unlock();
            }

            onFinally.forEach(Runnable::run);
        }
    }

    public boolean isDone() {
        return done;
    }

    @Nullable
    public R getResult() {
        if (!done) {
            throw new IllegalStateException("Can't get result before task is done");
        }

        if (exception != null) {
            throw new IllegalStateException("Can't get result because task threw an exception");
        }

        return result;
    }

    public void onFinally(@NotNull Runnable runnable) {
        onFinally.add(runnable);

        if (result != null || exception != null) {
            runnable.run();
        }
    }
}
