package com.hostfully.app.booking.exception;

public class ConcurrentBookingException extends RuntimeException {
    public ConcurrentBookingException(String message) {
        super(message);
    }
}
