package com.lending.platform.dto.response;

import com.lending.platform.entity.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationStatusHistoryResponse(
        Long id,
        Long applicationId,
        ApplicationStatus fromStatus,
        ApplicationStatus toStatus,
        Long changedByUserId,
        String changedByUserName,
        String comment,
        LocalDateTime changedAt
) {
}