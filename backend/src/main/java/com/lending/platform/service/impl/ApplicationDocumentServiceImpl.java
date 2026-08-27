package com.lending.platform.service.impl;

import com.lending.platform.dto.request.ApplicationDocumentRequest;
import com.lending.platform.dto.response.ApplicationDocumentResponse;
import com.lending.platform.entity.ApplicationDocument;
import com.lending.platform.entity.DocumentStatus;
import com.lending.platform.entity.LoanApplication;
import com.lending.platform.entity.User;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.mapper.ApplicationDocumentMapper;
import com.lending.platform.repository.ApplicationDocumentRepository;
import com.lending.platform.repository.LoanApplicationRepository;
import com.lending.platform.repository.UserRepository;
import com.lending.platform.service.ApplicationDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ApplicationDocumentServiceImpl
        implements ApplicationDocumentService {

    private final ApplicationDocumentRepository documentRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final ApplicationDocumentMapper documentMapper;

    public ApplicationDocumentServiceImpl(
            ApplicationDocumentRepository documentRepository,
            LoanApplicationRepository loanApplicationRepository,
            UserRepository userRepository,
            ApplicationDocumentMapper documentMapper
    ) {
        this.documentRepository = documentRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.userRepository = userRepository;
        this.documentMapper = documentMapper;
    }

    @Override
    public ApplicationDocumentResponse requestDocument(
            Long applicationId,
            ApplicationDocumentRequest request
    ) {

        LoanApplication application =
                findApplication(applicationId);

        if (documentRepository.existsByApplicationIdAndDocumentType(
                applicationId,
                request.documentType()
        )) {
            throw new ResourceConflictException(
                    "Document type already exists for this application"
            );
        }

        ApplicationDocument document =
                documentMapper.toEntity(
                        request,
                        application
                );

        document.setStatus(DocumentStatus.REQUESTED);

        ApplicationDocument savedDocument =
                documentRepository.save(document);

        return documentMapper.toResponse(savedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationDocumentResponse> getDocuments(
            Long applicationId
    ) {

        findApplication(applicationId);

        return documentRepository
                .findAllByApplicationIdOrderByCreatedAtAsc(applicationId)
                .stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationDocumentResponse getDocumentById(
            Long applicationId,
            Long documentId
    ) {

        return documentMapper.toResponse(
                findDocumentForApplication(
                        applicationId,
                        documentId
                )
        );
    }

    @Override
    public ApplicationDocumentResponse markReceived(
            Long applicationId,
            Long documentId,
            ApplicationDocumentRequest request
    ) {

        ApplicationDocument document =
                findDocumentForApplication(
                        applicationId,
                        documentId
                );

        if (document.getStatus() != DocumentStatus.REQUESTED) {
            throw new ResourceConflictException(
                    "Only requested documents can be marked as received"
            );
        }

        document.setFileName(request.fileName());
        document.setStorageKey(request.storageKey());
        document.setStatus(DocumentStatus.RECEIVED);
        document.setReceivedAt(LocalDateTime.now());

        ApplicationDocument updatedDocument =
                documentRepository.save(document);

        return documentMapper.toResponse(updatedDocument);
    }

    @Override
    public ApplicationDocumentResponse beginReview(
            Long applicationId,
            Long documentId
    ) {

        ApplicationDocument document =
                findDocumentForApplication(
                        applicationId,
                        documentId
                );

        if (document.getStatus() != DocumentStatus.RECEIVED) {
            throw new ResourceConflictException(
                    "Only received documents can enter review"
            );
        }

        document.setStatus(DocumentStatus.UNDER_REVIEW);

        ApplicationDocument updatedDocument =
                documentRepository.save(document);

        return documentMapper.toResponse(updatedDocument);
    }

    @Override
    public ApplicationDocumentResponse acceptDocument(
            Long applicationId,
            Long documentId,
            String authenticatedEmail
    ) {

        ApplicationDocument document =
                findDocumentForApplication(
                        applicationId,
                        documentId
                );

        if (document.getStatus() != DocumentStatus.UNDER_REVIEW) {
            throw new ResourceConflictException(
                    "Only documents under review can be accepted"
            );
        }

        User reviewer = findUser(authenticatedEmail);

        document.setStatus(DocumentStatus.ACCEPTED);
        document.setReviewedByUser(reviewer);
        document.setReviewedAt(LocalDateTime.now());
        document.setRejectionReason(null);

        ApplicationDocument updatedDocument =
                documentRepository.save(document);

        return documentMapper.toResponse(updatedDocument);
    }

    @Override
    public ApplicationDocumentResponse rejectDocument(
            Long applicationId,
            Long documentId,
            String rejectionReason,
            String authenticatedEmail
    ) {

        ApplicationDocument document =
                findDocumentForApplication(
                        applicationId,
                        documentId
                );

        if (document.getStatus() != DocumentStatus.UNDER_REVIEW) {
            throw new ResourceConflictException(
                    "Only documents under review can be rejected"
            );
        }

        if (rejectionReason == null
                || rejectionReason.isBlank()) {
            throw new ResourceConflictException(
                    "Rejection reason is required"
            );
        }

        User reviewer = findUser(authenticatedEmail);

        document.setStatus(DocumentStatus.REJECTED);
        document.setReviewedByUser(reviewer);
        document.setReviewedAt(LocalDateTime.now());
        document.setRejectionReason(rejectionReason);

        ApplicationDocument updatedDocument =
                documentRepository.save(document);

        return documentMapper.toResponse(updatedDocument);
    }

    private LoanApplication findApplication(
            Long applicationId
    ) {

        return loanApplicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan application not found with id: "
                                        + applicationId
                        )
                );
    }

    private ApplicationDocument findDocumentForApplication(
            Long applicationId,
            Long documentId
    ) {

        ApplicationDocument document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Application document not found with id: "
                                                + documentId
                                )
                        );

        if (!document.getApplication()
                .getId()
                .equals(applicationId)) {

            throw new ResourceNotFoundException(
                    "Application document not found with id: "
                            + documentId
                            + " for application id: "
                            + applicationId
            );
        }

        return document;
    }

    private User findUser(
            String authenticatedEmail
    ) {

        return userRepository.findByEmailIgnoreCase(
                        authenticatedEmail
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }
}