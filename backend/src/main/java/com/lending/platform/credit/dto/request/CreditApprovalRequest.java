package com.lending.platform.credit.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreditApprovalRequest(

        @NotNull(message = "Approved amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Approved amount must be greater than zero"
        )
        BigDecimal approvedAmount,

        @NotNull(message = "Approved term is required")
        @Positive(message = "Approved term must be greater than zero")
        Integer approvedTermMonths,

        @NotNull(message = "Interest rate is required")
        @DecimalMin(
                value = "0.00",
                message = "Interest rate cannot be negative"
        )
        BigDecimal interestRate,

        @Size(
                max = 4000,
                message = "Conditions must not exceed 4000 characters"
        )
        String conditions,

        @Size(
                max = 4000,
                message = "Decision notes must not exceed 4000 characters"
        )
        String decisionNotes
) {
}
