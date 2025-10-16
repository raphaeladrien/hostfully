package com.hostfully.app.infra.mapper;

import com.hostfully.app.block.domain.Block;
import com.hostfully.app.infra.entity.BlockEntity;
import com.hostfully.app.infra.entity.PropertyEntity;

public abstract class BlockMapper {

    public static BlockEntity toEntity(final Block block, final PropertyEntity propertyEntity) {
        return new BlockEntity(
                block.getId(), propertyEntity, block.getReason(), block.getStartDate(), block.getEndDate());
    }

    public static Block toDomain(final BlockEntity blockEntity) {
        return new Block(
                blockEntity.getExternalId(),
                blockEntity.getProperty().getExternalId(),
                blockEntity.getReason(),
                blockEntity.getStartDate(),
                blockEntity.getEndDate());
    }
}
