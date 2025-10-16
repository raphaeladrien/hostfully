package com.hostfully.app.block.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostfully.app.block.controller.dto.BlockRequest;
import com.hostfully.app.block.domain.Block;
import com.hostfully.app.block.exceptions.BlockCreationException;
import com.hostfully.app.block.exceptions.InvalidDateRangeException;
import com.hostfully.app.block.exceptions.OverlapBlockException;
import com.hostfully.app.block.exceptions.PropertyNotFoundException;
import com.hostfully.app.block.usecase.CreateBlock;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
public class BlockControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private CreateBlock createBlock;

    private final String url = "/v1/blocks";

    @Test
    void postCreateBlockSuccess() throws Exception {
        final BlockRequest payload = buildBlockRequest();

        Mockito.when(createBlock.execute(Mockito.any())).thenReturn(buildBlock());

        final MockHttpServletRequestBuilder request = post(url)
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isCreated());
    }

    @Test
    void postCreateBlockMissingIdempotencyHeader() throws Exception {
        final BlockRequest payload = buildBlockRequest();

        Mockito.when(createBlock.execute(Mockito.any())).thenReturn(buildBlock());

        final MockHttpServletRequestBuilder request =
                post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    void postCreateBlockInvalidDateRangeException() throws Exception {
        final BlockRequest payload = buildBlockRequest();

        Mockito.when(createBlock.execute(Mockito.any())).thenThrow(new InvalidDateRangeException("an error"));

        final MockHttpServletRequestBuilder request = post(url)
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    void postCreateBlockOverlapBlockException() throws Exception {
        final BlockRequest payload = buildBlockRequest();

        Mockito.when(createBlock.execute(Mockito.any())).thenThrow(new OverlapBlockException("an error"));

        final MockHttpServletRequestBuilder request = post(url)
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isConflict());
    }

    @Test
    void postCreateBlockPropertyNotFoundException() throws Exception {
        final BlockRequest payload = buildBlockRequest();

        Mockito.when(createBlock.execute(Mockito.any())).thenThrow(new PropertyNotFoundException("an error"));

        final MockHttpServletRequestBuilder request = post(url)
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    void postCreateBlockCreationException() throws Exception {
        final BlockRequest payload = buildBlockRequest();

        Mockito.when(createBlock.execute(Mockito.any())).thenThrow(new BlockCreationException("an error", null));

        final MockHttpServletRequestBuilder request = post(url)
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isInternalServerError());
    }

    private BlockRequest buildBlockRequest() {
        return new BlockRequest("AMAZINGHOUSE", "painting", "an-alias", LocalDate.now(), LocalDate.now());
    }

    private Block buildBlock() {
        return new Block("FLORESTXC", "AMAZINGHOUSE", "painting", LocalDate.now(), LocalDate.now());
    }
}
