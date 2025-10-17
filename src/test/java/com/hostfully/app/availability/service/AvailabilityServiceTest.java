package com.hostfully.app.availability.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.hostfully.app.block.domain.Block;
import com.hostfully.app.block.exceptions.OverlapBlockException;
import com.hostfully.app.infra.exception.InvalidDateRangeException;
import com.hostfully.app.infra.repository.BlockRepository;
import com.hostfully.app.infra.repository.BookingRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AvailabilityServiceTest {

    private final BlockRepository blockRepository = mock(BlockRepository.class);
    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final AvailabilityService availabilityService = new AvailabilityService(blockRepository, bookingRepository);

    private final String property = "prop-001";
    private final LocalDate startDate = LocalDate.of(2025, 1, 1);
    private final LocalDate endDate = LocalDate.of(2025, 1, 10);
    private final String bookingId = "a-book-id";

    @Test
    @DisplayName("should confirm availability when the property has no overlapping bookings and blocks")
    void shouldConfirmAvailabilityBookingBlocks() {
        when(bookingRepository.hasOverlapping(property, startDate, endDate, bookingId))
                .thenReturn(false);
        when(blockRepository.hasOverlapping(property, startDate, endDate, null)).thenReturn(false);

        assertTrue(availabilityService.canBook(startDate, endDate, property, bookingId));
    }

    @Test
    @DisplayName("should no confirm availability when the property has overlapping bookings")
    void shouldNoConfirmAvailabilityBooking() {
        when(bookingRepository.hasOverlapping(property, startDate, endDate, bookingId))
                .thenReturn(true);
        when(blockRepository.hasOverlapping(property, startDate, endDate, null)).thenReturn(false);

        assertFalse(availabilityService.canBook(startDate, endDate, property, bookingId));
    }

    @Test
    @DisplayName("should no confirm availability when the property has overlapping blocks")
    void shouldNoConfirmAvailabilityBlocks() {
        when(bookingRepository.hasOverlapping(property, startDate, endDate, bookingId))
                .thenReturn(false);
        when(blockRepository.hasOverlapping(property, startDate, endDate, null)).thenReturn(true);

        assertFalse(availabilityService.canBook(startDate, endDate, property, bookingId));
    }

    @Test
    @DisplayName("should return true when start date is before end date and no overlaps exist")
    void shouldReturnTrueWhenDateRangeIsValidAndNoOverlap() {
        final LocalDate startDate = LocalDate.of(2025, 1, 1);
        final LocalDate endDate = LocalDate.of(2025, 1, 10);
        final Block block = new Block("block-id", "prop-001", "a reason", startDate, endDate);

        when(blockRepository.hasOverlapping(block.getPropertyId(), startDate, endDate, null))
                .thenReturn(false);

        final Boolean result = availabilityService.hasValidDateRange(block, false);

        assertTrue(result);
        verify(blockRepository, times(1)).hasOverlapping(block.getPropertyId(), startDate, endDate, null);
    }

    @Test
    @DisplayName("should throw InvalidDateRangeException when start date is after end date")
    void shouldThrowInvalidDateRangeExceptionWhenStartDateAfterEndDate() {
        final LocalDate startDate = LocalDate.of(2025, 2, 1);
        final LocalDate endDate = LocalDate.of(2025, 1, 1);
        final Block block = new Block("block-id", "prop-001", "a reason", startDate, endDate);

        assertThrows(InvalidDateRangeException.class, () -> availabilityService.hasValidDateRange(block, false));

        verify(blockRepository, times(0)).hasOverlapping(block.getPropertyId(), startDate, endDate, null);
    }

    @Test
    @DisplayName("should allow equal start and end date (single-day block)")
    void shouldAllowEqualStartAndEndDate() {
        final LocalDate sameDate = LocalDate.of(2025, 5, 5);
        final Block block = new Block("block-id", "prop-001", "a reason", sameDate, sameDate);

        when(blockRepository.hasOverlapping(block.getPropertyId(), sameDate, sameDate, null))
                .thenReturn(false);

        final Boolean result = availabilityService.hasValidDateRange(block, false);

        assertTrue(result);
        verify(blockRepository, times(1)).hasOverlapping(block.getPropertyId(), sameDate, sameDate, null);
    }

    @Test
    @DisplayName("should throw OverlapBlockException when overlapping block exists in repository")
    void shouldThrowOverlapBlockExceptionWhenOverlapExists() {
        final LocalDate startDate = LocalDate.of(2025, 3, 1);
        final LocalDate endDate = LocalDate.of(2025, 3, 5);
        final Block block = new Block("block-id", "prop-001", "a reason", startDate, endDate);

        when(blockRepository.hasOverlapping(block.getPropertyId(), startDate, endDate, null))
                .thenReturn(true);

        assertThrows(OverlapBlockException.class, () -> availabilityService.hasValidDateRange(block, false));

        verify(blockRepository, times(1)).hasOverlapping(block.getPropertyId(), startDate, endDate, null);
    }
}
