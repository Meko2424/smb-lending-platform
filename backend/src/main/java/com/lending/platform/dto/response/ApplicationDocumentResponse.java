package com.lending.platform.dto.response;

import com.lending.platform.entity.DocumentStatus;
import com.lending.platform.entity.DocumentType;

import java.time.LocalDateTime;

public record ApplicationDocumentResponse(
        Long id,
        Long applicationId,
        DocumentType documentType,
        DocumentStatus status,
        String fileName,
        String storageKey,
        LocalDateTime requestedAt,
        LocalDateTime receivedAt,
        LocalDateTime reviewedAt,
        Long reviewedByUserId,
        String reviewedByUserName,
        String rejectionReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}