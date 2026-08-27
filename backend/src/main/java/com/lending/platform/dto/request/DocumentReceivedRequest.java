package com.lending.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentReceivedRequest(

        @NotBlank(message = "File name is required")
        @Size(
                max = 255,
                message = "File name must not exceed 255 characters"
        )
        String fileName,

        @NotBlank(message = "Storage key is required")
        @Size(
                max = 500,
                message = "Storage key must not exceed 500 characters"
        )
        String storageKey
) {
}