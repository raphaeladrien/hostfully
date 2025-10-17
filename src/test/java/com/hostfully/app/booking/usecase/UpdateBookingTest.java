package com.hostfully.app.booking.usecase;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import com.hostfully.app.availability.service.AvailabilityService;
import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.exception.BookingGenericException;
import com.hostfully.app.booking.exception.OverlapBookingException;
import com.hostfully.app.booking.exception.UpdateNotAllowedException;
import com.hostfully.app.booking.usecase.UpdateBooking.UpdateBookingCommand;
import com.hostfully.app.infra.entity.BookingEntity;
import com.hostfully.app.infra.entity.BookingEntity.BookingStatus;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.BookingRepository;
import java.time.LocalDate;
import java.util.Optional;
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

public class UpdateBookingTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final AvailabilityService availabilityService = mock(AvailabilityService.class);
    private final UpdateBooking subject = new UpdateBooking(bookingRepository, availabilityService);
    private final PropertyEntity property = mock(PropertyEntity.class);

    private final String id = "qweert-012";
    private final String guest = "Viserys I Targaryen";
    private final int numberGuest = 4;
    private final String propertyId = "prop-001";
    private final LocalDate startDate = LocalDate.now().plusDays(10);
    private final LocalDate endDate = LocalDate.now().plusDays(20);

    @BeforeEach
    void setup() {
        when(property.getExternalId()).thenReturn(propertyId);
    }

    @ParameterizedTest
    @MethodSource("params")
    @DisplayName("should successfully update booking when all conditions are met")
    void shouldUpdateBookingSuccessfully(LocalDate dateStart, LocalDate dateEnd, String guestName, int number) {
        final LocalDate usedStartDate = dateStart != null ? dateStart : startDate;
        final LocalDate usedEndDate = dateEnd != null ? dateEnd : endDate;
        final String usedGuest = (guestName != null && !guestName.isBlank()) ? guestName : guest;
        final int usedNumberGuest = number > 0 ? number : numberGuest;

        final Booking expectedBooking = createBooking(usedStartDate, usedEndDate, usedGuest, usedNumberGuest);
        final UpdateBookingCommand command = new UpdateBookingCommand(id, dateStart, dateEnd, guestName, number);
        final BookingEntity existingEntity =
                createBookingEntity(startDate, endDate, guest, numberGuest, BookingStatus.CONFIRMED);
        final BookingEntity updatedEntity =
                createBookingEntity(usedStartDate, usedEndDate, usedGuest, usedNumberGuest, BookingStatus.CONFIRMED);

        when(bookingRepository.findByExternalId(id))
                .thenReturn(Optional.of(existingEntity))
                .thenReturn(Optional.of(updatedEntity));
        when(availabilityService.canBook(usedStartDate, usedEndDate, propertyId, id))
                .thenReturn(true);

        final Booking result = subject.execute(command);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(result).isNotNull();
            softAssertions.assertThat(result).usingRecursiveComparison().isEqualTo(expectedBooking);
        });

        verify(bookingRepository)
                .updateStartDateEndGuestNumber(usedStartDate, usedEndDate, usedGuest, usedNumberGuest, id);
        verify(availabilityService, times(1)).canBook(usedStartDate, usedEndDate, propertyId, id);
        verify(bookingRepository, times(2)).findByExternalId(id);
        verify(bookingRepository, times(1))
                .updateStartDateEndGuestNumber(usedStartDate, usedEndDate, usedGuest, usedNumberGuest, id);
    }

    @Test
    @DisplayName("should throw UpdateNowAllowedException when booking status does not allow updates")
    void shouldThrowUpdateNowAllowedException() {
        final UpdateBookingCommand command =
                new UpdateBookingCommand(id, startDate, LocalDate.now().minusDays(10), guest, numberGuest);
        final BookingEntity existingEntity =
                createBookingEntity(startDate, endDate, guest, numberGuest, BookingStatus.CANCELLED);

        when(bookingRepository.findByExternalId(id)).thenReturn(Optional.of(existingEntity));

        Assertions.assertThrows(UpdateNotAllowedException.class, () -> subject.execute(command));

        verify(bookingRepository, times(1)).findByExternalId(id);
        verifyNoMoreInteractions(bookingRepository);
        verifyNoInteractions(availabilityService);
    }

    @Test
    @DisplayName("should throw InvalidDateRangeException when booking status does not allow updates")
    void shouldThrowInvalidDateRangeException() {
        final UpdateBookingCommand command = new UpdateBookingCommand(id, startDate, endDate, guest, numberGuest);
        final BookingEntity existingEntity =
                createBookingEntity(startDate, endDate, guest, numberGuest, BookingStatus.CANCELLED);

        when(bookingRepository.findByExternalId(id)).thenReturn(Optional.of(existingEntity));

        Assertions.assertThrows(UpdateNotAllowedException.class, () -> subject.execute(command));

        verify(bookingRepository, times(1)).findByExternalId(id);
        verifyNoMoreInteractions(bookingRepository);
        verifyNoInteractions(availabilityService);
    }

    @Test
    @DisplayName("should throw OverlapBookingException when property is not available for selected dates")
    void shouldThrowOverlapBookingException() {
        final UpdateBookingCommand command = new UpdateBookingCommand(id, startDate, endDate, guest, numberGuest);
        final BookingEntity existingEntity =
                createBookingEntity(startDate, endDate, guest, numberGuest, BookingStatus.CONFIRMED);

        when(bookingRepository.findByExternalId(id)).thenReturn(Optional.of(existingEntity));
        when(availabilityService.canBook(startDate, endDate, propertyId, id)).thenReturn(false);

        Assertions.assertThrows(OverlapBookingException.class, () -> subject.execute(command));

        verify(bookingRepository, times(1)).findByExternalId(id);
        verify(availabilityService, times(1)).canBook(startDate, endDate, propertyId, id);
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    @DisplayName("should throw BookingGenericException when unexpected exception occurred")
    void shouldThrowBookingGenericException() {
        final UpdateBookingCommand command = new UpdateBookingCommand(id, startDate, endDate, guest, numberGuest);
        final BookingEntity existingEntity =
                createBookingEntity(startDate, endDate, guest, numberGuest, BookingStatus.CONFIRMED);

        when(bookingRepository.findByExternalId(id)).thenReturn(Optional.of(existingEntity));
        when(availabilityService.canBook(startDate, endDate, propertyId, id)).thenReturn(true);
        when(bookingRepository.updateStartDateEndGuestNumber(startDate, endDate, guest, numberGuest, id))
                .thenThrow(new QueryTimeoutException("error"));

        Assertions.assertThrows(BookingGenericException.class, () -> subject.execute(command));

        verify(bookingRepository, times(1)).findByExternalId(id);
        verify(availabilityService, times(1)).canBook(startDate, endDate, propertyId, id);
        verify(bookingRepository, times(1)).updateStartDateEndGuestNumber(startDate, endDate, guest, numberGuest, id);
        verifyNoMoreInteractions(bookingRepository);
    }

    private static Stream<Arguments> params() {
        return Stream.of(
                arguments(null, null, null, 0),
                arguments(LocalDate.now(), null, null, 0),
                arguments(LocalDate.now(), LocalDate.now().plusDays(3), null, 0),
                arguments(LocalDate.now(), LocalDate.now().plusDays(3), "guest-1", 0),
                arguments(LocalDate.now(), LocalDate.now().plusDays(3), "guest-1", 5),
                arguments(LocalDate.now(), LocalDate.now().plusDays(3), null, 0),
                arguments(LocalDate.now(), LocalDate.now().plusDays(3), "guest-1", 0),
                arguments(null, null, "guest-1", 5),
                arguments(null, null, null, 5));
    }

    private Booking createBooking(LocalDate startDate, LocalDate endDate, String guestName, int number) {
        return new Booking(
                id, propertyId, startDate, endDate, guestName, number, BookingEntity.BookingStatus.CONFIRMED.name());
    }

    private BookingEntity createBookingEntity(
            LocalDate startDate, LocalDate endDate, String guestName, int number, BookingStatus status) {
        return new BookingEntity(id, property, guestName, number, status, startDate, endDate);
    }
}
