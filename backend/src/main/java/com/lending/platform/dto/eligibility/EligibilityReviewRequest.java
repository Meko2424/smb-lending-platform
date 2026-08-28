package com.lending.platform.dto.eligibility;

import jakarta.validation.constraints.Size;

public record EligibilityReviewRequest(

        @Size(
                max = 2000,
                message = "Summary must not exceed 2000 characters"
        )
        String summary
) {
}
