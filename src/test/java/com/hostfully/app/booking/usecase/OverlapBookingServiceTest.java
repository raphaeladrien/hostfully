package com.hostfully.app.booking.usecase;

import static com.hostfully.app.booking.usecase.CreateBooking.CreateBookingCommand;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.hostfully.app.availability.service.AvailabilityService;
import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.BookingRepository;
import com.hostfully.app.infra.repository.PropertyRepository;
import com.hostfully.app.shared.IdempotencyService;
import com.hostfully.app.shared.util.NanoIdGenerator;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Assertions;
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
    void ensureNoOverbookingTest() throws InterruptedException, ExecutionException {
        final CreateBookingCommand command = createCommand(UUID.randomUUID(), propertyId, startDate, endDate, "user-1");
        final CreateBookingCommand command2 =
                createCommand(UUID.randomUUID(), propertyId, startDate, endDate, "user-2");

        final CreateBooking spyCreateBooking = Mockito.spy(new CreateBooking(
                idempotencyService, nanoIdGenerator, availabilityService, propertyRepository, bookingRepository));

        final ExecutorService executor = Executors.newFixedThreadPool(2);

        final Future<Booking> result1 = executor.submit(() -> spyCreateBooking.execute(command));
        final Future<Booking> result2 = executor.submit(() -> spyCreateBooking.execute(command2));

        final Booking booking1 = result1.get();
        final Booking booking2 = result2.get();

        verify(spyCreateBooking, times(1)).execute(command);
        verify(spyCreateBooking, times(1)).execute(command2);

        Assertions.assertNotNull(booking1);
        Assertions.assertNotNull(booking2);
    }

    private CreateBooking.CreateBookingCommand createCommand(
            UUID idempotencyKey, String propertyId, LocalDate startDate, LocalDate endDate, String guest) {
        return new CreateBooking.CreateBookingCommand(propertyId, startDate, endDate, guest, 2, idempotencyKey);
    }
}
