package com.lending.platform.dto.eligibility;

import com.lending.platform.entity.EligibilityReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

public record EligibilityReviewResponse(

        Long id,
        Long applicationId,
        EligibilityReviewStatus status,

        Long reviewedByUserId,
        String reviewedByUserName,

        LocalDateTime startedAt,
        LocalDateTime completedAt,

        String summary,

        List<EligibilityCriterionResponse> criteria,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}