package com.hostfully.app.block.domain;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Block {
    private String id;
    private String propertyId;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
}
