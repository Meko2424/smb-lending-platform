package com.lending.platform.dto.request;

import com.lending.platform.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationStatusTransitionRequest(

        @NotNull(message = "Target status is required")
        ApplicationStatus targetStatus,

        @Size(
                max = 1000,
                message = "Comment must not exceed 1000 characters"
        )
        String comment
) {
}
