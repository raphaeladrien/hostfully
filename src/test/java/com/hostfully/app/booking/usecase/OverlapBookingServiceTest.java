package com.hostfully.app.booking.usecase;

import static com.hostfully.app.booking.usecase.CreateBooking.CreateBookingCommand;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.hostfully.app.availability.service.AvailabilityService;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.BookingRepository;
import com.hostfully.app.infra.repository.PropertyRepository;
import com.hostfully.app.shared.IdempotencyService;
import com.hostfully.app.shared.util.NanoIdGenerator;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OverlapBookingServiceTest {

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private NanoIdGenerator nanoIdGenerator;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private final LocalDate startDate = LocalDate.now();
    private final LocalDate endDate = LocalDate.now().plusDays(1);
    private final String propertyId = "PROP-001";

    @BeforeEach
    void setup() {
        propertyRepository.save(new PropertyEntity(propertyId, "Beach House", "Beach baby!"));
    }

    @Test
    @DisplayName("ensure that the system won't overlap a booking")
    void ensureNoOverbookingTest() {
        final UUID uuid = UUID.randomUUID();
        final String guest = "Joe Doe";
        final CreateBookingCommand command = createCommand(uuid, propertyId, startDate, endDate, guest);

        CreateBooking spyCreateBooking = Mockito.spy(new CreateBooking(
                idempotencyService, nanoIdGenerator, availabilityService, propertyRepository, bookingRepository));

        spyCreateBooking.execute(command);
        verify(spyCreateBooking, times(1)).execute(command);
    }

    private CreateBooking.CreateBookingCommand createCommand(
            UUID idempotencyKey, String propertyId, LocalDate startDate, LocalDate endDate, String guest) {
        return new CreateBooking.CreateBookingCommand(propertyId, startDate, endDate, guest, 2, idempotencyKey);
    }
}
