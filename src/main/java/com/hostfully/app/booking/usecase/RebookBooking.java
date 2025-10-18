package com.hostfully.app.booking.usecase;

import com.hostfully.app.availability.service.AvailabilityService;
import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.exception.BookingGenericException;
import com.hostfully.app.booking.exception.BookingNotFoundException;
import com.hostfully.app.booking.exception.OverlapBookingException;
import com.hostfully.app.booking.exception.RebookNotAllowedException;
import com.hostfully.app.infra.entity.BookingEntity;
import com.hostfully.app.infra.exception.InvalidDateRangeException;
import com.hostfully.app.infra.mapper.BookingMapper;
import com.hostfully.app.infra.repository.BookingRepository;
import com.hostfully.app.shared.IdempotencyService;
import com.hostfully.app.shared.util.DateRangeValidator;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RebookBooking {

    private static final Logger log = LoggerFactory.getLogger(RebookBooking.class);

    private final IdempotencyService idempotencyService;
    private final AvailabilityService availabilityService;
    private final BookingRepository bookingRepository;

    @Transactional
    public Booking execute(final RebookCommand command) {
        final UUID idempotencyKey = command.idempotencyKey;
        final Optional<Booking> result = idempotencyService.getResponse(idempotencyKey, Booking.class);
        if (result.isPresent()) return result.get();

        try {
            final BookingEntity bookingEntity = getBooking(command.id);
            final Booking booking = buildDomain(command, bookingEntity);

            if (!canRebook(booking.getStatus()))
                throw new RebookNotAllowedException("Booking cannot be rebooked unless it is cancelled.");

            if (!DateRangeValidator.validateDateRange(booking.getStartDate(), booking.getEndDate(), false))
                throw new InvalidDateRangeException("The start date and end date must not be the same. "
                        + "The end date should be greater than the start date.");

            if (!availabilityService.canBook(
                    booking.getStartDate(), booking.getEndDate(), booking.getPropertyId(), booking.getId()))
                throw new OverlapBookingException("We’re unable to process your booking for this property. "
                        + "Please refresh the page or try again later.");

            bookingRepository.updateStatusAndTimeframe(
                    BookingEntity.BookingStatus.CONFIRMED,
                    booking.getStartDate(),
                    booking.getEndDate(),
                    booking.getId());

            final Booking bookingResult = BookingMapper.toDomain(getBooking(booking.getId()));
            idempotencyService.saveResponse(idempotencyKey, bookingResult);
            return bookingResult;
        } catch (DataAccessException ex) {
            log.error("Failed to rebook a booking: {}", command.id, ex);
            throw new BookingGenericException("Unexpected error while rebooking booking", ex);
        }
    }

    private boolean canRebook(final String status) {
        return "CANCELLED".equals(status);
    }

    private Booking buildDomain(final RebookCommand command, final BookingEntity bookingEntity) {
        return new Booking(
                command.id,
                bookingEntity.getProperty().getExternalId(),
                command.startDate,
                command.endDate,
                bookingEntity.getGuest(),
                bookingEntity.getNumberGuest(),
                bookingEntity.getStatus().name());
    }

    private BookingEntity getBooking(String id) {
        return bookingRepository
                .findByExternalId(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
    }

    public record RebookCommand(String id, LocalDate startDate, LocalDate endDate, UUID idempotencyKey) {}
}
