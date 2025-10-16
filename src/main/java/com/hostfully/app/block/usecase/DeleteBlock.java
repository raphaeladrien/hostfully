package com.hostfully.app.block.usecase;

import com.hostfully.app.block.exceptions.BlockGenericException;
import com.hostfully.app.infra.repository.BlockRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeleteBlock {

    private static final Logger log = LoggerFactory.getLogger(DeleteBlock.class);

    private final BlockRepository blockRepository;

    public Boolean execute(final String id) {
        try {
            return blockRepository.deleteByExternalId(id) > 0;
        } catch (Exception ex) {
            log.error("Failed to delete a block: {}", id);
            throw new BlockGenericException("Unexpected error while removing block", ex);
        }
    }
}
