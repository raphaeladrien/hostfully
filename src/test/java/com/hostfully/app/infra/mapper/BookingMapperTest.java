package com.hostfully.app.infra.mapper;

import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.infra.entity.BookingEntity;
import com.hostfully.app.infra.entity.BookingEntity.BookingStatus;
import com.hostfully.app.infra.entity.PropertyEntity;
import java.time.LocalDate;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BookingMapperTest {

    @Test
    @DisplayName("should map property domain to entity")
    void shouldMapPropertyToEntity() {
        final String propertyId = "asert-mnbvcx";
        final String id = "book-id";
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = LocalDate.now();
        final String guestName = "Tyrion Lannister";
        final Integer numberOfGuest = 3;

        final Booking booking = new Booking(
                id, propertyId, startDate, endDate, guestName, numberOfGuest, BookingStatus.CONFIRMED.name());

        final PropertyEntity propertyEntity = new PropertyEntity(propertyId, "a-super-description", "a-alias");

        final BookingEntity result = BookingMapper.toEntity(booking, propertyEntity);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(result.getId()).isNull();
            softAssertions.assertThat(result.getExternalId()).isEqualTo(id);
            softAssertions.assertThat(result.getProperty().getExternalId()).isEqualTo(propertyId);
            softAssertions.assertThat(result.getGuest()).isEqualTo(guestName);
            softAssertions.assertThat(result.getNumberGuest()).isEqualTo(numberOfGuest);
            softAssertions.assertThat(result.getStartDate()).isEqualTo(startDate);
            softAssertions.assertThat(result.getStartDate()).isEqualTo(endDate);
            softAssertions.assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        });
    }

    @Test
    @DisplayName("should map property entity to domain")
    void shouldMapEntityToDomain() {
        final String propertyId = "asert-mnbvcx";
        final String id = "book-id";
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = LocalDate.now();
        final String guestName = "Tyrion Lannister";
        final Integer numberOfGuest = 3;

        final PropertyEntity propertyEntity = new PropertyEntity(propertyId, "a-super-description", "a-alias");

        final BookingEntity bookingEntity = new BookingEntity(
                id, propertyEntity, guestName, numberOfGuest, BookingStatus.CONFIRMED, startDate, endDate);

        final Booking result = BookingMapper.toDomain(bookingEntity);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(result.getId()).isEqualTo(id);
            softAssertions.assertThat(result.getPropertyId()).isEqualTo(propertyId);
            softAssertions.assertThat(result.getGuestName()).isEqualTo(guestName);
            softAssertions.assertThat(result.getNumberGuest()).isEqualTo(numberOfGuest);
            softAssertions.assertThat(result.getStartDate()).isEqualTo(startDate);
            softAssertions.assertThat(result.getStartDate()).isEqualTo(endDate);
            softAssertions.assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED.name());
        });
    }
}
