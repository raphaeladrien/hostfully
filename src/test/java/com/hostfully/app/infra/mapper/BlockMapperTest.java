package com.hostfully.app.infra.mapper;

import com.hostfully.app.block.domain.Block;
import com.hostfully.app.infra.entity.BlockEntity;
import com.hostfully.app.infra.entity.PropertyEntity;
import java.time.LocalDate;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BlockMapperTest {

    @Test
    @DisplayName("should map property domain to entity")
    void shouldMapPropertyToEntity() {
        final String id = "qwerty-poiuy";
        final String propertyId = "asert-mnbvcx";
        final String reason = "a-reason";
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = LocalDate.now();

        final Block block = new Block(id, propertyId, reason, startDate, endDate);

        final PropertyEntity propertyEntity = new PropertyEntity(propertyId, "a-super-description", "a-alias");

        final BlockEntity result = BlockMapper.toEntity(block, propertyEntity);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(result.getId()).isNull();
            softAssertions.assertThat(result.getExternalId()).isEqualTo(id);
            softAssertions.assertThat(result.getProperty().getExternalId()).isEqualTo(propertyId);
            softAssertions.assertThat(result.getReason()).isEqualTo(reason);
            softAssertions.assertThat(result.getStartDate()).isEqualTo(startDate);
            softAssertions.assertThat(result.getStartDate()).isEqualTo(endDate);
        });
    }

    @Test
    @DisplayName("should map property entity to domain")
    void shouldMapEntityToDomain() {
        final String id = "qwerty-poiuy";
        final String propertyId = "asert-mnbvcx";
        final String reason = "a-reason";
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = LocalDate.now();
        final PropertyEntity property = new PropertyEntity(propertyId, "a-super-description", "a-alias");

        final BlockEntity blockEntity = new BlockEntity(id, property, reason, startDate, endDate);

        final Block result = BlockMapper.toDomain(blockEntity);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(result.getId()).isEqualTo(id);
            softAssertions.assertThat(result.getPropertyId()).isEqualTo(propertyId);
            softAssertions.assertThat(result.getReason()).isEqualTo(reason);
            softAssertions.assertThat(result.getStartDate()).isEqualTo(startDate);
            softAssertions.assertThat(result.getStartDate()).isEqualTo(endDate);
        });
    }
}
