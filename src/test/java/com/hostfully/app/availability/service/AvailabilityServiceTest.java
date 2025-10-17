package com.hostfully.app.availability.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    @DisplayName("should confirm booking availability when the property has no overlapping bookings and blocks")
    void shouldConfirmAvailabilityBookingBlocks() {
        when(bookingRepository.hasOverlapping(property, startDate, endDate, bookingId))
                .thenReturn(false);
        when(blockRepository.hasOverlapping(property, startDate, endDate)).thenReturn(false);

        assertTrue(availabilityService.canBook(startDate, endDate, property, bookingId));
    }

    @Test
    @DisplayName("should no confirm booking availability when the property has overlapping bookings")
    void shouldNoConfirmAvailabilityBooking() {
        when(bookingRepository.hasOverlapping(property, startDate, endDate, bookingId))
                .thenReturn(true);
        when(blockRepository.hasOverlapping(property, startDate, endDate)).thenReturn(false);

        assertFalse(availabilityService.canBook(startDate, endDate, property, bookingId));
    }

    @Test
    @DisplayName("should no confirm booking availability when the property has overlapping blocks")
    void shouldNoConfirmAvailabilityBlocks() {
        when(bookingRepository.hasOverlapping(property, startDate, endDate, bookingId))
                .thenReturn(false);
        when(blockRepository.hasOverlapping(property, startDate, endDate)).thenReturn(true);

        assertFalse(availabilityService.canBook(startDate, endDate, property, bookingId));
    }

    @Test
    @DisplayName("should confirm block availability when the property has no overlapping bookings")
    void shouldConfirmAvailabilityBlocks() {
        when(bookingRepository.hasOverlapping(property, startDate, endDate, null))
                .thenReturn(false);

        assertTrue(availabilityService.canBlock(startDate, endDate, property));
    }

    @Test
    @DisplayName("should no confirm block availability when the property has overlapping bookings")
    void shouldNoConfirmAvailabilityBlock() {
        when(bookingRepository.hasOverlapping(property, startDate, endDate, null))
                .thenReturn(true);

        assertFalse(availabilityService.canBlock(startDate, endDate, property));
    }
}
