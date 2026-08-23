package com.lending.platform.dto.response;

import com.lending.platform.entity.BusinessType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BusinessResponse(
        Long id,
        String legalName,
        String dbaName,
        String ein,
        BusinessType businessType,
        String industry,
        String naicsCode,
        LocalDate establishedDate,
        String phone,
        String email,
        String website,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        BigDecimal annualRevenue,
        Integer employeeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}