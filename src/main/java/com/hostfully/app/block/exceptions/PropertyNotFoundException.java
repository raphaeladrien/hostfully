package com.hostfully.app.block.exceptions;

public class PropertyNotFoundException extends RuntimeException {
    public PropertyNotFoundException(String msg) {
        super(msg);
    }

    public String getTitle() {
        return "Property not found";
    }
}
