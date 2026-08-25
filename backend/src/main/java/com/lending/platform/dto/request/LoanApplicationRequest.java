package com.lending.platform.dto.request;

import com.lending.platform.entity.LoanProduct;
import com.lending.platform.entity.LoanPurpose;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LoanApplicationRequest(

        @NotNull(message = "Business id is required")
        Long businessId,

        @NotNull(message = "Loan product is required")
        LoanProduct loanProduct,

        @NotNull(message = "Loan purpose is required")
        LoanPurpose loanPurpose,

        @NotNull(message = "Requested amount is required")
        @Positive(message = "Requested amount must be greater than zero")
        BigDecimal requestedAmount,

        @Positive(message = "Requested term must be greater than zero")
        Integer requestedTermMonths
) {
}
