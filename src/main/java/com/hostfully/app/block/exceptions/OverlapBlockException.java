package com.hostfully.app.block.exceptions;

public class OverlapBlockException extends RuntimeException {
    public OverlapBlockException(String msg) {
        super(msg);
    }

    public String getTitle() {
        return "Block not allowed";
    }
}
