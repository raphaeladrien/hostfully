package com.hostfully.app.booking.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

import com.hostfully.app.availability.service.AvailabilityService;
import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.exception.BookingGenericException;
import com.hostfully.app.booking.exception.BookingNotFoundException;
import com.hostfully.app.booking.exception.OverlapBookingException;
import com.hostfully.app.booking.exception.RebookNotAllowedException;
import com.hostfully.app.booking.usecase.RebookBooking.RebookCommand;
import com.hostfully.app.infra.entity.BookingEntity;
import com.hostfully.app.infra.entity.BookingEntity.BookingStatus;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.exception.InvalidDateRangeException;
import com.hostfully.app.infra.repository.BookingRepository;
import com.hostfully.app.shared.IdempotencyService;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.QueryTimeoutException;

public class RebookBookingTest {

    private final IdempotencyService idempotencyService = mock(IdempotencyService.class);
    private final AvailabilityService availabilityService = mock(AvailabilityService.class);
    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final PropertyEntity property = mock(PropertyEntity.class);

    private final String id = "booking-123";
    private final String propertyId = "property-1";
    private final String guest = "Joe Doe";
    private final int numberGuest = 2;
    private final LocalDate startDate = LocalDate.of(2025, 10, 20);
    private final LocalDate endDate = LocalDate.of(2025, 10, 25);

    private final RebookBooking subject = new RebookBooking(idempotencyService, availabilityService, bookingRepository);

    @BeforeEach
    void setup() {
        when(property.getExternalId()).thenReturn(propertyId);
    }

    @Test
    @DisplayName("should return existing booking when idempotency key already exists")
    void shouldReturnExistingBookingWhenIdempotencyKeyAlreadyExists() {
        final UUID idempotencyKey = UUID.randomUUID();
        final Booking existingBooking = createBooking(BookingStatus.CONFIRMED.name());
        final RebookCommand command = createCommand(idempotencyKey, startDate, endDate);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.of(existingBooking));

        final Booking result = subject.execute(command);

