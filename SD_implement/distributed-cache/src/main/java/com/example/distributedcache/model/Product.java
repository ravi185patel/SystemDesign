package com.example.distributedcache.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Product(

        Long id,

        String name,

        String description,

        BigDecimal price,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}