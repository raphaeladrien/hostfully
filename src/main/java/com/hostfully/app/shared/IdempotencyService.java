package com.hostfully.app.shared;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostfully.app.infra.entity.Idempotency;
import com.hostfully.app.infra.repository.IdempotencyRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper mapper;

    public <T> void saveResponse(UUID id, T response) {
        try {
            String json = mapper.writeValueAsString(response);
            idempotencyRepository.save(new Idempotency(id, json));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize response", e);
        }
    }

    public <T> Optional<T> getResponse(UUID key, Class<T> responseType) {
        return idempotencyRepository.findById(key).map(record -> {
            try {
                return mapper.readValue(record.getResponse(), responseType);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize response", e);
            }
        });
    }
}
