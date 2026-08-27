package com.lending.platform.mapper;

import com.lending.platform.dto.request.ApplicationDocumentRequest;
import com.lending.platform.dto.response.ApplicationDocumentResponse;
import com.lending.platform.entity.ApplicationDocument;
import com.lending.platform.entity.LoanApplication;
import com.lending.platform.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ApplicationDocumentMapper {

    public ApplicationDocument toEntity(
            ApplicationDocumentRequest request,
            LoanApplication application
    ) {
        ApplicationDocument document = new ApplicationDocument();

        document.setApplication(application);
        document.setDocumentType(request.documentType());
        document.setFileName(request.fileName());
        document.setStorageKey(request.storageKey());

        return document;
    }

    public ApplicationDocumentResponse toResponse(
            ApplicationDocument document
    ) {
        User reviewer = document.getReviewedByUser();

        return new ApplicationDocumentResponse(
                document.getId(),
                document.getApplication().getId(),
                document.getDocumentType(),
                document.getStatus(),
                document.getFileName(),
                document.getStorageKey(),
                document.getRequestedAt(),
                document.getReceivedAt(),
                document.getReviewedAt(),
                reviewer != null ? reviewer.getId() : null,
                reviewer != null
                        ? reviewer.getFirstName() + " " + reviewer.getLastName()
                        : null,
                document.getRejectionReason(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
