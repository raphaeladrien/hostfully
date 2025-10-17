package com.hostfully.app.booking.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String msg) {
        super(msg);
    }

    public String getTitle() {
        return "Booking not found";
    }
}
