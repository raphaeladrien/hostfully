package com.hostfully.app.infra.mapper;

import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.infra.entity.BookingEntity;
import com.hostfully.app.infra.entity.BookingEntity.BookingStatus;
import com.hostfully.app.infra.entity.PropertyEntity;

public abstract class BookingMapper {

    public static BookingEntity toEntity(final Booking booking, final PropertyEntity propertyEntity) {
        final BookingStatus status = BookingStatus.fromString(booking.getStatus());
        return new BookingEntity(
                booking.getId(),
                propertyEntity,
                booking.getGuestName(),
                booking.getNumberGuest(),
                status,
                booking.getStartDate(),
                booking.getEndDate());
    }

    public static Booking toDomain(final BookingEntity bookingEntity) {
        return new Booking(
                bookingEntity.getExternalId(),
                bookingEntity.getProperty().getExternalId(),
                bookingEntity.getStartDate(),
                bookingEntity.getEndDate(),
                bookingEntity.getGuest(),
                bookingEntity.getNumberGuest(),
                bookingEntity.getStatus().name());
    }
}
