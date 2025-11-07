package com.hostfully.app.booking.usecase;

import static com.hostfully.app.booking.usecase.CreateBooking.CreateBookingCommand;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.hostfully.app.availability.service.AvailabilityService;
import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.exception.ConcurrentBookingException;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.BookingRepository;
import com.hostfully.app.infra.repository.PropertyRepository;
import com.hostfully.app.shared.BookingLockRegistry;
import com.hostfully.app.shared.IdempotencyService;
import com.hostfully.app.shared.util.NanoIdGenerator;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.*;
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

    @Autowired
    private BookingLockRegistry bookingLockRegistry;

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
        final CreateBookingCommand command1 =
                createCommand(UUID.randomUUID(), propertyId, startDate, endDate, "user-1");
        final CreateBookingCommand command2 =
                createCommand(UUID.randomUUID(), propertyId, startDate, endDate, "user-2");

        final CreateBooking spyCreateBooking = Mockito.spy(new CreateBooking(
                idempotencyService,
                nanoIdGenerator,
                availabilityService,
                propertyRepository,
                bookingRepository,
                bookingLockRegistry));

        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final CountDownLatch latch = new CountDownLatch(1);

        Callable<Booking> task1 = () -> {
            latch.await();
            return spyCreateBooking.execute(command1);
        };

        Callable<Booking> task2 = () -> {
            latch.await();
            return spyCreateBooking.execute(command2);
        };

        final Future<Booking> result1 = executor.submit(task1);
        final Future<Booking> result2 = executor.submit(task2);

        latch.countDown();

        Booking booking1 = null;
        Booking booking2 = null;
        Exception exception = null;

        try {
            booking1 = result1.get();
        } catch (ExecutionException e) {
            exception = (Exception) e.getCause();
        }

        try {
            booking2 = result2.get();
        } catch (ExecutionException e) {
            exception = (Exception) e.getCause();
        }

        Assertions.assertTrue((booking1 != null && booking2 == null && exception instanceof ConcurrentBookingException)
                || (booking2 != null && booking1 == null && exception instanceof ConcurrentBookingException));

        verify(spyCreateBooking, times(1)).execute(command1);
        verify(spyCreateBooking, times(1)).execute(command2);

        executor.shutdown();
    }

    private CreateBooking.CreateBookingCommand createCommand(
            UUID idempotencyKey, String propertyId, LocalDate startDate, LocalDate endDate, String guest) {
        return new CreateBooking.CreateBookingCommand(propertyId, startDate, endDate, guest, 2, idempotencyKey);
    }
}
