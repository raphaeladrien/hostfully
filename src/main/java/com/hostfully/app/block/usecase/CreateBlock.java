package com.hostfully.app.block.usecase;

import com.hostfully.app.availability.service.AvailabilityService;
import com.hostfully.app.block.domain.Block;
import com.hostfully.app.block.exceptions.BlockGenericException;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.exception.PropertyNotFoundException;
import com.hostfully.app.infra.mapper.BlockMapper;
import com.hostfully.app.infra.repository.BlockRepository;
import com.hostfully.app.infra.repository.PropertyRepository;
import com.hostfully.app.shared.IdempotencyService;
import com.hostfully.app.shared.util.NanoIdGenerator;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreateBlock {

    private static final Logger log = LoggerFactory.getLogger(CreateBlock.class);

    private final BlockRepository blockRepository;
    private final PropertyRepository propertyRepository;
    private final NanoIdGenerator nanoIdGenerator;
    private final IdempotencyService idempotencyService;
    private final AvailabilityService availabilityService;

    @Transactional
    public Block execute(final CreateBlockCommand createBlockCommand) {
        final UUID idempotencyKey = createBlockCommand.idempotencyKey;
        final Optional<Block> result = idempotencyService.getResponse(idempotencyKey, Block.class);
        if (result.isPresent()) return result.get();

        final Block block = new Block(
                nanoIdGenerator.generateId(),
                createBlockCommand.property,
                createBlockCommand.reason,
                createBlockCommand.startDate,
                createBlockCommand.endDate);

        availabilityService.hasValidDateRange(block, false);
        final PropertyEntity propertyEntity = getProperty(block.getPropertyId());
        try {
            final Block blockResult =
                    BlockMapper.toDomain(blockRepository.save(BlockMapper.toEntity(block, propertyEntity)));
            idempotencyService.saveResponse(idempotencyKey, blockResult);
            return blockResult;
        } catch (Exception ex) {
            log.error("Failed to create a block: {}", block, ex);
            throw new BlockGenericException("Unexpected error while creating block", ex);
        }
    }

    private PropertyEntity getProperty(final String propertyId) {
        return propertyRepository
                .findByExternalId(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found by ID provided"));
    }

    public record CreateBlockCommand(
            String property, String reason, LocalDate startDate, LocalDate endDate, UUID idempotencyKey) {}
}
