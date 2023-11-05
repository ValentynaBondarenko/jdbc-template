package com.bondarenko.exception;

public class DataAccessException extends RuntimeException {
    public DataAccessException(Exception exception) {
        super(exception);
    }

    public DataAccessException(String message, Exception exception) {
    }
}
