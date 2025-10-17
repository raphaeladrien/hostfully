package com.hostfully.app.booking.usecase;

import com.hostfully.app.block.exceptions.BlockGenericException;
import com.hostfully.app.infra.repository.BookingRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeleteBooking {

    private static final Logger log = LoggerFactory.getLogger(DeleteBooking.class);

    private final BookingRepository bookingRepository;

    public Boolean execute(final String id) {
        try {
            return bookingRepository.deleteByExternalId(id) > 0;
        } catch (Exception ex) {
            log.error("Failed to delete a block: {}", id);
            throw new BlockGenericException("Unexpected error while removing block", ex);
        }
    }
}
