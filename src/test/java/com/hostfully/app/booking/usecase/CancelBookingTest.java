package com.hostfully.app.booking.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.exception.BookingGenericException;
import com.hostfully.app.booking.exception.BookingNotFoundException;
import com.hostfully.app.infra.entity.BookingEntity;
import com.hostfully.app.infra.entity.BookingEntity.BookingStatus;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.BookingRepository;
import com.hostfully.app.shared.IdempotencyService;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.QueryTimeoutException;

public class CancelBookingTest {

    private final IdempotencyService idempotencyService = mock(IdempotencyService.class);
    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final PropertyEntity property = mock(PropertyEntity.class);

    @BeforeEach
    void setup() {
        when(property.getExternalId()).thenReturn("PROP-0001");
    }

    private final CancelBooking subject = new CancelBooking(idempotencyService, bookingRepository);

    final String bookingId = "booking-123";
    final String propertyId = "PROP-0001";
    final String guestName = "Māui-tikitiki-a-Taranga";
    final int numberGuest = 3;
    final LocalDate startDate = LocalDate.of(2025, 10, 20);
    final LocalDate endDate = LocalDate.of(2025, 10, 25);

    @Test
    @DisplayName(
            "should return cached booking from idempotency service when response already exists for given idempotency key")
    void shouldReturnCachedBookingWhenIdempotencyKeyExists() {

        final UUID idempotencyKey = UUID.randomUUID();
        final Booking cachedBooking = createBooking(BookingStatus.CANCELLED);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.of(cachedBooking));

        Booking result = subject.execute(bookingId, idempotencyKey);

        assertNotNull(result);
        assertEquals(cachedBooking, result);
        verify(idempotencyService).getResponse(idempotencyKey, Booking.class);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    @DisplayName("should successfully cancel a booking and save response when booking is not already cancelled")
    void shouldCancelBookingAndSaveResponseWhenBookingIsNotCancelled() {
        final UUID idempotencyKey = UUID.randomUUID();
        final BookingEntity activeBooking = createBookingEntity(BookingStatus.CONFIRMED);
        final BookingEntity cancelledBooking = createBookingEntity(BookingStatus.CANCELLED);
        final Booking expectedBooking = createBooking(BookingStatus.CANCELLED);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(bookingRepository.findByExternalId(bookingId))
                .thenReturn(Optional.of(activeBooking))
                .thenReturn(Optional.of(cancelledBooking));

        final Booking result = subject.execute(bookingId, idempotencyKey);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(result).isNotNull();
            softAssertions.assertThat(result).usingRecursiveComparison().isEqualTo(expectedBooking);
        });

        verify(bookingRepository, times(1)).updateStatus(BookingStatus.CANCELLED, bookingId);
        verify(idempotencyService, times(1)).saveResponse(idempotencyKey, result);
    }

    @Test
    @DisplayName(
            "should not update booking status when booking is already cancelled but still save idempotency response")
    void shouldSkipStatusUpdateWhenBookingIsAlreadyCancelled() {
        final UUID idempotencyKey = UUID.randomUUID();
        final BookingEntity cancelledBooking = createBookingEntity(BookingStatus.CANCELLED);
        final Booking expectedBooking = createBooking(BookingStatus.CANCELLED);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(bookingRepository.findByExternalId(bookingId))
                .thenReturn(Optional.of(cancelledBooking))
                .thenReturn(Optional.of(cancelledBooking));

        final Booking result = subject.execute(bookingId, idempotencyKey);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(result).isNotNull();
            softAssertions.assertThat(result).usingRecursiveComparison().isEqualTo(expectedBooking);
        });

        verify(bookingRepository, times(0)).updateStatus(BookingStatus.CANCELLED, bookingId);
        verify(idempotencyService, times(1)).saveResponse(idempotencyKey, result);
    }

    @Test
    @DisplayName("should throw BookingGenericException when DataAccessException occurs during booking retrieval")
    void shouldThrowBookingGenericExceptionWhenDataAccessExceptionOccurs() {
        final UUID idempotencyKey = UUID.randomUUID();
        final BookingEntity cancelledBooking = createBookingEntity(BookingStatus.CANCELLED);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(bookingRepository.findByExternalId(bookingId))
                .thenReturn(Optional.of(cancelledBooking))
                .thenThrow(new QueryTimeoutException("error"));

        Assertions.assertThrows(BookingGenericException.class, () -> subject.execute(bookingId, idempotencyKey));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Booking.class);
        verify(bookingRepository, times(2)).findByExternalId(bookingId);
        verifyNoMoreInteractions(bookingRepository, idempotencyService);
    }

    @Test
    @DisplayName("should throw BookingNotFoundException when booking isn't found by id")
    void shouldThrowBookingNotFoundExceptionWhenBookingIsNotFound() {
        final UUID idempotencyKey = UUID.randomUUID();
        final BookingEntity cancelledBooking = createBookingEntity(BookingStatus.CANCELLED);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(bookingRepository.findByExternalId(bookingId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());

        Assertions.assertThrows(BookingNotFoundException.class, () -> subject.execute(bookingId, idempotencyKey));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Booking.class);
        verify(bookingRepository, times(1)).findByExternalId(bookingId);
        verifyNoMoreInteractions(bookingRepository, idempotencyService);
    }

    private Booking createBooking(final BookingStatus status) {
        return new Booking(bookingId, propertyId, startDate, endDate, guestName, numberGuest, status.name());
    }

    private BookingEntity createBookingEntity(final BookingStatus status) {
        return new BookingEntity(bookingId, property, guestName, numberGuest, status, startDate, endDate);
    }
}
