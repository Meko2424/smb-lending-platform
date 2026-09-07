package com.lending.platform.underwriting.dto.response;

import com.lending.platform.underwriting.entity.RiskRating;
import com.lending.platform.underwriting.entity.UnderwritingReviewStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UnderwritingReviewResponse(
        Long id,
        Long applicationId,
        String applicationNumber,

        UnderwritingReviewStatus status,

        Long underwriterUserId,
        String underwriterUserName,

        BigDecimal requestedAmount,
        BigDecimal annualRevenue,
        BigDecimal existingDebt,
        BigDecimal monthlyDebtService,
        BigDecimal monthlyCashFlow,
        BigDecimal debtServiceCoverageRatio,

        RiskRating riskRating,

        String strengths,
        String weaknesses,
        String riskFactors,
        String mitigants,
        String financialAnalysis,
        String recommendationNotes,

        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
