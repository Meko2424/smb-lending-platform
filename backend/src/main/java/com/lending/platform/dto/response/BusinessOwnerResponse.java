package com.lending.platform.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BusinessOwnerResponse(
        Long id,
        Long businessId,
        String firstName,
        String lastName,
        String title,
        BigDecimal ownershipPercentage,
        String email,
        String phone,
        boolean guarantor,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}