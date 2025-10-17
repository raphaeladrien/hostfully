package com.hostfully.app.availability.service;

import com.hostfully.app.block.domain.Block;
import com.hostfully.app.block.exceptions.OverlapBlockException;
import com.hostfully.app.infra.exception.InvalidDateRangeException;
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
                && !blockRepository.hasOverlapping(propertyId, startDate, endDate, null);
    }

    public Boolean hasValidDateRange(final Block block, final Boolean isUpdate) {
        final LocalDate endDate = block.getEndDate();
        final LocalDate startDate = block.getStartDate();

        if (!endDate.isAfter(startDate)) {
            if (!startDate.isEqual(endDate)) throw new InvalidDateRangeException("Start date must be before end date");
        }

        String externalId = isUpdate ? block.getId() : null;

        if (blockRepository.hasOverlapping(block.getPropertyId(), startDate, endDate, externalId))
            throw new OverlapBlockException("Block already created for this property in the timeframe provided");

        return true;
    }
}
