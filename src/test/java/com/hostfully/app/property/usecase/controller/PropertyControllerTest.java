package com.hostfully.app.property.usecase.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostfully.app.property.controller.dto.PropertyRequest;
import com.hostfully.app.property.domain.Property;
import com.hostfully.app.property.exception.PropertyCreationException;
import com.hostfully.app.property.usecase.CreateProperty;
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
public class PropertyControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private CreateProperty createProperty;

    private final String url = "/v1/properties";

    private final String description = "Amazing place";
    private final String alias = "super alias";

    @Test
    @DisplayName("POST /properties - properties created successfully")
    void postCreatePropertySuccess() throws Exception {
        final PropertyRequest payload = buildRequest();

        Mockito.when(createProperty.execute(Mockito.any())).thenReturn(buildProperty());

        final MockHttpServletRequestBuilder request =
                post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /properties - error Internal server error")
    void postCreatePropertyInternalServerError() throws Exception {
        final PropertyRequest payload = buildRequest();

        Mockito.when(createProperty.execute(Mockito.any())).thenThrow(new PropertyCreationException("error", null));

        final MockHttpServletRequestBuilder request =
                post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(mapper.writeValueAsString(payload));

        mvc.perform(request).andExpect(status().isInternalServerError());
    }

    private Property buildProperty() {
        return new Property("qwerty-123", description, alias);
    }

    private PropertyRequest buildRequest() {
        return new PropertyRequest(description, alias);
    }
}
