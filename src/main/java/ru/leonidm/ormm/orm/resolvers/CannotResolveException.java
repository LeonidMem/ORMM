package ru.leonidm.ormm.orm.resolvers;

public class CannotResolveException extends Exception {

    public CannotResolveException() {
        super();
    }

    public CannotResolveException(String message) {
        super(message);
    }

    public CannotResolveException(String message, Throwable cause) {
        super(message, cause);
    }

    public CannotResolveException(Throwable cause) {
        super(cause);
    }

    protected CannotResolveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
