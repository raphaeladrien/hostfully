package com.hostfully.app.booking.controller.dto;

import com.hostfully.app.shared.util.DateRangeValidator;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;

public record UpdateBookingRequest(
        @FutureOrPresent(message = "Start date must be today or in the future") LocalDate startDate,
        @FutureOrPresent(message = "End date must be today or in the future") LocalDate endDate,
        String guest,
        int numberGuest) {
    @AssertTrue(message = "End date must be after start date")
    public boolean isEndAfterStart() {
        if (startDate != null && endDate != null)
            return DateRangeValidator.validateDateRange(startDate, endDate, false);

        return true;
    }
}
