package com.hostfully.app.block.usecase;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.hostfully.app.block.domain.Block;
import com.hostfully.app.block.exceptions.BlockGenericException;
import com.hostfully.app.block.exceptions.InvalidDateRangeException;
import com.hostfully.app.block.exceptions.OverlapBlockException;
import com.hostfully.app.block.exceptions.PropertyNotFoundException;
import com.hostfully.app.block.service.BlockDateValidationService;
import com.hostfully.app.block.usecase.CreateBlock.CreateBlockCommand;
import com.hostfully.app.infra.entity.BlockEntity;
import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.BlockRepository;
import com.hostfully.app.infra.repository.PropertyRepository;
import com.hostfully.app.shared.IdempotencyService;
import com.hostfully.app.shared.util.NanoIdGenerator;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

public class CreateBlockTest {

    private final String idGenerated = "12345-123456";
    final LocalDate startDate = LocalDate.of(2025, 4, 25);
    final LocalDate endDate = LocalDate.of(2025, 4, 30);
    final String propertyId = "prop001-orx";
    final String reason = "Dry wall maintenance";
    final PropertyEntity propertyEntity = buildPropertyEntity(propertyId);
    final UUID idempotencyKey = UUID.randomUUID();

    private final BlockRepository blockRepository = mock(BlockRepository.class);
    private final PropertyRepository propertyRepository = mock(PropertyRepository.class);
    private final NanoIdGenerator nanoIdGenerator = mock(NanoIdGenerator.class);
    private final IdempotencyService idempotencyService = mock(IdempotencyService.class);
    private final BlockDateValidationService blockDateValidationService = mock(BlockDateValidationService.class);

    private final CreateBlock subject = new CreateBlock(
            blockRepository, propertyRepository, nanoIdGenerator, idempotencyService, blockDateValidationService);

    @BeforeEach
    public void setup() {
        when(nanoIdGenerator.generateId()).thenReturn(idGenerated);
    }

