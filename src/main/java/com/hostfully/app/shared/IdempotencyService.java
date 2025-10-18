package com.hostfully.app.shared;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hostfully.app.block.usecase.CreateBlock;
import com.hostfully.app.infra.entity.Idempotency;
import com.hostfully.app.infra.repository.IdempotencyRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper mapper;

    public <T> void saveResponse(UUID id, T response) {
        try {
            String json = mapper.writeValueAsString(response);
            idempotencyRepository.save(new Idempotency(id, json));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Failed to serialize response", e);
        }
    }

    public <T> Optional<T> getResponse(UUID key, Class<T> responseType) {
        return idempotencyRepository.findById(key).map(record -> {
            try {
                return mapper.readValue(record.getResponse(), responseType);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException("Failed to deserialize response", e);
            }
        });
    }
}
