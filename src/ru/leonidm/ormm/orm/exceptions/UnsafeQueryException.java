package ru.leonidm.ormm.orm.exceptions;

public class UnsafeQueryException extends RuntimeException {

    public UnsafeQueryException() {

    }

    public UnsafeQueryException(String message) {
        super(message);
    }

    public UnsafeQueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsafeQueryException(Throwable cause) {
        super(cause);
    }

    public UnsafeQueryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
