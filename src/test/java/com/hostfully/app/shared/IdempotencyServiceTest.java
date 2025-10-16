package com.hostfully.app.shared;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostfully.app.infra.entity.Idempotency;
import com.hostfully.app.infra.repository.IdempotencyRepository;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

public class IdempotencyServiceTest {

    private final IdempotencyRepository idempotencyRepository = mock(IdempotencyRepository.class);
    private final ObjectMapper mapper = new Jackson2ObjectMapperBuilder().build();

    private final IdempotencyService subject = new IdempotencyService(idempotencyRepository, mapper);

    @Test
    @DisplayName("should serialize and save the response with idempotency key")
    void shouldSerializeAndSaveResponse() {
        final String expectedJson = "{\"name\":\"test-value\",\"age\":30}";
        final TestResponse testResponse = new TestResponse("test-value", 30);
        final UUID id = UUID.randomUUID();

        subject.saveResponse(id, testResponse);

        verify(idempotencyRepository, times(1))
                .save(argThat(idempotency -> idempotency.getId().equals(id)
                        && idempotency.getResponse().equals(expectedJson)));
    }

    @Test
    @DisplayName("throws RuntimeException, when an unexpected exception occurred")
    void throwsRuntimeExceptionWhenUnexpectedErrorOccurredSave() {
        when(idempotencyRepository.save(any())).thenThrow(new RuntimeException("an error"));

        Assertions.assertThrows(RuntimeException.class, () -> subject.saveResponse(any(), any()));
    }

    @Test
    @DisplayName("should return empty option, when idempotency isn't found")
    void shouldReturnEmptyOptionalWhenNotFound() {
        final UUID id = UUID.randomUUID();

        when(idempotencyRepository.findById(id)).thenReturn(Optional.empty());

        Optional<TestResponse> result = subject.getResponse(id, TestResponse.class);

        Assertions.assertFalse(result.isPresent());
        verify(idempotencyRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("should return a response option, when idempotency is found")
    void shouldReturnResponseOptionalWhenNotFound() {
        final UUID id = UUID.randomUUID();
        final String json = "{\"name\":\"test-value\",\"age\":30}";
        final Idempotency idempotency = new Idempotency(id, json);

        when(idempotencyRepository.findById(id)).thenReturn(Optional.of(idempotency));

        Optional<TestResponse> optionalResult = subject.getResponse(id, TestResponse.class);

        SoftAssertions.assertSoftly(assertion -> {
            assertion.assertThat(optionalResult.isPresent()).isTrue();
            final TestResponse result = optionalResult.get();
            assertion.assertThat(result.name).isEqualTo("test-value");
            assertion.assertThat(result.age).isEqualTo(30);
        });

        verify(idempotencyRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("throws RuntimeException, when an unexpected exception occurred to obtain")
    void throwsRuntimeExceptionWhenUnexpectedErrorObtain() {
        when(idempotencyRepository.findById(any())).thenThrow(new RuntimeException("an error"));

        Assertions.assertThrows(RuntimeException.class, () -> subject.getResponse(any(), any()));
    }

    public record TestResponse(String name, Integer age) {}
}
