package com.hostfully.app.block.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.hostfully.app.block.domain.Block;
import com.hostfully.app.block.exceptions.InvalidDateRangeException;
import com.hostfully.app.block.exceptions.OverlapBlockException;
import com.hostfully.app.infra.repository.BlockRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BlockDateValidationServiceTest {

    private final BlockRepository blockRepository = mock(BlockRepository.class);
    private final BlockDateValidationService blockDateValidationService =
            new BlockDateValidationService(blockRepository);

    @Test
    @DisplayName("should return true when start date is before end date and no overlaps exist")
    void shouldReturnTrueWhenDateRangeIsValidAndNoOverlap() {
        final LocalDate startDate = LocalDate.of(2025, 1, 1);
        final LocalDate endDate = LocalDate.of(2025, 1, 10);
        final Block block = new Block("block-id", "prop-001", "a reason", startDate, endDate);

        when(blockRepository.hasOverlapping(block.getPropertyId(), startDate, endDate, null))
                .thenReturn(false);

        final Boolean result = blockDateValidationService.hasValidDateRange(block, false);

        assertTrue(result);
        verify(blockRepository, times(1)).hasOverlapping(block.getPropertyId(), startDate, endDate, null);
    }

    @Test
    @DisplayName("should throw InvalidDateRangeException when start date is after end date")
    void shouldThrowInvalidDateRangeExceptionWhenStartDateAfterEndDate() {
        final LocalDate startDate = LocalDate.of(2025, 2, 1);
        final LocalDate endDate = LocalDate.of(2025, 1, 1);
        final Block block = new Block("block-id", "prop-001", "a reason", startDate, endDate);

        assertThrows(InvalidDateRangeException.class, () -> blockDateValidationService.hasValidDateRange(block, false));

        verify(blockRepository, times(0)).hasOverlapping(block.getPropertyId(), startDate, endDate, null);
    }

    @Test
    @DisplayName("should allow equal start and end date (single-day block)")
    void shouldAllowEqualStartAndEndDate() {
        final LocalDate sameDate = LocalDate.of(2025, 5, 5);
        final Block block = new Block("block-id", "prop-001", "a reason", sameDate, sameDate);

        when(blockRepository.hasOverlapping(block.getPropertyId(), sameDate, sameDate, null))
                .thenReturn(false);

        final Boolean result = blockDateValidationService.hasValidDateRange(block, false);

        assertTrue(result);
        verify(blockRepository, times(1)).hasOverlapping(block.getPropertyId(), sameDate, sameDate, null);
    }

    @Test
    @DisplayName("should throw OverlapBlockException when overlapping block exists in repository")
    void shouldThrowOverlapBlockExceptionWhenOverlapExists() {
        final LocalDate startDate = LocalDate.of(2025, 3, 1);
        final LocalDate endDate = LocalDate.of(2025, 3, 5);
        final Block block = new Block("block-id", "prop-001", "a reason", startDate, endDate);

        when(blockRepository.hasOverlapping(block.getPropertyId(), startDate, endDate, null))
                .thenReturn(true);

        assertThrows(OverlapBlockException.class, () -> blockDateValidationService.hasValidDateRange(block, false));

        verify(blockRepository, times(1)).hasOverlapping(block.getPropertyId(), startDate, endDate, null);
    }
}
