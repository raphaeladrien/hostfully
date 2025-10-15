package com.hostfully.app.shared.util;

import com.soundicly.jnanoidenhanced.jnanoid.NanoIdUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NanoIdGenerator {

    private final Integer keySize;

    public NanoIdGenerator(@Value("${nanoid.key.size}") final Integer keySize) {
        this.keySize = keySize;
    }

    public String generateId() {
        return NanoIdUtils.randomNanoId(keySize);
    }
}
