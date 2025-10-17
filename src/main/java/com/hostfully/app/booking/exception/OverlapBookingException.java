package com.hostfully.app.booking.exception;

public class OverlapBookingException extends RuntimeException {

    public OverlapBookingException(String msg) {
        super(msg);
    }

    public String getTitle() {
        return "Booking already scheduled for this property";
    }
}
