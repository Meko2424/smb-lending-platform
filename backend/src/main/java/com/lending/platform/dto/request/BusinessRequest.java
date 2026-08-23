package com.lending.platform.dto.request;

import com.lending.platform.entity.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BusinessRequest(

        @NotBlank(message = "Legal name is required")
        @Size(max = 255, message = "Legal name must not exceed 255 characters")
        String legalName,

        @Size(max = 255, message = "DBA name must not exceed 255 characters")
        String dbaName,

        @NotBlank(message = "EIN is required")
        @Pattern(
                regexp = "^\\d{2}-?\\d{7}$",
                message = "EIN must contain 9 digits"
        )
        String ein,

        @NotNull(message = "Business type is required")
        BusinessType businessType,

        @NotBlank(message = "Industry is required")
        @Size(max = 150, message = "Industry must not exceed 150 characters")
        String industry,

        @Pattern(
                regexp = "^\\d{6}$",
                message = "NAICS code must contain 6 digits"
        )
        String naicsCode,

        LocalDate establishedDate,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,

        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @Size(max = 255, message = "Website must not exceed 255 characters")
        String website,

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
        String addressLine1,

        @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
        String addressLine2,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @NotBlank(message = "State is required")
        @Size(max = 50, message = "State must not exceed 50 characters")
        String state,

        @NotBlank(message = "Postal code is required")
        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        String postalCode,

        @PositiveOrZero(message = "Annual revenue cannot be negative")
        BigDecimal annualRevenue,

        @Min(value = 0, message = "Employee count cannot be negative")
        Integer employeeCount
) {
}
