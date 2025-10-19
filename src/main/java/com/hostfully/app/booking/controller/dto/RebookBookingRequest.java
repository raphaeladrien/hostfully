package com.hostfully.app.booking.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hostfully.app.shared.util.DateRangeValidator;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record RebookBookingRequest(
        @NotNull(message = "Start date is required")
                @FutureOrPresent(message = "Start date must be today or in the future")
                LocalDate startDate,
        @NotNull(message = "End date is required") @FutureOrPresent(message = "End date must be today or in the future")
                LocalDate endDate) {
    @AssertTrue(message = "End date must be after start date")
    @JsonIgnore
    public boolean isEndAfterStart() {
        return DateRangeValidator.validateDateRange(startDate, endDate, false);
    }
}
