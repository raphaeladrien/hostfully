package com.hostfully.app.shared.util;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DateRangeValidatorTest {

    @ParameterizedTest
    @MethodSource("provideRanges")
    @DisplayName("should return true when a valid data range is provided")
    void testValidDateRange(final LocalDate startDate, final LocalDate endDate, final boolean couldBeSame) {
        Assertions.assertTrue(DateRangeValidator.validateDateRange(startDate, endDate, couldBeSame));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRanges")
    @DisplayName("should return false when a valid data range is provided")
    void testInvalidDateRange(final LocalDate startDate, final LocalDate endDate, final boolean couldBeSame) {
        Assertions.assertFalse(DateRangeValidator.validateDateRange(startDate, endDate, couldBeSame));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidParam")
    @DisplayName("should throw IllegalArgumentException when a invalid param provided")
    void testInvalidParam(final LocalDate startDate, final LocalDate endDate, final boolean couldBeSame) {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> DateRangeValidator.validateDateRange(startDate, endDate, couldBeSame));
    }

    private static Stream<Arguments> provideRanges() {
        return Stream.of(
                arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1), true),
                arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 10), true),
                arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 10), false));
    }

    private static Stream<Arguments> provideInvalidRanges() {
        return Stream.of(
                arguments(LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 1), false),
                arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1), false));
    }

    private static Stream<Arguments> provideInvalidParam() {
        return Stream.of(
                arguments(null, null, false),
                arguments(null, LocalDate.of(2025, 1, 1), false),
                arguments(LocalDate.of(2025, 1, 1), null, false));
    }
}
