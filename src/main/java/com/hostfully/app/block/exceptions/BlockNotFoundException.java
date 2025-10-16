package com.hostfully.app.block.exceptions;

public class BlockNotFoundException extends RuntimeException {
    public BlockNotFoundException(String msg) {
        super(msg);
    }

    public String getTitle() {
        return "Block not found";
    }
}
