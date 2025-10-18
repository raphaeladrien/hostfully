package com.hostfully.app.block.controller.dto;

import com.hostfully.app.shared.util.DateRangeValidator;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record BlockRequest(
        @NotNull @NotBlank(message = "Property must not be blank") @Size(max = 12, message = "Property is too long")
                String property,
        @NotNull(message = "Reason is required")
                @NotBlank(message = "Reason must not be blank")
                @Size(max = 250, message = "Reason is too long")
                String reason,
        @NotNull(message = "Start date is required")
                @FutureOrPresent(message = "Start date must be today or in the future")
                LocalDate startDate,
        @NotNull(message = "End date is required") @FutureOrPresent(message = "End date must be today or in the future")
                LocalDate endDate) {
    @AssertTrue(message = "End date must be after start date")
    public boolean isEndAfterStart() {
        return DateRangeValidator.validateDateRange(startDate, endDate, true);
    }
}
