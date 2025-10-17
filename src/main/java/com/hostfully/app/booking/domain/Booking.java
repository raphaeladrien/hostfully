package com.hostfully.app.booking.domain;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Booking {
    private String id;
    private String propertyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String guestName;
    private Integer numberGuest;
    private String status;
}
