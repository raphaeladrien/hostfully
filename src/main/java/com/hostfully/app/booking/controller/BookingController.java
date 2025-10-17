package com.hostfully.app.booking.controller;

import com.hostfully.app.booking.controller.dto.BookingRequest;
import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.usecase.CreateBooking;
import com.hostfully.app.booking.usecase.CreateBooking.CreateBookingCommand;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/bookings")
@AllArgsConstructor
public class BookingController {

    private CreateBooking createBooking;

    @PostMapping
    public ResponseEntity<Booking> createBlock(
            @Valid @RequestBody BookingRequest request,
            @RequestHeader(value = "Idempotency-Key") final UUID idempotencyKey) {
        final Booking booking = createBooking.execute(new CreateBookingCommand(
                request.property(),
                request.startDate(),
                request.endDate(),
                request.guest(),
                request.numberGuest(),
                idempotencyKey));
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }
}
