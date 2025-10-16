package com.hostfully.app.block.exceptions;

public class BlockGenericException extends RuntimeException {
    public BlockGenericException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getTitle() {
        return "Unexpected error while processing block";
    }
}