    @Test
    @DisplayName("should create a block, when a command is provided")
    void createBlockTest() {
        final CreateBlockCommand command =
                new CreateBlockCommand(propertyId, reason, startDate, endDate, idempotencyKey);

        when(idempotencyService.getResponse(idempotencyKey, Block.class)).thenReturn(Optional.empty());
        when(blockDateValidationService.hasValidDateRange(any(), any())).thenReturn(true);
        when(propertyRepository.findByExternalId(propertyId)).thenReturn(Optional.of(propertyEntity));
        when(blockRepository.save(any())).thenReturn(build(propertyEntity, reason, startDate, endDate));

        final Block result = subject.execute(command);

        SoftAssertions.assertSoftly(assertion -> {
            assertion.assertThat(result).isNotNull();
            assertion.assertThat(result.getPropertyId()).isEqualTo(propertyEntity.getExternalId());
            assertion.assertThat(result.getReason()).isEqualTo(reason);
            assertion.assertThat(result.getStartDate()).isEqualTo(startDate);
            assertion.assertThat(result.getEndDate()).isEqualTo(endDate);
        });

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Block.class);
        verify(blockDateValidationService, times(1)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(1)).findByExternalId(propertyId);
        verify(blockRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("throws InvalidDateRangeException, when start date is greater than end date")
    void throwsInvalidDateRangeException() {
        final CreateBlockCommand command =
                new CreateBlockCommand(propertyId, reason, startDate, endDate, idempotencyKey);

        when(idempotencyService.getResponse(idempotencyKey, Block.class)).thenReturn(Optional.empty());
        when(blockDateValidationService.hasValidDateRange(any(), any())).thenThrow(new InvalidDateRangeException("an error"));

        Assertions.assertThrows(InvalidDateRangeException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Block.class);
        verify(blockDateValidationService, times(1)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(0)).findByExternalId(any());
        verify(blockRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("throws OverlapBlockException, when exists overlap between block")
    void throwsOverlapBlockException() {
        final String propertyId = "prop001-orx";
        final LocalDate startDate = LocalDate.of(2025, 4, 25);
        final LocalDate endDate = LocalDate.of(2025, 4, 30);
        final String reason = "Dry wall maintenance";
        final UUID idempotencyKey = UUID.randomUUID();

        final CreateBlockCommand command =
                new CreateBlockCommand(propertyId, reason, startDate, endDate, idempotencyKey);

        when(idempotencyService.getResponse(idempotencyKey, Block.class)).thenReturn(Optional.empty());
        when(blockDateValidationService.hasValidDateRange(any(), any())).thenThrow(new OverlapBlockException("an error"));

        Assertions.assertThrows(OverlapBlockException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Block.class);
        verify(blockDateValidationService, times(1)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(0)).findByExternalId(any());
        verify(blockRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("throws PropertyNotFoundException, when property isn't found by id provided")
    void throwsPropertyNotFoundException() {
        final String propertyId = "prop001-orx";
        final LocalDate startDate = LocalDate.of(2025, 4, 25);
        final LocalDate endDate = LocalDate.of(2025, 4, 30);
        final String reason = "Dry wall maintenance";
        final UUID idempotencyKey = UUID.randomUUID();

        final CreateBlockCommand command =
                new CreateBlockCommand(propertyId, reason, startDate, endDate, idempotencyKey);

        when(idempotencyService.getResponse(idempotencyKey, Block.class)).thenReturn(Optional.empty());
        when(blockDateValidationService.hasValidDateRange(any(), any())).thenReturn(true);
        when(propertyRepository.findByExternalId(propertyId)).thenReturn(Optional.empty());

        Assertions.assertThrows(PropertyNotFoundException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Block.class);
        verify(blockDateValidationService, times(1)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(1)).findByExternalId(propertyId);
        verify(blockRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("throws BlockGenericException, when an unexpected exception occurred")
    void throwsBlockGenericException() {
        final String propertyId = "prop001-orx";
        final LocalDate startDate = LocalDate.of(2025, 4, 25);
        final LocalDate endDate = LocalDate.of(2025, 4, 30);
        final String reason = "Dry wall maintenance";
        final PropertyEntity propertyEntity = buildPropertyEntity(propertyId);
        final UUID idempotencyKey = UUID.randomUUID();

        final CreateBlockCommand command =
                new CreateBlockCommand(propertyId, reason, startDate, endDate, idempotencyKey);

        when(idempotencyService.getResponse(idempotencyKey, Block.class)).thenReturn(Optional.empty());
        when(blockDateValidationService.hasValidDateRange(any(), any())).thenReturn(true);
        when(propertyRepository.findByExternalId(propertyId)).thenReturn(Optional.of(propertyEntity));
        when(blockRepository.save(any())).thenThrow(new RuntimeException("an exception"));

        Assertions.assertThrows(BlockGenericException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Block.class);
        verify(blockDateValidationService, times(1)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(1)).findByExternalId(propertyId);
        verify(blockRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("should return existing block response when idempotency key already exists")
    void shouldReturnBlockWhenIdempotencyKeyExists() {
        final String propertyId = "prop001-orx";
        final String reason = "Dry wall maintenance";
        final LocalDate startDate = LocalDate.of(2025, 4, 26);
        final LocalDate endDate = LocalDate.of(2025, 4, 30);
        final UUID idempotencyKey = UUID.randomUUID();
        final Block block = new Block(idGenerated, propertyId, reason, startDate, endDate);

        final CreateBlockCommand command =
                new CreateBlockCommand(propertyId, reason, startDate, endDate, idempotencyKey);

        when(idempotencyService.getResponse(idempotencyKey, Block.class)).thenReturn(Optional.of(block));

        final Block result = subject.execute(command);

        SoftAssertions.assertSoftly(assertion -> {
            assertion.assertThat(result).isNotNull();
            assertion.assertThat(result.getPropertyId()).isEqualTo(propertyId);
            assertion.assertThat(result.getReason()).isEqualTo(reason);
            assertion.assertThat(result.getStartDate()).isEqualTo(startDate);
            assertion.assertThat(result.getEndDate()).isEqualTo(endDate);
        });

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Block.class);
        verify(blockDateValidationService, times(0)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(0)).findByExternalId(propertyId);
        verify(blockRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("throw RuntimeException when idempotency service throws an exception")
    void throwsRuntimeException() {
        final String propertyId = "prop001-orx";
        final String reason = "Dry wall maintenance";
        final LocalDate startDate = LocalDate.of(2025, 4, 26);
        final LocalDate endDate = LocalDate.of(2025, 4, 30);
        final UUID idempotencyKey = UUID.randomUUID();

        final CreateBlockCommand command =
                new CreateBlockCommand(propertyId, reason, startDate, endDate, idempotencyKey);

        when(idempotencyService.getResponse(idempotencyKey, Block.class)).thenThrow(new RuntimeException("an error"));

        Assertions.assertThrows(RuntimeException.class, () -> subject.execute(command));

        verify(idempotencyService, times(1)).getResponse(idempotencyKey, Block.class);
        verify(blockDateValidationService, times(0)).hasValidDateRange(any(), any());
        verify(propertyRepository, times(0)).findByExternalId(propertyId);
        verify(blockRepository, times(0)).save(any());
    }

    private static Stream<Arguments> provideRanges() {
        return Stream.of(
                arguments(LocalDate.of(2025, 1, 15), LocalDate.of(2025, 1, 15)),
                arguments(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 5)));
    }

    private BlockEntity build(PropertyEntity propertyEntity, String reason, LocalDate startDate, LocalDate endDate) {
        return new BlockEntity(idGenerated, propertyEntity, reason, startDate, endDate);
    }

    private PropertyEntity buildPropertyEntity(String propertyId) {
        return new PropertyEntity(propertyId, "a-super-description", "a-alias");
    }
}
