package com.lending.platform.dto.request;

import com.lending.platform.entity.DocumentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationDocumentRequest(

        @NotNull(message = "Document type is required")
        DocumentType documentType,

        @Size(
                max = 255,
                message = "File name must not exceed 255 characters"
        )
        String fileName,

        @Size(
                max = 500,
                message = "Storage key must not exceed 500 characters"
        )
        String storageKey
) {
}