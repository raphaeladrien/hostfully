package com.hostfully.app.booking.usecase;

import org.springframework.stereotype.Service;

@Service
public class CreateBookingUseCase {

    public boolean call(final CreateBookingCommand request) {
        return true;
    }

    public record CreateBookingCommand() {}
}
