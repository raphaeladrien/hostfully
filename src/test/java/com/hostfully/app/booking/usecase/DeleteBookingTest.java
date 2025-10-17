package com.hostfully.app.booking.usecase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hostfully.app.block.exceptions.BlockGenericException;
import com.hostfully.app.infra.repository.BookingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DeleteBookingTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final DeleteBooking subject = new DeleteBooking(bookingRepository);

    @Test
    @DisplayName("should delete a booking, when a ID is provided")
    void shouldDeleteBooking() {
        final String externalId = "a-id-spec";
        when(bookingRepository.deleteByExternalId(externalId)).thenReturn(1);
        Assertions.assertTrue(subject.execute(externalId));
    }

    @Test
    @DisplayName("should return false, when no record is found")
    void shouldReturnFalse() {
        final String externalId = "a-id-spec";
        when(bookingRepository.deleteByExternalId(externalId)).thenReturn(0);
        Assertions.assertFalse(subject.execute(externalId));
    }

    @Test
    @DisplayName("throws BlockGenericException, when an unexpected exception occurred")
    void throwsBlockGenericException() {
        final String externalId = "a-id-spec";
        when(bookingRepository.deleteByExternalId(externalId)).thenThrow(new RuntimeException("an-error"));
        Assertions.assertThrows(BlockGenericException.class, () -> subject.execute(externalId));
    }
}
