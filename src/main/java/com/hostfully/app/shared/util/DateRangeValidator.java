package com.hostfully.app.shared.util;

import java.time.LocalDate;

public final class DateRangeValidator {

    public static boolean validateDateRange(LocalDate startDate, LocalDate endDate, boolean couldBeSame) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }

        final boolean result = endDate.isAfter(startDate);

        if (couldBeSame && !result) return startDate.isEqual(endDate);

        return result;
    }
}
