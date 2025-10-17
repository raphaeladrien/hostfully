package com.hostfully.app.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostfully.app.block.exceptions.BlockGenericException;
import com.hostfully.app.block.exceptions.OverlapBlockException;
import com.hostfully.app.booking.controller.dto.BookingRequest;
import com.hostfully.app.booking.domain.Booking;
import com.hostfully.app.booking.usecase.CreateBooking;
import com.hostfully.app.infra.exception.InvalidDateRangeException;
import com.hostfully.app.infra.exception.PropertyNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private CreateBooking createBooking;

    private final String url = "/v1/bookings";

    private final String guest = "Galadriel";
    private final LocalDate startDate = LocalDate.now();
    private final LocalDate endDate = LocalDate.now();

    @Test
    @DisplayName("POST /bookings - booking created successfully")
    void postCreateBookingSuccess() throws Exception {
        final BookingRequest payload = buildBookingRequest();

        Mockito.when(createBooking.execute(Mockito.any())).thenReturn(buildBooking());

        final MockHttpServletRequestBuilder request = post(url)
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /bookings - error missing Idempotency-Key header")
    void postCreateBookingMissingIdempotencyHeader() throws Exception {
        final BookingRequest payload = buildBookingRequest();

        Mockito.when(createBooking.execute(Mockito.any())).thenReturn(buildBooking());

        final MockHttpServletRequestBuilder request =
                post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /bookings - error invalid date range")
    void postCreateBookingInvalidDateRangeException() throws Exception {
        final BookingRequest payload = buildBookingRequest();

        Mockito.when(createBooking.execute(Mockito.any())).thenThrow(new InvalidDateRangeException("an error"));

        final MockHttpServletRequestBuilder request = post(url)
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /bookings - error overlapping block exists")
    void postCreateBookingOverlapBlockException() throws Exception {
        final BookingRequest payload = buildBookingRequest();

        Mockito.when(createBooking.execute(Mockito.any())).thenThrow(new OverlapBlockException("an error"));

        final MockHttpServletRequestBuilder request = post(url)
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /bookings - error property not found")
    void postCreateBookingPropertyNotFoundException() throws Exception {
        final BookingRequest payload = buildBookingRequest();

        Mockito.when(createBooking.execute(Mockito.any())).thenThrow(new PropertyNotFoundException("an error"));

        final MockHttpServletRequestBuilder request = post(url)
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /bookings - error unexpected internal error")
    void postGenericBookingCreationException() throws Exception {
        final BookingRequest payload = buildBookingRequest();

        Mockito.when(createBooking.execute(Mockito.any())).thenThrow(new BlockGenericException("an error", null));

        final MockHttpServletRequestBuilder request = post(url)
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isInternalServerError());
    }

    private BookingRequest buildBookingRequest() {
        return new BookingRequest("AMAZINGHOUSE",  guest, 2, startDate, endDate);
    }

    private Booking buildBooking() {
        return new Booking("FLORESTXC", "AMAZINGHOUSE", startDate, endDate, guest, 2, "CONFIRMED");
    }
}
