package com.hostfully.app.booking.exception;

public class UpdateNotAllowedException extends RuntimeException {
    public UpdateNotAllowedException(String msg) {
        super(msg);
    }

    public String getTitle() {
        return "Updated not allowed";
    }
}
