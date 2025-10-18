package com.hostfully.app.booking.controller;

import com.hostfully.app.booking.controller.dto.BookingRequest;
import com.hostfully.app.booking.controller.dto.RebookBookingRequest;
import com.hostfully.app.booking.controller.dto.UpdateBookingRequest;
import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.usecase.*;
import com.hostfully.app.booking.usecase.CreateBooking.CreateBookingCommand;
import com.hostfully.app.booking.usecase.UpdateBooking.UpdateBookingCommand;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/bookings")
@AllArgsConstructor
public class BookingController {

    private CreateBooking createBooking;
    private DeleteBooking deleteBooking;
    private GetBooking getBooking;
    private CancelBooking cancelBooking;
    private RebookBooking rebookBooking;
    private UpdateBooking updateBooking;

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody final BookingRequest request,
            @RequestHeader(value = "Idempotency-Key") final UUID idempotencyKey) {
        final Booking booking = createBooking.execute(new CreateBookingCommand(
                request.property(),
                request.startDate(),
                request.endDate(),
                request.guest(),
                request.numberGuest(),
                idempotencyKey));

        final URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/bookings/{id}")
                .buildAndExpand(booking.getId())
                .toUri();

        return ResponseEntity.created(location).body(booking);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable final String id, @RequestHeader(value = "Idempotency-Key") final UUID idempotencyKey) {
        return ResponseEntity.ok(cancelBooking.execute(id, idempotencyKey));
    }

    @PostMapping("/{id}/rebook")
    public ResponseEntity<Booking> rebookBooking(
            @Valid @RequestBody final RebookBookingRequest request,
            @PathVariable final String id,
            @RequestHeader(value = "Idempotency-Key") final UUID idempotencyKey) {
        return ResponseEntity.ok(rebookBooking.execute(
                new RebookBooking.RebookCommand(id, request.startDate(), request.endDate(), idempotencyKey)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable final String id) {
        deleteBooking.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable final String id) {
        return ResponseEntity.ok(getBooking.execute(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(
            @PathVariable final String id, @Valid @RequestBody final UpdateBookingRequest request) {
        final Booking booking = updateBooking.execute(new UpdateBookingCommand(
                id, request.startDate(), request.endDate(), request.guest(), request.numberGuest()));
        return ResponseEntity.ok(booking);
    }
}
