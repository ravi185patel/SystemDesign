package com.example.distributedcache.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "Name is required")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(
                value = "0.0",
                message = "Price cannot be negative"
        )
        BigDecimal price

) {
}