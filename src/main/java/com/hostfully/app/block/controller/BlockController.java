package com.hostfully.app.block.controller;

import com.hostfully.app.block.controller.dto.BlockRequest;
import com.hostfully.app.block.domain.Block;
import com.hostfully.app.block.usecase.CreateBlock;
import com.hostfully.app.block.usecase.CreateBlock.CreateBlockCommand;
import com.hostfully.app.block.usecase.DeleteBlock;
import com.hostfully.app.block.usecase.UpdateBlock;
import com.hostfully.app.block.usecase.UpdateBlock.UpdateBlockCommand;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/blocks")
@AllArgsConstructor
public class BlockController {

    private CreateBlock createBlock;
    private DeleteBlock deleteBlock;
    private UpdateBlock updateBlock;

    @PostMapping
    public ResponseEntity<Block> createBlock(
            @Valid @RequestBody BlockRequest request,
            @RequestHeader(value = "Idempotency-Key") final UUID idempotencyKey) {
        final Block block = createBlock.execute(new CreateBlockCommand(
                request.property(), request.reason(), request.startDate(), request.endDate(), idempotencyKey));
        return ResponseEntity.status(HttpStatus.CREATED).body(block);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        deleteBlock.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Block> updateBlock(@Valid @RequestBody BlockRequest request, @PathVariable String id) {
        final Block block = updateBlock.execute(new UpdateBlockCommand(
                id, request.property(), request.reason(), request.startDate(), request.endDate()));
        return ResponseEntity.status(HttpStatus.OK).body(block);
    }
}
