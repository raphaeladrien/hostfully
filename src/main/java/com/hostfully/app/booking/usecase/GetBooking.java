package com.hostfully.app.booking.usecase;

import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.exception.BookingGenericException;
import com.hostfully.app.booking.exception.BookingNotFoundException;
import com.hostfully.app.infra.entity.BookingEntity;
import com.hostfully.app.infra.mapper.BookingMapper;
import com.hostfully.app.infra.repository.BookingRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetBooking {

    private static final Logger log = LoggerFactory.getLogger(GetBooking.class);

    private BookingRepository bookingRepository;

    public Booking execute(final String id) {
        try {
            final BookingEntity bookingEntity = bookingRepository
                    .findByExternalId(id)
                    .orElseThrow(() -> new BookingNotFoundException("The requested booking could not be found. "
                            + "Please verify the booking ID and try again."));
            return BookingMapper.toDomain(bookingEntity);
        } catch (DataAccessException ex) {
            log.error("Failed to retrieve a booking: {}", id, ex);
            throw new BookingGenericException("Unexpected error while retrieving booking", ex);
        }
    }
}
