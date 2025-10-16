package com.hostfully.app.block.exceptions;

public class BlockCreationException extends RuntimeException {
    public BlockCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getTitle() {
        return "Unexpected error while creating block";
    }
}
