package com.lending.platform.credit.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreditDeclineRequest(

        @NotBlank(message = "Decline reason is required")
        @Size(
                max = 2000,
                message = "Decline reason must not exceed 2000 characters"
        )
        String declineReason,

        @Size(
                max = 4000,
                message = "Decision notes must not exceed 4000 characters"
        )
        String decisionNotes
) {
}