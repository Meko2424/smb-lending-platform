package com.lending.platform.underwriting.dto.request;

import com.lending.platform.underwriting.entity.RiskRating;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UnderwritingReviewRequest(

        @DecimalMin(
                value = "0.00",
                message = "Existing debt cannot be negative"
        )
        BigDecimal existingDebt,

        @DecimalMin(
                value = "0.00",
                message = "Monthly debt service cannot be negative"
        )
        BigDecimal monthlyDebtService,

        @DecimalMin(
                value = "0.00",
                message = "Monthly cash flow cannot be negative"
        )
        BigDecimal monthlyCashFlow,

        RiskRating riskRating,

        @Size(max = 3000)
        String strengths,

        @Size(max = 3000)
        String weaknesses,

        @Size(max = 3000)
        String riskFactors,

        @Size(max = 3000)
        String mitigants,

        @Size(max = 4000)
        String financialAnalysis,

        @Size(max = 4000)
        String recommendationNotes
) {
}