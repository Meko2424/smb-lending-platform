package com.lending.platform.dto.eligibility;

import com.lending.platform.entity.EligibilityCriterionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EligibilityCriterionRequest(

        @NotNull(message = "Criterion status is required")
        EligibilityCriterionStatus status,

        @Size(
                max = 1000,
                message = "Notes must not exceed 1000 characters"
        )
        String notes
) {
}
