package com.lending.platform.controller;

import com.lending.platform.dto.request.ApplicationDocumentRequest;
import com.lending.platform.dto.request.DocumentReceivedRequest;
import com.lending.platform.dto.request.DocumentRejectionRequest;
import com.lending.platform.dto.response.ApplicationDocumentResponse;
import com.lending.platform.service.ApplicationDocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/documents")
public class ApplicationDocumentController {

    private final ApplicationDocumentService applicationDocumentService;

    public ApplicationDocumentController(
            ApplicationDocumentService applicationDocumentService
    ) {
        this.applicationDocumentService = applicationDocumentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR')")
    public ResponseEntity<ApplicationDocumentResponse> requestDocument(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationDocumentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        applicationDocumentService.requestDocument(
                                applicationId,
                                request
                        )
                );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', 'UNDERWRITER', 'CREDIT_MANAGER', 'CLOSING_SPECIALIST')")
    public ResponseEntity<List<ApplicationDocumentResponse>> getDocuments(
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(
                applicationDocumentService.getDocuments(
                        applicationId
                )
        );
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', 'UNDERWRITER', 'CREDIT_MANAGER', 'CLOSING_SPECIALIST')")
    public ResponseEntity<ApplicationDocumentResponse> getDocumentById(
            @PathVariable Long applicationId,
            @PathVariable Long documentId
    ) {
        return ResponseEntity.ok(
                applicationDocumentService.getDocumentById(
                        applicationId,
                        documentId
                )
        );
    }

    @PutMapping("/{documentId}/received")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR')")
    public ResponseEntity<ApplicationDocumentResponse> markReceived(
            @PathVariable Long applicationId,
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentReceivedRequest request
    ) {
        return ResponseEntity.ok(
                applicationDocumentService.markReceived(
                        applicationId,
                        documentId,
                        request
                )
        );
    }

    @PostMapping("/{documentId}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCESSOR', 'UNDERWRITER')")
    public ResponseEntity<ApplicationDocumentResponse> beginReview(
            @PathVariable Long applicationId,
            @PathVariable Long documentId
    ) {
        return ResponseEntity.ok(
                applicationDocumentService.beginReview(
                        applicationId,
                        documentId
                )
        );
    }

    @PostMapping("/{documentId}/accept")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCESSOR', 'UNDERWRITER')")
    public ResponseEntity<ApplicationDocumentResponse> acceptDocument(
            @PathVariable Long applicationId,
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                applicationDocumentService.acceptDocument(
                        applicationId,
                        documentId,
                        authentication.getName()
                )
        );
    }

    @PostMapping("/{documentId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROCESSOR', 'UNDERWRITER')")
    public ResponseEntity<ApplicationDocumentResponse> rejectDocument(
            @PathVariable Long applicationId,
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentRejectionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                applicationDocumentService.rejectDocument(
                        applicationId,
                        documentId,
                        request.rejectionReason(),
                        authentication.getName()
                )
        );
    }
}