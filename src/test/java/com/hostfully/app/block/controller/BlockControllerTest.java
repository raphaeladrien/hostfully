package com.hostfully.app.block.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostfully.app.block.controller.dto.BlockRequest;
import com.hostfully.app.block.domain.Block;
import com.hostfully.app.block.exceptions.*;
import com.hostfully.app.block.usecase.CreateBlock;
import com.hostfully.app.block.usecase.DeleteBlock;
import com.hostfully.app.block.usecase.UpdateBlock;
import java.time.LocalDate;
import java.util.UUID;
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

@SpringBootTest
@AutoConfigureMockMvc
public class BlockControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private CreateBlock createBlock;

    @MockitoBean
    private DeleteBlock deleteBlock;

    @MockitoBean
    private UpdateBlock updateBlock;

    private final String url = "/v1/blocks";

    @Test
    @DisplayName("POST /blocks - block created successfully")
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
    @DisplayName("POST /blocks - error missing Idempotency-Key header")
    void postCreateBlockMissingIdempotencyHeader() throws Exception {
        final BlockRequest payload = buildBlockRequest();

        Mockito.when(createBlock.execute(Mockito.any())).thenReturn(buildBlock());

        final MockHttpServletRequestBuilder request =
                post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /blocks - error invalid date range")
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
    @DisplayName("POST /blocks - error overlapping block exists")
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
    @DisplayName("POST /blocks - error property not found")
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
    @DisplayName("POST /blocks - error unexpected internal error")
    void postGenericBlockCreationException() throws Exception {
        final BlockRequest payload = buildBlockRequest();

        Mockito.when(createBlock.execute(Mockito.any())).thenThrow(new BlockGenericException("an error", null));

        final MockHttpServletRequestBuilder request = post(url)
                .header("Idempotency-Key", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("DELETE /blocks/{id} - block deleted")
    void deleteBlock() throws Exception {
        final String id = "a-super-id";
        Mockito.when(deleteBlock.execute(id)).thenReturn(true);

        final MockHttpServletRequestBuilder request = delete(url + "/" + id);

        mvc.perform(request).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /blocks/{id} - error unexpected internal error")
    void deleteGenericBlockCreationException() throws Exception {
        final String id = "a-super-id";
        Mockito.when(deleteBlock.execute(id)).thenThrow(new BlockGenericException("a error", null));

        final MockHttpServletRequestBuilder request = delete(url + "/" + id);

        mvc.perform(request).andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("PUT /blocks - block updated successfully")
    void putUpdateBlockSuccess() throws Exception {
        final BlockRequest payload = buildBlockRequest();
        final String id = "block-id";

        Mockito.when(updateBlock.execute(Mockito.any())).thenReturn(buildBlock());

        final MockHttpServletRequestBuilder request = put(url + "/" + id)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /blocks/{id} - error block not found exception")
    void putBlockNotFoundException() throws Exception {
        final BlockRequest payload = buildBlockRequest();
        final String id = "block-id";

        Mockito.when(updateBlock.execute(Mockito.any())).thenThrow(new BlockNotFoundException("a error"));

        final MockHttpServletRequestBuilder request = put(url + "/" + id)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /blocks/{id} - error invalid date range exception")
    void putBlockInvalidDateRangeException() throws Exception {
        final BlockRequest payload = buildBlockRequest();
        final String id = "block-id";

        Mockito.when(updateBlock.execute(Mockito.any())).thenThrow(new InvalidDateRangeException("a error"));

        final MockHttpServletRequestBuilder request = put(url + "/" + id)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /blocks/{id} - error overlap block exception")
    void putBlockOverlapBlockException() throws Exception {
        final BlockRequest payload = buildBlockRequest();
        final String id = "block-id";

        Mockito.when(updateBlock.execute(Mockito.any())).thenThrow(new OverlapBlockException("a error"));

        final MockHttpServletRequestBuilder request = put(url + "/" + id)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /blocks/{id} - error unexpected internal error")
    void putBlockBlockGenericException() throws Exception {
        final BlockRequest payload = buildBlockRequest();
        final String id = "block-id";

        Mockito.when(updateBlock.execute(Mockito.any())).thenThrow(new BlockGenericException("a error", null));

        final MockHttpServletRequestBuilder request = put(url + "/" + id)
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
