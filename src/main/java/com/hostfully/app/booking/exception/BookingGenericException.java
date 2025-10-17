package com.hostfully.app.booking.exception;

public class BookingGenericException extends RuntimeException {
    public BookingGenericException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getTitle() {
        return "Unexpected error while processing block";
    }
}
