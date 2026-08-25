package com.lending.platform.dto.response;

import com.lending.platform.entity.ApplicationStatus;
import com.lending.platform.entity.LoanProduct;
import com.lending.platform.entity.LoanPurpose;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanApplicationResponse(
        Long id,
        Long businessId,
        String businessName,
        String applicationNumber,
        LoanProduct loanProduct,
        LoanPurpose loanPurpose,
        BigDecimal requestedAmount,
        Integer requestedTermMonths,
        ApplicationStatus status,

        Long assignedLoanOfficerId,
        String assignedLoanOfficerName,

        Long assignedUnderwriterId,
        String assignedUnderwriterName,

        LocalDateTime submittedAt,
        LocalDateTime decisionAt,
        LocalDateTime fundedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}