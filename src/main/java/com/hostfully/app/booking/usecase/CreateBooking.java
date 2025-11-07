package com.hostfully.app.booking.usecase;

import com.hostfully.app.availability.service.AvailabilityService;
import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.exception.BookingGenericException;
import com.hostfully.app.booking.exception.ConcurrentBookingException;
import com.hostfully.app.booking.exception.OverlapBookingException;
import com.hostfully.app.infra.entity.BookingEntity.BookingStatus;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.exception.InvalidDateRangeException;
import com.hostfully.app.infra.exception.PropertyNotFoundException;
import com.hostfully.app.infra.mapper.BookingMapper;
import com.hostfully.app.infra.repository.BookingRepository;
import com.hostfully.app.infra.repository.PropertyRepository;
import com.hostfully.app.shared.BookingLockRegistry;
import com.hostfully.app.shared.IdempotencyService;
import com.hostfully.app.shared.util.DateRangeValidator;
import com.hostfully.app.shared.util.NanoIdGenerator;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CreateBooking {

    private static final Logger log = LoggerFactory.getLogger(CreateBooking.class);

    private final IdempotencyService idempotencyService;
    private final NanoIdGenerator nanoIdGenerator;
    private final AvailabilityService availabilityService;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final BookingLockRegistry bookingLockRegistry;

    public Booking execute(final CreateBookingCommand command) {
        final ReentrantLock lock = bookingLockRegistry.getLock(command.property);
        boolean acquired = lock.tryLock();

        if (!acquired) {
            throw new ConcurrentBookingException("Booking is already in progress");
        }

        try {
            return doBooking(command);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    private Booking doBooking(final CreateBookingCommand command) {
        final UUID idempotencyKey = command.idempotencyKey;
        final Optional<Booking> result = idempotencyService.getResponse(idempotencyKey, Booking.class);
        if (result.isPresent()) return result.get();

        final Booking booking = new Booking(
                nanoIdGenerator.generateId(),
                command.property,
                command.startDate,
                command.endDate,
                command.guestName,
                command.numberGuests,
                BookingStatus.CONFIRMED.name());

        if (!DateRangeValidator.validateDateRange(booking.getStartDate(), booking.getEndDate(), false))
            throw new InvalidDateRangeException("The start date and end date must not be the same. "
                    + "The end date should be greater than the start date.");

        if (!availabilityService.canBook(
                booking.getStartDate(), booking.getEndDate(), booking.getPropertyId(), booking.getId()))
            throw new OverlapBookingException("We’re unable to process your booking for this property. "
                    + "Please refresh the page or try again later.");

        final PropertyEntity propertyEntity = getProperty(booking.getPropertyId());
        try {
            final Booking bookingResult =
                    BookingMapper.toDomain(bookingRepository.save(BookingMapper.toEntity(booking, propertyEntity)));
            idempotencyService.saveResponse(idempotencyKey, bookingResult);
            return booking;
        } catch (Exception ex) {
            log.error("Failed to create a booking: {}", booking, ex);
            throw new BookingGenericException("Unexpected error while creating booking", ex);
        }
    }

    private PropertyEntity getProperty(final String propertyId) {
        return propertyRepository
                .findByExternalId(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found by ID provided"));
    }

    public record CreateBookingCommand(
            String property,
            LocalDate startDate,
            LocalDate endDate,
            String guestName,
            Integer numberGuests,
            UUID idempotencyKey) {}
}
