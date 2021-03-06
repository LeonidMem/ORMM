package ru.leonidm.ormm.orm.thread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ORMTask<R> extends Thread {

    private final Supplier<R> supplier;
    private final Consumer<R> consumer;
    private final StackTraceElement[] stackTraceElements;
    private volatile boolean done = false;
    private R result;

    // TODO: save stacktrace at the moment where this constructor was called
    public ORMTask(@NotNull Supplier<R> supplier, @NotNull Consumer<R> consumer) {
        this.supplier = supplier;
        this.consumer = consumer;

        StackTraceElement[] stackTraceElements = Thread.getAllStackTraces().get(Thread.currentThread());
        this.stackTraceElements = new StackTraceElement[stackTraceElements.length - 3];

        System.arraycopy(stackTraceElements, 3, this.stackTraceElements, 0, this.stackTraceElements.length);
    }

    // TODO: think about name
    @NotNull
    public ORMTask<R> waitForResult() {
        while(!this.done) {
            Thread.onSpinWait();
        }

        return this;
    }

    @Override
    public void run() {
        try {
            this.result = this.supplier.get();
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println("Task initialize StackTrace:");
            for(StackTraceElement traceElement : this.stackTraceElements) {
                System.err.println("\tat " + traceElement);
            }
        }
        this.consumer.accept(this.result);
        this.done = true;
    }

    public boolean isDone() {
        return this.done;
    }

    @Nullable
    public R getResult() {
        if(!this.done) {
            throw new IllegalStateException("Can't get result because the task isn't done yet!");
        }

        return this.result;
    }
}
