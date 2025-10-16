package com.hostfully.app.block.usecase;

import com.hostfully.app.block.domain.Block;
import com.hostfully.app.block.exceptions.BlockGenericException;
import com.hostfully.app.block.exceptions.BlockNotFoundException;
import com.hostfully.app.block.exceptions.PropertyNotFoundException;
import com.hostfully.app.block.service.BlockDateValidationService;
import com.hostfully.app.infra.entity.BlockEntity;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.mapper.BlockMapper;
import com.hostfully.app.infra.repository.BlockRepository;
import com.hostfully.app.infra.repository.PropertyRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateBlock {

    private static final Logger log = LoggerFactory.getLogger(UpdateBlock.class);

    private final BlockRepository blockRepository;
    private final PropertyRepository propertyRepository;
    private final BlockDateValidationService blockDateValidationService;

    @Transactional
    public Block execute(final UpdateBlockCommand updateBlockCommand) {
        final Block block = new Block(
                updateBlockCommand.id,
                updateBlockCommand.property,
                updateBlockCommand.reason,
                updateBlockCommand.startDate,
                updateBlockCommand.endDate);

        if (!blockRepository.existsByExternalId(block.getId()))
            throw new BlockNotFoundException("Block not found by id provided");

        blockDateValidationService.hasValidDateRange(block, true);

        final PropertyEntity propertyEntity = getProperty(block.getPropertyId());
        updateBlock(propertyEntity, block);
        final BlockEntity blockEntity = blockRepository
                .findByExternalId(block.getId())
                .orElseThrow(() -> new BlockNotFoundException("Block not found by id provided"));

        return BlockMapper.toDomain(blockEntity);
    }

    private void updateBlock(PropertyEntity propertyEntity, Block block) {
        try {
            blockRepository.updateByExternalId(
                    propertyEntity, block.getReason(), block.getStartDate(), block.getEndDate(), block.getId());
        } catch (Exception ex) {
            log.error("Failed to update a block: {}", block, ex);
            throw new BlockGenericException("Unexpected error while updating block", ex);
        }
    }

    private PropertyEntity getProperty(final String propertyId) {
        return propertyRepository
                .findByExternalId(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException("Property not found by ID provided"));
    }

    public record UpdateBlockCommand(
            String id, String property, String reason, LocalDate startDate, LocalDate endDate) {}
}
