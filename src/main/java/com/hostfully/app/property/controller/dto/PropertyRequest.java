package com.hostfully.app.property.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PropertyRequest(
        @NotNull @Size(max = 250, message = "Description is too long") String description,
        @NotNull @Size(max = 50, message = "Alias is too long") String alias) {}
