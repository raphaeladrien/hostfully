package com.hostfully.app.block.usecase;

import com.hostfully.app.block.domain.Block;
import com.hostfully.app.block.exceptions.BlockGenericException;
import com.hostfully.app.block.exceptions.InvalidDateRangeException;
import com.hostfully.app.block.exceptions.OverlapBlockException;
import com.hostfully.app.block.exceptions.PropertyNotFoundException;
import com.hostfully.app.infra.entity.PropertyEntity;
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

        hasValidDateRange(block);
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

    private void hasValidDateRange(final Block block) {
        final LocalDate endDate = block.getEndDate();
        final LocalDate startDate = block.getStartDate();
        if (!endDate.isAfter(startDate)) {
            if (!startDate.isEqual(endDate)) throw new InvalidDateRangeException("Start date must be before end date");
        }
        if (blockRepository.hasOverlapping(block.getPropertyId(), startDate, endDate))
            throw new OverlapBlockException("Block already created for this property in the timeframe provided");
    }

    private PropertyEntity getProperty(final String propertyId) {
        return propertyRepository
                .findByExternalId(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found by ID provided"));
    }

    public record CreateBlockCommand(
            String property, String reason, LocalDate startDate, LocalDate endDate, UUID idempotencyKey) {}
}