        assertEquals(existingBooking, result);
        verify(idempotencyService).getResponse(idempotencyKey, Booking.class);
        verifyNoInteractions(availabilityService, bookingRepository);
    }

    @Test
    @DisplayName("should rebook the booking")
    void shouldRebookBooking() {
        final UUID idempotencyKey = UUID.randomUUID();
        final RebookCommand command = createCommand(idempotencyKey, startDate, endDate);
        final Booking expectedBooking = createBooking(BookingStatus.CONFIRMED.name());

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(bookingRepository.findByExternalId(id))
                .thenReturn(Optional.of(createBookingEntity(BookingStatus.CANCELLED)))
                .thenReturn(Optional.of(createBookingEntity(BookingStatus.CONFIRMED)));
        when(availabilityService.canBook(startDate, endDate, propertyId, id)).thenReturn(true);
        when(bookingRepository.updateStatusAndTimeframe(BookingStatus.CONFIRMED, startDate, endDate, id))
                .thenReturn(1);

        final Booking result = subject.execute(command);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(result).isNotNull();
            softAssertions.assertThat(result).usingRecursiveComparison().isEqualTo(expectedBooking);
        });

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Booking.class);
        verify(bookingRepository, times(2)).findByExternalId(id);
        verify(availabilityService, times(1)).canBook(startDate, endDate, propertyId, id);
        verify(bookingRepository, times(1)).updateStatusAndTimeframe(BookingStatus.CONFIRMED, startDate, endDate, id);
        verify(idempotencyService, times(1)).saveResponse(idempotencyKey, result);
    }

    @Test
    @DisplayName("should throw BookingNotFoundException, when booking isn't found by id")
    void shouldThrowBookingNotFoundException() {
        final UUID idempotencyKey = UUID.randomUUID();
        final RebookCommand command = createCommand(idempotencyKey, startDate, endDate);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(bookingRepository.findByExternalId(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(BookingNotFoundException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Booking.class);
        verify(bookingRepository, times(1)).findByExternalId(id);

        verifyNoInteractions(availabilityService);
        verifyNoMoreInteractions(bookingRepository, idempotencyService);
    }

    @Test
    @DisplayName("should throw ReebokNotAllowedException, when booking isn't cancelled")
    void shouldThrowReebokNotAllowedException() {
        final UUID idempotencyKey = UUID.randomUUID();
        final RebookCommand command = createCommand(idempotencyKey, startDate, endDate);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(bookingRepository.findByExternalId(id))
                .thenReturn(Optional.of(createBookingEntity(BookingStatus.CONFIRMED)));

        Assertions.assertThrows(RebookNotAllowedException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Booking.class);
        verify(bookingRepository, times(1)).findByExternalId(id);

        verifyNoInteractions(availabilityService);
        verifyNoMoreInteractions(bookingRepository, idempotencyService);
    }

    @ParameterizedTest
    @MethodSource("invalidRanges")
    @DisplayName("should throw InvalidDateRangeException, when invalid timeframe is provided")
    void shouldThrowInvalidDateRangeException(LocalDate startDate, LocalDate endDate) {
        final UUID idempotencyKey = UUID.randomUUID();
        final RebookCommand command = createCommand(idempotencyKey, startDate, endDate);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(bookingRepository.findByExternalId(id))
                .thenReturn(Optional.of(createBookingEntity(BookingStatus.CANCELLED)));

        Assertions.assertThrows(InvalidDateRangeException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Booking.class);
        verify(bookingRepository, times(1)).findByExternalId(id);

        verifyNoInteractions(availabilityService);
        verifyNoMoreInteractions(bookingRepository, idempotencyService);
    }

    @Test
    @DisplayName("should throw OverlapBookingException, when exits booking for the timeframe provided")
    void shouldThrowOverlapBookingException() {
        final UUID idempotencyKey = UUID.randomUUID();
        final RebookCommand command = createCommand(idempotencyKey, startDate, endDate);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(bookingRepository.findByExternalId(id))
                .thenReturn(Optional.of(createBookingEntity(BookingStatus.CANCELLED)));
        when(availabilityService.canBook(startDate, endDate, propertyId, id)).thenReturn(false);

        Assertions.assertThrows(OverlapBookingException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Booking.class);
        verify(bookingRepository, times(1)).findByExternalId(id);
        verify(availabilityService, times(1)).canBook(startDate, endDate, propertyId, id);

        verifyNoMoreInteractions(bookingRepository, idempotencyService);
    }

    @Test
    @DisplayName("should throw BookingGenericException when DataAccessException occurs during booking retrieval")
    void shouldThrowBookingGenericException() {
        final UUID idempotencyKey = UUID.randomUUID();
        final RebookCommand command = createCommand(idempotencyKey, startDate, endDate);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(bookingRepository.findByExternalId(id)).thenThrow(new QueryTimeoutException("error"));

        Assertions.assertThrows(BookingGenericException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Booking.class);
        verify(bookingRepository, times(1)).findByExternalId(id);

        verifyNoMoreInteractions(bookingRepository, idempotencyService);
        verifyNoInteractions(availabilityService);
    }

    private static Stream<Arguments> invalidRanges() {
        return Stream.of(
                arguments(LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 2)),
                arguments(LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 5)));
    }

    private Booking createBooking(String status) {
        return new Booking(
                id, propertyId, LocalDate.of(2025, 10, 20), LocalDate.of(2025, 10, 25), guest, numberGuest, status);
    }

    private BookingEntity createBookingEntity(BookingStatus status) {
        return new BookingEntity(id, property, guest, numberGuest, status, startDate, endDate);
    }

    private RebookCommand createCommand(final UUID idempotencyKey, final LocalDate startDate, final LocalDate endDate) {
        return new RebookCommand(id, startDate, endDate, idempotencyKey);
    }
}
