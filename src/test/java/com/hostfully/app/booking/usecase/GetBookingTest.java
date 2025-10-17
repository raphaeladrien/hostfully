package com.hostfully.app.booking.usecase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.exception.BookingGenericException;
import com.hostfully.app.booking.exception.BookingNotFoundException;
import com.hostfully.app.infra.entity.BookingEntity;
import com.hostfully.app.infra.entity.BookingEntity.BookingStatus;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.BookingRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.QueryTimeoutException;

public class GetBookingTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final GetBooking subject = new GetBooking(bookingRepository);
    private final PropertyEntity property = mock(PropertyEntity.class);

    private final String guest = "Daenerys Targaryen";
    private final int numberGuest = 4;
    private final LocalDate startDate = LocalDate.now();
    private final LocalDate endDate = LocalDate.now().plusDays(2);
    final String externalId = "a-id-spec";

    @BeforeEach
    void setup() {
        when(property.getExternalId()).thenReturn("PROP-0001");
    }

    @Test
    @DisplayName("should return Booking, when a ID is provided")
    void shouldDeleteABlock() {
        final Booking expectedBooking = buildBooking();
        when(bookingRepository.findByExternalId(externalId)).thenReturn(Optional.of(buildBookingEntity()));

        final Booking result = subject.execute(externalId);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(result).isNotNull();
            softAssertions.assertThat(result).usingRecursiveComparison().isEqualTo(expectedBooking);
        });
    }

    @Test
    @DisplayName("should throw BookingNotFoundException, when booking isn't found by id")
    void shouldThrowBookingNotFoundException() {
        when(bookingRepository.findByExternalId(externalId)).thenReturn(Optional.empty());
        Assertions.assertThrows(BookingNotFoundException.class, () -> subject.execute(externalId));
    }

    @Test
    @DisplayName("should throw BookingGenericException, when unexpected error occurred")
    void shouldThrowBookingGenericException() {
        when(bookingRepository.findByExternalId(externalId)).thenThrow(new QueryTimeoutException("error"));
        Assertions.assertThrows(BookingGenericException.class, () -> subject.execute(externalId));
    }

    private BookingEntity buildBookingEntity() {
        return new BookingEntity(externalId, property, guest, numberGuest, BookingStatus.CONFIRMED, startDate, endDate);
    }

    private Booking buildBooking() {
        return new Booking(
                externalId,
                property.getExternalId(),
                startDate,
                endDate,
                guest,
                numberGuest,
                BookingStatus.CONFIRMED.name());
    }
}
