package com.lending.platform.dto.request;

import jakarta.validation.constraints.NotNull;

public record ApplicationAssignmentRequest(

        @NotNull(message = "User id is required")
        Long userId

) {
}
