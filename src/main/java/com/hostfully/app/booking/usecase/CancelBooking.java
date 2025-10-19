package com.hostfully.app.booking.usecase;

import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.exception.BookingGenericException;
import com.hostfully.app.booking.exception.BookingNotFoundException;
import com.hostfully.app.infra.entity.BookingEntity;
import com.hostfully.app.infra.entity.BookingEntity.BookingStatus;
import com.hostfully.app.infra.mapper.BookingMapper;
import com.hostfully.app.infra.repository.BookingRepository;
import com.hostfully.app.shared.IdempotencyService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CancelBooking {

    private static final Logger log = LoggerFactory.getLogger(CancelBooking.class);

    private final IdempotencyService idempotencyService;
    private final BookingRepository bookingRepository;

    @Transactional
    public Booking execute(final String id, final UUID idempotencyKey) {
        final Optional<Booking> result = idempotencyService.getResponse(idempotencyKey, Booking.class);
        if (result.isPresent()) return result.get();

        try {
            BookingEntity booking = getBooking(id);

            if (!booking.getStatus().isCancelled()) {
                bookingRepository.updateStatus(BookingStatus.CANCELLED, id);
            }

            final Booking bookingResult = BookingMapper.toDomain(getBooking(id));
            idempotencyService.saveResponse(idempotencyKey, bookingResult);
            return bookingResult;
        } catch (DataAccessException ex) {
            log.error("Failed to retrieve a booking: {}", id, ex);
            throw new BookingGenericException("Unexpected error while canceling booking", ex);
        }
    }

    private BookingEntity getBooking(String id) {
        return bookingRepository
                .findByExternalId(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
    }
}
