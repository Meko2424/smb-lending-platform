package com.lending.platform.credit.dto.response;

import com.lending.platform.credit.entity.CreditDecisionStatus;
import com.lending.platform.credit.entity.CreditDecisionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreditDecisionResponse(
        Long id,
        Long applicationId,
        String applicationNumber,

        CreditDecisionStatus status,
        CreditDecisionType decisionType,

        Long creditManagerUserId,
        String creditManagerUserName,

        BigDecimal requestedAmount,
        BigDecimal approvedAmount,

        Integer requestedTermMonths,
        Integer approvedTermMonths,

        BigDecimal interestRate,

        String conditions,
        String declineReason,
        String decisionNotes,

        LocalDateTime startedAt,
        LocalDateTime decidedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
