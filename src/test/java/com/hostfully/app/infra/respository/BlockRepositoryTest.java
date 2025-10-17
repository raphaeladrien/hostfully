package com.hostfully.app.infra.respository;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.hostfully.app.infra.entity.BlockEntity;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.BlockRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
public class BlockRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BlockRepository blockRepository;

    private PropertyEntity property1;
    private PropertyEntity property2;

    @BeforeEach
    void setUp() {
        property1 = entityManager.persist(new PropertyEntity("PROP-001", "Beach House", "Beach baby!"));
        property2 = entityManager.persist(new PropertyEntity("PROP-002", "Mountain Cabin", "Relax time"));

        createAndSaveBlock("asert-1234", property2, LocalDate.of(2025, 2, 5), LocalDate.of(2025, 2, 15));

        entityManager.flush();
    }

    @ParameterizedTest
    @MethodSource("provideRanges")
    @DisplayName("when overlap isn't detected, returns false")
    void whenOverlapIsNotDetectedReturnsFalse(LocalDate startDate, LocalDate endDate, String propertyId) {
        createAndSaveBlock("qwerty-1234", property1, LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15));

        Assertions.assertThat(blockRepository.hasOverlapping(propertyId, startDate, endDate))
                .isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideOverlapRanges")
    @DisplayName("when overlap is detected, returns true")
    void whenOverlapIsDetectedReturnsTrue(LocalDate startDate, LocalDate endDate, String propertyId) {
        createAndSaveBlock("qwerty-1234", property1, LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15));

        Assertions.assertThat(blockRepository.hasOverlapping(propertyId, startDate, endDate))
                .isTrue();
    }

    @Test
    @DisplayName("should delete block by external id provided")
    void deleteBlockByExternalId() {
        final String id = "qwerty-1234";
        createAndSaveBlock(id, property1, LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15));

        Assertions.assertThat(blockRepository.deleteByExternalId(id)).isEqualTo(1);
    }

    @Test
    @DisplayName("returns 0 when no record to delete")
    void returnsZeroWhenNoRecordToDelete() {
        Assertions.assertThat(blockRepository.deleteByExternalId("my-amazing")).isEqualTo(0);
    }

    @Test
    @DisplayName("should update a block by external id provided")
    void shouldUpdateBlock() {
        final String id = "qwerty-1234";
        final String newReason = "new-reason";
        final LocalDate newStartDate = LocalDate.of(2025, 10, 11);
        final LocalDate newEndDate = LocalDate.of(2025, 10, 12);
        final Long dbId = createAndSaveBlock(id, property1, LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15));

        final int updated = blockRepository.updateByExternalId(property2, newReason, newStartDate, newEndDate, id);
        entityManager.flush();
        entityManager.clear();

        final BlockEntity result = entityManager.find(BlockEntity.class, dbId);
        SoftAssertions.assertSoftly(softAssertions -> {
            Assertions.assertThat(updated).isEqualTo(1);
            Assertions.assertThat(result.getStartDate()).isEqualTo(newStartDate);
            Assertions.assertThat(result.getEndDate()).isEqualTo(newEndDate);
            Assertions.assertThat(result.getReason()).isEqualTo(newReason);
            Assertions.assertThat(result.getExternalId()).isEqualTo(id);
        });
    }

    @Test
    @DisplayName("when no record is found by id provided, don't update any record")
    void shouldNotUpdateAnyRecord() {
        final String id = "qwerty-1234";
        final String newReason = "new-reason";
        final LocalDate newStartDate = LocalDate.of(2025, 10, 11);
        final LocalDate newEndDate = LocalDate.of(2025, 10, 12);
        createAndSaveBlock(id, property1, LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15));

        final int updated =
                blockRepository.updateByExternalId(property2, newReason, newStartDate, newEndDate, "wow-id");
        entityManager.flush();
        entityManager.clear();

        Assertions.assertThat(updated).isEqualTo(0);
    }

    @Test
    @DisplayName("returns true when a record exists by external id")
    void shouldReturnTrueWhenExistsByExternalId() {
        final String id = "qwerty-1234";
        createAndSaveBlock(id, property1, LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15));

        Assertions.assertThat(blockRepository.existsByExternalId(id)).isTrue();
    }

    @Test
    @DisplayName("returns false when a record doesn't exist by external id")
    void shouldReturnFalseWhenExistsByExternalId() {
        final String id = "qwerty-1234";
        createAndSaveBlock(id, property1, LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15));

        Assertions.assertThat(blockRepository.existsByExternalId("wow-id")).isFalse();
    }

    @Test
    @DisplayName("when record is found by external id, returns Optional")
    void whenRecordIsFoundByExternalId() {
        final String id = "qwerty-1234";
        createAndSaveBlock(id, property1, LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15));

        final Optional<BlockEntity> optionalBlock = blockRepository.findByExternalId(id);

        SoftAssertions.assertSoftly(softAssertions -> {
            Assertions.assertThat(optionalBlock.isPresent()).isTrue();
            final BlockEntity result = optionalBlock.get();
            Assertions.assertThat(result.getProperty().getAlias()).isEqualTo(property1.getAlias());
            Assertions.assertThat(result.getProperty().getExternalId()).isEqualTo(property1.getExternalId());
            Assertions.assertThat(result.getProperty().getDescription()).isEqualTo(property1.getDescription());
            Assertions.assertThat(result.getExternalId()).isEqualTo(id);
        });
    }

    @Test
    @DisplayName("when record isn't found by external id, returns Optional empty")
    void whenRecordIsNotFoundByExternalId() {
        final String id = "qwerty-1234";
        createAndSaveBlock(id, property1, LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15));

        final Optional<BlockEntity> optionalBlock = blockRepository.findByExternalId("wow-id");

        Assertions.assertThat(optionalBlock.isPresent()).isFalse();
    }

    private Long createAndSaveBlock(
            final String id, final PropertyEntity property, final LocalDate startDate, final LocalDate endDate) {
        final BlockEntity block = new BlockEntity(id, property, "painting", startDate, endDate);
        entityManager.persist(block);
        entityManager.flush();
        return block.getId();
    }

    private static Stream<Arguments> provideRanges() {
        return Stream.of(
                arguments(LocalDate.of(2025, 1, 16), LocalDate.of(2025, 1, 20), "PROP-001"),
                arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 4), "PROP-001"),
                arguments(LocalDate.of(2025, 1, 20), LocalDate.of(2025, 1, 29), "PROP-001"),
                arguments(LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 15), "PROP-002"));
    }

    private static Stream<Arguments> provideOverlapRanges() {
        return Stream.of(
                arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 6), "PROP-001"),
                arguments(LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 10), "PROP-001"),
                arguments(LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15), "PROP-001"),
                arguments(LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 16), "PROP-001"));
    }
}
