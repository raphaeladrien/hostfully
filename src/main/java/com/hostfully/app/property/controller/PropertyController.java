package com.hostfully.app.property.controller;

import com.hostfully.app.property.controller.dto.PropertyRequest;
import com.hostfully.app.property.domain.Property;
import com.hostfully.app.property.usecase.CreateProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/properties")
@AllArgsConstructor
public class PropertyController {

    private final CreateProperty createProperty;

    @PostMapping
    public ResponseEntity<Property> createBlock(@Valid @RequestBody PropertyRequest request) {
        final Property property = createProperty.execute(
                new CreateProperty.CreatePropertyCommand(request.description(), request.alias()));

        // Per RFC 7231 POST may return 201 created with location have, none sent here, once we don't have a get
        // endpoint defined
        return ResponseEntity.status(HttpStatus.CREATED).body(property);
    }
}
