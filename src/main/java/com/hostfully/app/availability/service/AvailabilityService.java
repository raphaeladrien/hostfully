package com.hostfully.app.availability.service;

import com.hostfully.app.infra.repository.BlockRepository;
import com.hostfully.app.infra.repository.BookingRepository;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AvailabilityService {

    private final BlockRepository blockRepository;
    private final BookingRepository bookingRepository;

    public Boolean canBook(
            final LocalDate startDate, final LocalDate endDate, final String propertyId, final String bookingId) {
        return !bookingRepository.hasOverlapping(propertyId, startDate, endDate, bookingId)
                && !blockRepository.hasOverlapping(propertyId, startDate, endDate);
    }

    public Boolean canBlock(final LocalDate startDate, final LocalDate endDate, final String propertyId) {
        return !bookingRepository.hasOverlapping(propertyId, startDate, endDate, null);
    }
}
