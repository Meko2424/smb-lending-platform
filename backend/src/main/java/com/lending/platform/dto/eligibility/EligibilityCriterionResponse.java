package com.lending.platform.dto.eligibility;

import com.lending.platform.entity.EligibilityCriterionStatus;
import com.lending.platform.entity.EligibilityCriterionType;

import java.time.LocalDateTime;

public record EligibilityCriterionResponse(

        Long id,
        EligibilityCriterionType criterionType,
        EligibilityCriterionStatus status,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
