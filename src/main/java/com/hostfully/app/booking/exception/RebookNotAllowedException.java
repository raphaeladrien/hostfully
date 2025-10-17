package com.hostfully.app.booking.exception;

public class RebookNotAllowedException extends RuntimeException {
    public RebookNotAllowedException(String msg) {
        super(msg);
    }

    public String getTitle() {
        return "Rebooking not allowed for this reservation";
    }
}
