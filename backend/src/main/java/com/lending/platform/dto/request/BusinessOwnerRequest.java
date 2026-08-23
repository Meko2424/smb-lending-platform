package com.lending.platform.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BusinessOwnerRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @Size(max = 100, message = "Title must not exceed 100 characters")
        String title,

        @NotNull(message = "Ownership percentage is required")
        @DecimalMin(
                value = "0.00",
                message = "Ownership percentage cannot be negative"
        )
        @DecimalMax(
                value = "100.00",
                message = "Ownership percentage cannot exceed 100"
        )
        BigDecimal ownershipPercentage,

        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        boolean guarantor
) {
}