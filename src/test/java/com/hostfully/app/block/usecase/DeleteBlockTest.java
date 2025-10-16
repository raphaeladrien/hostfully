package com.hostfully.app.block.usecase;

import static org.mockito.Mockito.*;

import com.hostfully.app.block.exceptions.BlockGenericException;
import com.hostfully.app.infra.repository.BlockRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DeleteBlockTest {

    private final BlockRepository blockRepository = mock(BlockRepository.class);
    private final DeleteBlock subject = new DeleteBlock(blockRepository);

    @Test
    @DisplayName("should delete a block, when a ID is provided")
    void shouldDeleteABlock() {
        final String externalId = "a-id-spec";
        when(blockRepository.deleteByExternalId(externalId)).thenReturn(1);
        Assertions.assertTrue(subject.execute(externalId));
        ;
    }

    @Test
    @DisplayName("should return false, when no record is found")
    void shouldReturnFalse() {
        final String externalId = "a-id-spec";
        when(blockRepository.deleteByExternalId(externalId)).thenReturn(0);
        Assertions.assertFalse(subject.execute(externalId));
        ;
    }

    @Test
    @DisplayName("throws BlockGenericException, when an unexpected exception occurred")
    void throwsBlockGenericException() {
        final String externalId = "a-id-spec";
        when(blockRepository.deleteByExternalId(externalId)).thenThrow(new RuntimeException("an-error"));
        Assertions.assertThrows(BlockGenericException.class, () -> subject.execute(externalId));
    }
}
