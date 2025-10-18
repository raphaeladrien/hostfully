package com.hostfully.app.booking.usecase;

import com.hostfully.app.availability.service.AvailabilityService;
import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.exception.BookingGenericException;
import com.hostfully.app.booking.exception.BookingNotFoundException;
import com.hostfully.app.booking.exception.OverlapBookingException;
import com.hostfully.app.booking.exception.UpdateNotAllowedException;
import com.hostfully.app.infra.entity.BookingEntity;
import com.hostfully.app.infra.exception.InvalidDateRangeException;
import com.hostfully.app.infra.mapper.BookingMapper;
import com.hostfully.app.infra.repository.BookingRepository;
import com.hostfully.app.shared.util.DateRangeValidator;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateBooking {

    private static final Logger log = LoggerFactory.getLogger(UpdateBooking.class);

    private final BookingRepository bookingRepository;
    private final AvailabilityService availabilityService;

    @Transactional
    public Booking execute(final UpdateBookingCommand command) {
        final BookingEntity entity = getBooking(command.id);

        if (!canUpdate(entity.getStatus()))
            throw new UpdateNotAllowedException("Booking updates are allowed only when the booking is active");

        final Booking booking = buildDomain(entity, command);

        if (!DateRangeValidator.validateDateRange(booking.getStartDate(), booking.getEndDate(), false))
            throw new InvalidDateRangeException("The start date and end date must not be the same. "
                    + "The end date should be greater than the start date.");

        if (!availabilityService.canBook(
                booking.getStartDate(), booking.getEndDate(), booking.getPropertyId(), booking.getId()))
            throw new OverlapBookingException("We’re unable to process your booking for this property. "
                    + "Please refresh the page or try again later.");

        try {
            bookingRepository.updateStartDateEndGuestNumber(
                    booking.getStartDate(),
                    booking.getEndDate(),
                    booking.getGuestName(),
                    booking.getNumberGuest(),
                    booking.getId());

            return BookingMapper.toDomain(getBooking(booking.getId()));
        } catch (DataAccessException ex) {
            log.error("Failed to update a booking: {}", command.id, ex);
            throw new BookingGenericException("Unexpected error while updating booking", ex);
        }
    }

    private boolean canUpdate(BookingEntity.BookingStatus status) {
        return !status.isCancelled();
    }

    private Booking buildDomain(final BookingEntity entity, final UpdateBookingCommand command) {
        final LocalDate startDate = command.startDate != null ? command.startDate : entity.getStartDate();
        final LocalDate endDate = command.endDate != null ? command.endDate : entity.getEndDate();
        final String guest =
                (command.guestName != null && !command.guestName.isBlank()) ? command.guestName() : entity.getGuest();
        final int numberGuest = command.numberGuests > 0 ? command.numberGuests : entity.getNumberGuest();

        return new Booking(
                command.id,
                entity.getProperty().getExternalId(),
                startDate,
                endDate,
                guest,
                numberGuest,
                entity.getStatus().name());
    }

    private BookingEntity getBooking(String id) {
        return bookingRepository
                .findByExternalId(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
    }

    public record UpdateBookingCommand(
            String id, LocalDate startDate, LocalDate endDate, String guestName, int numberGuests) {}
}
