package com.hostfully.app.infra.respository;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.hostfully.app.infra.entity.BlockEntity;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.BlockRepository;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @BeforeEach
    void setUp() {
        property1 = entityManager.persist(new PropertyEntity("PROP-001", "Beach House", "Beach baby!"));
        final PropertyEntity property2 =
                entityManager.persist(new PropertyEntity("PROP-002", "Mountain Cabin", "Relax time"));

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

    private void createAndSaveBlock(
            final String id, final PropertyEntity property, final LocalDate startDate, final LocalDate endDate) {
        final BlockEntity block = new BlockEntity(id, property, "painting", startDate, endDate);
        entityManager.persist(block);
        entityManager.flush();
    }

    private static Stream<Arguments> provideRanges() {
        return Stream.of(
                arguments(LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 20), "PROP-001"),
                arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5), "PROP-001"),
                arguments(LocalDate.of(2025, 1, 20), LocalDate.of(2025, 1, 29), "PROP-001"),
                arguments(LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15), "PROP-002"));
    }

    private static Stream<Arguments> provideOverlapRanges() {
        return Stream.of(
                arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 6), "PROP-001"),
                arguments(LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 10), "PROP-001"),
                arguments(LocalDate.of(2025, 1, 5), LocalDate.of(2025, 1, 15), "PROP-001"));
    }
}
