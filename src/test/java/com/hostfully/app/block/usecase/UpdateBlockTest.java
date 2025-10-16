package com.hostfully.app.block.usecase;

import static org.mockito.Mockito.*;

import com.hostfully.app.block.domain.Block;
import com.hostfully.app.block.exceptions.*;
import com.hostfully.app.block.service.BlockDateValidationService;
import com.hostfully.app.block.usecase.UpdateBlock.UpdateBlockCommand;
import com.hostfully.app.infra.entity.BlockEntity;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.BlockRepository;
import com.hostfully.app.infra.repository.PropertyRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UpdateBlockTest {

    private final BlockRepository blockRepository = mock(BlockRepository.class);
    private final PropertyRepository propertyRepository = mock(PropertyRepository.class);
    private final BlockDateValidationService blockDateValidationService = mock(BlockDateValidationService.class);

    private final UpdateBlock subject =
            new UpdateBlock(blockRepository, propertyRepository, blockDateValidationService);

    private final LocalDate startDate = LocalDate.of(2025, 1, 15);
    private final LocalDate endDate = LocalDate.of(2025, 1, 16);
    private final String id = "qwerty-1234";
    private final String property = "prop-1";
    private final PropertyEntity propertyEntity = buildPropertyEntity(property);
    private final String reason = "piper maintenance";

    @Test
    @DisplayName("should update a block, when a command is provided")
    void shouldUpdateBlock() {
        final UpdateBlockCommand updateBlockCommand = new UpdateBlockCommand(id, property, reason, startDate, endDate);

        when(blockRepository.existsByExternalId(updateBlockCommand.id())).thenReturn(true);
        when(blockDateValidationService.hasValidDateRange(any(), any())).thenReturn(true);
        when(propertyRepository.findByExternalId(property)).thenReturn(Optional.of(propertyEntity));
        when(blockRepository.updateByExternalId(propertyEntity, reason, startDate, endDate, id))
                .thenReturn(1);
        when(blockRepository.findByExternalId(id))
                .thenReturn(Optional.of(buildBlockEntity(id, propertyEntity, reason, startDate, endDate)));

        final Block block = subject.execute(updateBlockCommand);

        SoftAssertions.assertSoftly(softAssertions -> {
            Assertions.assertThat(block.getId()).isEqualTo(id);
            Assertions.assertThat(block.getPropertyId()).isEqualTo(property);
            Assertions.assertThat(block.getReason()).isEqualTo(reason);
            Assertions.assertThat(block.getStartDate()).isEqualTo(startDate);
            Assertions.assertThat(block.getEndDate()).isEqualTo(endDate);
        });

        verify(blockRepository, times(1)).existsByExternalId(updateBlockCommand.id());
        verify(blockDateValidationService, times(1)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(1)).findByExternalId(property);
        verify(blockRepository, times(1)).updateByExternalId(propertyEntity, reason, startDate, endDate, id);
        verify(blockRepository, times(1)).findByExternalId(id);
    }

    @Test
    @DisplayName("when block isn't found by id provided, throws BlockNotFoundException")
    void throwsBlockNotFoundException() {
        final UpdateBlockCommand updateBlockCommand = new UpdateBlockCommand(id, property, reason, startDate, endDate);

        when(blockRepository.existsByExternalId(updateBlockCommand.id())).thenReturn(false);

        Assertions.assertThatThrownBy(() -> subject.execute(updateBlockCommand))
                .isInstanceOf(BlockNotFoundException.class);

        verify(blockRepository, times(1)).existsByExternalId(updateBlockCommand.id());
        verify(blockDateValidationService, times(0)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(0)).findByExternalId(property);
        verify(blockRepository, times(0)).updateByExternalId(propertyEntity, reason, startDate, endDate, id);
        verify(blockRepository, times(0)).findByExternalId(id);
    }

    @Test
    @DisplayName("when invalid date range is provided, throws InvalidDateRangeException")
    void throwsInvalidDateRangeException() {
        final LocalDate startDate = LocalDate.of(2025, 1, 17);
        final LocalDate endDate = LocalDate.of(2025, 1, 16);

        final UpdateBlockCommand updateBlockCommand = new UpdateBlockCommand(id, property, reason, startDate, endDate);

        when(blockRepository.existsByExternalId(updateBlockCommand.id())).thenReturn(true);
        when(blockDateValidationService.hasValidDateRange(any(), any()))
                .thenThrow(new InvalidDateRangeException("a error"));

        Assertions.assertThatThrownBy(() -> subject.execute(updateBlockCommand))
                .isInstanceOf(InvalidDateRangeException.class);

        verify(blockRepository, times(1)).existsByExternalId(updateBlockCommand.id());
        verify(blockDateValidationService, times(1)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(0)).findByExternalId(property);
        verify(blockRepository, times(0)).updateByExternalId(propertyEntity, reason, startDate, endDate, id);
        verify(blockRepository, times(0)).findByExternalId(id);
    }

    @Test
    @DisplayName("throws OverlapBlockException, when exists overlap between block")
    void throwsOverlapBlockException() {
        final UpdateBlockCommand updateBlockCommand = new UpdateBlockCommand(id, property, reason, startDate, endDate);

        when(blockRepository.existsByExternalId(updateBlockCommand.id())).thenReturn(true);
        when(blockDateValidationService.hasValidDateRange(any(), any()))
                .thenThrow(new OverlapBlockException("a error"));

        Assertions.assertThatThrownBy(() -> subject.execute(updateBlockCommand))
                .isInstanceOf(OverlapBlockException.class);

        verify(blockRepository, times(1)).existsByExternalId(updateBlockCommand.id());
        verify(blockDateValidationService, times(1)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(0)).findByExternalId(property);
        verify(blockRepository, times(0)).updateByExternalId(propertyEntity, reason, startDate, endDate, id);
        verify(blockRepository, times(0)).findByExternalId(id);
    }

    @Test
    @DisplayName("throws PropertyNotFoundException, when property isn't found by id provided")
    void throwsPropertyNotFoundException() {
        final UpdateBlockCommand updateBlockCommand = new UpdateBlockCommand(id, property, reason, startDate, endDate);

        when(blockRepository.existsByExternalId(updateBlockCommand.id())).thenReturn(true);
        when(blockDateValidationService.hasValidDateRange(any(), any())).thenReturn(true);
        when(propertyRepository.findByExternalId(property)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> subject.execute(updateBlockCommand))
                .isInstanceOf(PropertyNotFoundException.class);

        verify(blockRepository, times(1)).existsByExternalId(updateBlockCommand.id());
        verify(blockDateValidationService, times(1)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(1)).findByExternalId(property);
        verify(blockRepository, times(0)).updateByExternalId(propertyEntity, reason, startDate, endDate, id);
        verify(blockRepository, times(0)).findByExternalId(id);
    }

    @Test
    @DisplayName("throws BlockGenericException, when an unexpected exception occurred")
    void throwsBlockGenericException() {
        final UpdateBlockCommand updateBlockCommand = new UpdateBlockCommand(id, property, reason, startDate, endDate);

        when(blockRepository.existsByExternalId(updateBlockCommand.id())).thenReturn(true);
        when(blockDateValidationService.hasValidDateRange(any(), any())).thenReturn(true);
        when(propertyRepository.findByExternalId(property)).thenReturn(Optional.of(propertyEntity));
        when(blockRepository.updateByExternalId(propertyEntity, reason, startDate, endDate, id))
                .thenThrow(new RuntimeException("a error"));

        Assertions.assertThatThrownBy(() -> subject.execute(updateBlockCommand))
                .isInstanceOf(BlockGenericException.class);

        verify(blockRepository, times(1)).existsByExternalId(updateBlockCommand.id());
        verify(blockDateValidationService, times(1)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(1)).findByExternalId(property);
        verify(blockRepository, times(1)).updateByExternalId(propertyEntity, reason, startDate, endDate, id);
        verify(blockRepository, times(0)).findByExternalId(id);
    }

    @Test
    @DisplayName("throws BlockNotFoundException, when block isnt't found by id provided")
    void throwsBlockGenericExceptionWhenBlockIsFoundById() {
        final UpdateBlockCommand updateBlockCommand = new UpdateBlockCommand(id, property, reason, startDate, endDate);

        when(blockRepository.existsByExternalId(updateBlockCommand.id())).thenReturn(true);
        when(blockDateValidationService.hasValidDateRange(any(), any())).thenReturn(true);
        when(propertyRepository.findByExternalId(property)).thenReturn(Optional.of(propertyEntity));
        when(blockRepository.updateByExternalId(propertyEntity, reason, startDate, endDate, id))
                .thenReturn(1);
        when(blockRepository.findByExternalId(id)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> subject.execute(updateBlockCommand))
                .isInstanceOf(BlockNotFoundException.class);

        verify(blockRepository, times(1)).existsByExternalId(updateBlockCommand.id());
        verify(blockDateValidationService, times(1)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(1)).findByExternalId(property);
        verify(blockRepository, times(1)).updateByExternalId(propertyEntity, reason, startDate, endDate, id);
        verify(blockRepository, times(1)).findByExternalId(id);
    }

    private BlockEntity buildBlockEntity(
            final String externalId,
            PropertyEntity property,
            final String reason,
            final LocalDate startDate,
            final LocalDate endDate) {
        return new BlockEntity(externalId, property, reason, startDate, endDate);
    }

    private PropertyEntity buildPropertyEntity(String propertyId) {
        return new PropertyEntity(propertyId, "a-super-description", "a-alias");
    }

    private UpdateBlockCommand buildCommand(
            final String id,
            final String property,
            final String reason,
            final LocalDate startDate,
            final LocalDate endDate) {
        return new UpdateBlockCommand(id, property, reason, startDate, endDate);
    }
}
