package com.hostfully.app.infra.exception;

public class InvalidDateRangeException extends RuntimeException {
    public InvalidDateRangeException(String msg) {
        super(msg);
    }

    public String getTitle() {
        return "Invalid Temporal Range";
    }
}
