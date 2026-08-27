package com.lending.platform.service;

import com.lending.platform.dto.request.ApplicationDocumentRequest;
import com.lending.platform.dto.response.ApplicationDocumentResponse;

import java.util.List;

public interface ApplicationDocumentService {

    ApplicationDocumentResponse requestDocument(
            Long applicationId,
            ApplicationDocumentRequest request
    );

    List<ApplicationDocumentResponse> getDocuments(
            Long applicationId
    );

    ApplicationDocumentResponse getDocumentById(
            Long applicationId,
            Long documentId
    );

    ApplicationDocumentResponse markReceived(
            Long applicationId,
            Long documentId,
            ApplicationDocumentRequest request
    );

    ApplicationDocumentResponse beginReview(
            Long applicationId,
            Long documentId
    );

    ApplicationDocumentResponse acceptDocument(
            Long applicationId,
            Long documentId,
            String authenticatedEmail
    );

    ApplicationDocumentResponse rejectDocument(
            Long applicationId,
            Long documentId,
            String rejectionReason,
            String authenticatedEmail
    );
}