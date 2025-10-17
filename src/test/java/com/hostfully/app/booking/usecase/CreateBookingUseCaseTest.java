package com.hostfully.app.booking.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.hostfully.app.availability.service.AvailabilityService;
import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.exception.BookingGenericException;
import com.hostfully.app.booking.exception.OverlapBookingException;
import com.hostfully.app.booking.usecase.CreateBookingUseCase.CreateBookingCommand;
import com.hostfully.app.infra.entity.BookingEntity;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.exception.PropertyNotFoundException;
import com.hostfully.app.infra.repository.BookingRepository;
import com.hostfully.app.infra.repository.PropertyRepository;
import com.hostfully.app.shared.IdempotencyService;
import com.hostfully.app.shared.util.NanoIdGenerator;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CreateBookingUseCaseTest {

    private final IdempotencyService idempotencyService = mock(IdempotencyService.class);
    private final NanoIdGenerator nanoIdGenerator = mock(NanoIdGenerator.class);

    private final AvailabilityService availabilityService = mock(AvailabilityService.class);
    private final PropertyRepository propertyRepository = mock(PropertyRepository.class);
    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final CreateBookingUseCase subject = new CreateBookingUseCase(
            idempotencyService, nanoIdGenerator, availabilityService, propertyRepository, bookingRepository);

    @Test
    @DisplayName("should return existing booking when idempotency key already exists")
    void shouldReturnExistingBookingWhenIdempotencyKeyAlreadyExists() {
        final UUID idempotencyKey = UUID.randomUUID();
        final Booking existingBooking = createBooking("booking-123", "property-1", "Joe Doe", 2);
        final CreateBookingCommand command = createCommand(idempotencyKey, "property-1");

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.of(existingBooking));

        final Booking result = subject.execute(command);

        assertEquals(existingBooking, result);
        verify(idempotencyService).getResponse(idempotencyKey, Booking.class);
        verifyNoInteractions(nanoIdGenerator, availabilityService, propertyRepository, bookingRepository);
    }

    @Test
    @DisplayName("should create new booking")
    void shouldCreateNewBookingWhenAllValidationsPass() {
        final UUID idempotencyKey = UUID.randomUUID();
        final String generatedId = "booking-123";
        final String propertyId = "property-1";
        final LocalDate startDate = LocalDate.of(2025, 10, 20);
        final LocalDate endDate = LocalDate.of(2025, 10, 25);
        final String guestName = "Daenerys Targaryen";
        final int guestNumber = 4;

        final CreateBookingCommand command =
                new CreateBookingCommand(propertyId, startDate, endDate, guestName, guestNumber, idempotencyKey);

        final PropertyEntity propertyEntity = new PropertyEntity(propertyId, "description", "cozy place");

        final BookingEntity bookingEntity = new BookingEntity(
                generatedId,
                propertyEntity,
                guestName,
                guestNumber,
                BookingEntity.BookingStatus.CONFIRMED,
                startDate,
                endDate);
        final Booking expectedBooking = createBooking(generatedId, propertyId, guestName, guestNumber);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(nanoIdGenerator.generateId()).thenReturn(generatedId);
        when(propertyRepository.findByExternalId(propertyId)).thenReturn(Optional.of(propertyEntity));
        when(availabilityService.canBook(startDate, endDate, propertyId, generatedId))
                .thenReturn(true);
        when(bookingRepository.save(any(BookingEntity.class))).thenReturn(bookingEntity);

        final Booking result = subject.execute(command);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(result).isNotNull();
            softAssertions.assertThat(result).usingRecursiveComparison().isEqualTo(expectedBooking);
        });

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Booking.class);
        verify(nanoIdGenerator, times(1)).generateId();
        verify(availabilityService, times(1)).canBook(startDate, endDate, propertyId, generatedId);
        verify(propertyRepository, times(1)).findByExternalId(propertyId);
        verify(bookingRepository, times(1)).save(any(BookingEntity.class));
        verify(idempotencyService, times(1)).saveResponse(eq(idempotencyKey), any());
    }

    @ParameterizedTest
    @MethodSource("invalidRanges")
    @DisplayName("should throw InvalidDateRangeException when start date is after end date")
    void shouldThrowInvalidDateRangeExceptionWhenStartDateIsAfterEndDate(
            final LocalDate startDate, final LocalDate endDate) {
        final UUID idempotencyKey = UUID.randomUUID();
        CreateBookingCommand command =
                new CreateBookingCommand("property-1", startDate, endDate, "John Doe", 2, idempotencyKey);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(nanoIdGenerator.generateId()).thenReturn("booking-123");

        Assertions.assertThrows(RuntimeException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Booking.class);
        verify(nanoIdGenerator, times(1)).generateId();
        verify(idempotencyService, times(0)).saveResponse(idempotencyKey, Booking.class);
        verifyNoInteractions(availabilityService, propertyRepository, bookingRepository);
    }

    @Test
    @DisplayName("should throw OverlapBookingException when exits booking for the timeframe provided")
    void shouldThrowBlockGenericExceptionWhenDatabaseSaveFails() {
        final LocalDate startDate = LocalDate.of(2025, 10, 20);
        final LocalDate endDate = LocalDate.of(2025, 10, 25);
        final UUID idempotencyKey = UUID.randomUUID();
        final String property = "property-1";
        final String bookingId = "booking-123";
        CreateBookingCommand command =
                new CreateBookingCommand(property, startDate, endDate, "John Doe", 2, idempotencyKey);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(nanoIdGenerator.generateId()).thenReturn(bookingId);
        when(availabilityService.canBook(startDate, endDate, property, bookingId))
                .thenReturn(false);

        Assertions.assertThrows(OverlapBookingException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Booking.class);
        verify(nanoIdGenerator, times(1)).generateId();
        verify(availabilityService, times(1)).canBook(startDate, endDate, property, bookingId);
        verify(idempotencyService, times(0)).saveResponse(idempotencyKey, Booking.class);
        verifyNoInteractions(propertyRepository, bookingRepository);
    }

    @Test
    @DisplayName("should throw PropertyNotFoundException when property does not exist")
    void shouldThrowPropertyNotFoundExceptionWhenPropertyDoesNotExist() {
        final LocalDate startDate = LocalDate.of(2025, 10, 20);
        final LocalDate endDate = LocalDate.of(2025, 10, 25);
        final UUID idempotencyKey = UUID.randomUUID();
        final String property = "property-1";
        final String bookingId = "booking-123";
        CreateBookingCommand command =
                new CreateBookingCommand(property, startDate, endDate, "John Doe", 2, idempotencyKey);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(nanoIdGenerator.generateId()).thenReturn(bookingId);
        when(availabilityService.canBook(startDate, endDate, property, bookingId))
                .thenReturn(true);
        when(propertyRepository.findByExternalId(property)).thenReturn(Optional.empty());

        Assertions.assertThrows(PropertyNotFoundException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Booking.class);
        verify(nanoIdGenerator, times(1)).generateId();
        verify(availabilityService, times(1)).canBook(startDate, endDate, property, bookingId);
        verify(propertyRepository, times(1)).findByExternalId(property);
        verify(idempotencyService, times(0)).saveResponse(idempotencyKey, Booking.class);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    @DisplayName("should throw BookingGenericException when unexpected exception occurred")
    void shouldThrowBookingGenericExceptionWhenUnexpectedException() {
        final UUID idempotencyKey = UUID.randomUUID();
        final String generatedId = "booking-123";
        final String propertyId = "property-1";
        final LocalDate startDate = LocalDate.of(2025, 10, 20);
        final LocalDate endDate = LocalDate.of(2025, 10, 25);
        final String guestName = "Daenerys Targaryen";
        final int guestNumber = 4;

        final CreateBookingCommand command =
                new CreateBookingCommand(propertyId, startDate, endDate, guestName, guestNumber, idempotencyKey);

        final PropertyEntity propertyEntity = new PropertyEntity(propertyId, "description", "cozy place");

        final BookingEntity bookingEntity = new BookingEntity(
                generatedId,
                propertyEntity,
                guestName,
                guestNumber,
                BookingEntity.BookingStatus.CONFIRMED,
                startDate,
                endDate);
        final Booking expectedBooking = createBooking(generatedId, propertyId, guestName, guestNumber);

        when(idempotencyService.getResponse(idempotencyKey, Booking.class)).thenReturn(Optional.empty());
        when(nanoIdGenerator.generateId()).thenReturn(generatedId);
        when(propertyRepository.findByExternalId(propertyId)).thenReturn(Optional.of(propertyEntity));
        when(availabilityService.canBook(startDate, endDate, propertyId, generatedId))
                .thenReturn(true);
        when(bookingRepository.save(any())).thenThrow(RuntimeException.class);

        Assertions.assertThrows(BookingGenericException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Booking.class);
        verify(nanoIdGenerator, times(1)).generateId();
        verify(availabilityService, times(1)).canBook(startDate, endDate, propertyId, generatedId);
        verify(propertyRepository, times(1)).findByExternalId(propertyId);
        verify(bookingRepository, times(1)).save(any());
        verify(idempotencyService, times(0)).saveResponse(idempotencyKey, Booking.class);
    }

    private static Stream<Arguments> invalidRanges() {
        return Stream.of(
                arguments(LocalDate.of(2025, 1, 10), LocalDate.of(2025, 1, 2)),
                arguments(LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 5)));
    }

    private CreateBookingCommand createCommand(UUID idempotencyKey, String propertyId) {
        return new CreateBookingCommand(
                propertyId, LocalDate.of(2025, 10, 20), LocalDate.of(2025, 10, 25), "John Doe", 2, idempotencyKey);
    }

    private Booking createBooking(String id, String propertyId, String guestName, int numberGuest) {
        return new Booking(
                id,
                propertyId,
                LocalDate.of(2025, 10, 20),
                LocalDate.of(2025, 10, 25),
                guestName,
                numberGuest,
                BookingEntity.BookingStatus.CONFIRMED.name());
    }
}
