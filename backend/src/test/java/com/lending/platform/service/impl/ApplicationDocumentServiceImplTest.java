package com.lending.platform.service.impl;

import com.lending.platform.dto.request.ApplicationDocumentRequest;
import com.lending.platform.dto.request.DocumentReceivedRequest;
import com.lending.platform.dto.response.ApplicationDocumentResponse;
import com.lending.platform.entity.*;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.mapper.ApplicationDocumentMapper;
import com.lending.platform.repository.ApplicationDocumentRepository;
import com.lending.platform.repository.LoanApplicationRepository;
import com.lending.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentServiceImplTest {

    @Mock
    private ApplicationDocumentRepository documentRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private UserRepository userRepository;

    private ApplicationDocumentMapper documentMapper;
    private ApplicationDocumentServiceImpl documentService;

    @BeforeEach
    void setUp() {
        documentMapper = new ApplicationDocumentMapper();

        documentService = new ApplicationDocumentServiceImpl(
                documentRepository,
                loanApplicationRepository,
                userRepository,
                documentMapper
        );
    }

    @Test
    void requestDocument_shouldCreateRequestedDocument() {

        LoanApplication application = createApplication(10L);

        ApplicationDocumentRequest request =
                new ApplicationDocumentRequest(
                        DocumentType.BUSINESS_TAX_RETURN,
                        null,
                        null
                );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(documentRepository.existsByApplicationIdAndDocumentType(
                10L,
                DocumentType.BUSINESS_TAX_RETURN
        )).thenReturn(false);

        when(documentRepository.save(any(ApplicationDocument.class)))
                .thenAnswer(invocation -> {
                    ApplicationDocument document =
                            invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            document,
                            "id",
                            100L
                    );

                    document.onCreate();

                    return document;
                });

        ApplicationDocumentResponse response =
                documentService.requestDocument(
                        10L,
                        request
                );

        assertEquals(100L, response.id());
        assertEquals(10L, response.applicationId());
        assertEquals(
                DocumentType.BUSINESS_TAX_RETURN,
                response.documentType()
        );
        assertEquals(
                DocumentStatus.REQUESTED,
                response.status()
        );
        assertNotNull(response.requestedAt());

        verify(documentRepository)
                .save(any(ApplicationDocument.class));
    }

    @Test
    void requestDocument_shouldRejectDuplicateType() {

        LoanApplication application = createApplication(10L);

        ApplicationDocumentRequest request =
                new ApplicationDocumentRequest(
                        DocumentType.BANK_STATEMENTS,
                        null,
                        null
                );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(documentRepository.existsByApplicationIdAndDocumentType(
                10L,
                DocumentType.BANK_STATEMENTS
        )).thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> documentService.requestDocument(
                        10L,
                        request
                )
        );

        verify(documentRepository, never())
                .save(any());
    }

    @Test
    void requestDocument_shouldThrowWhenApplicationMissing() {

        when(loanApplicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> documentService.requestDocument(
                        999L,
                        new ApplicationDocumentRequest(
                                DocumentType.BANK_STATEMENTS,
                                null,
                                null
                        )
                )
        );

        verify(documentRepository, never())
                .save(any());
    }

    @Test
    void markReceived_shouldMoveRequestedToReceived() {

        ApplicationDocument document =
                createDocument(
                        100L,
                        10L,
                        DocumentStatus.REQUESTED
                );

        when(documentRepository.findById(100L))
                .thenReturn(Optional.of(document));

        when(documentRepository.save(document))
                .thenReturn(document);

        ApplicationDocumentResponse response =
                documentService.markReceived(
                        10L,
                        100L,
                        new DocumentReceivedRequest(
                                "2025-tax-return.pdf",
                                "applications/10/documents/2025-tax-return.pdf"
                        )
                );

        assertEquals(
                DocumentStatus.RECEIVED,
                response.status()
        );

        assertEquals(
                "2025-tax-return.pdf",
                response.fileName()
        );

        assertNotNull(response.receivedAt());
        assertNotNull(response.updatedAt());
    }

    @Test
    void markReceived_shouldRejectNonRequestedDocument() {

        ApplicationDocument document =
                createDocument(
                        100L,
                        10L,
                        DocumentStatus.RECEIVED
                );

        when(documentRepository.findById(100L))
                .thenReturn(Optional.of(document));

        assertThrows(
                ResourceConflictException.class,
                () -> documentService.markReceived(
                        10L,
                        100L,
                        new DocumentReceivedRequest(
                                "replacement.pdf",
                                "applications/10/documents/replacement.pdf"
                        )
                )
        );

        verify(documentRepository, never())
                .save(any());
    }

    @Test
    void beginReview_shouldMoveReceivedToUnderReview() {

        ApplicationDocument document =
                createDocument(
                        100L,
                        10L,
                        DocumentStatus.RECEIVED
                );

        when(documentRepository.findById(100L))
                .thenReturn(Optional.of(document));

        when(documentRepository.save(document))
                .thenReturn(document);

        ApplicationDocumentResponse response =
                documentService.beginReview(
                        10L,
                        100L
                );

        assertEquals(
                DocumentStatus.UNDER_REVIEW,
                response.status()
        );

        assertNotNull(response.updatedAt());
    }

    @Test
    void beginReview_shouldRejectNonReceivedDocument() {

        ApplicationDocument document =
                createDocument(
                        100L,
                        10L,
                        DocumentStatus.REQUESTED
                );

        when(documentRepository.findById(100L))
                .thenReturn(Optional.of(document));

        assertThrows(
                ResourceConflictException.class,
                () -> documentService.beginReview(
                        10L,
                        100L
                )
        );

        verify(documentRepository, never())
                .save(any());
    }

    @Test
    void acceptDocument_shouldAcceptUnderReviewDocument() {

        ApplicationDocument document =
                createDocument(
                        100L,
                        10L,
                        DocumentStatus.UNDER_REVIEW
                );

        User reviewer = createUser();

        when(documentRepository.findById(100L))
                .thenReturn(Optional.of(document));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(reviewer));

        when(documentRepository.save(document))
                .thenReturn(document);

        ApplicationDocumentResponse response =
                documentService.acceptDocument(
                        10L,
                        100L,
                        "admin@lending.local"
                );

        assertEquals(
                DocumentStatus.ACCEPTED,
                response.status()
        );

        assertEquals(
                reviewer.getId(),
                response.reviewedByUserId()
        );

        assertEquals(
                "System Administrator",
                response.reviewedByUserName()
        );

        assertNotNull(response.reviewedAt());
        assertNull(response.rejectionReason());
    }

    @Test
    void acceptDocument_shouldRejectInvalidStatus() {

        ApplicationDocument document =
                createDocument(
                        100L,
                        10L,
                        DocumentStatus.REQUESTED
                );

        when(documentRepository.findById(100L))
                .thenReturn(Optional.of(document));

        assertThrows(
                ResourceConflictException.class,
                () -> documentService.acceptDocument(
                        10L,
                        100L,
                        "admin@lending.local"
                )
        );

        verify(userRepository, never())
                .findByEmailIgnoreCase(anyString());

        verify(documentRepository, never())
                .save(any());
    }

    @Test
    void rejectDocument_shouldRejectUnderReviewDocument() {

        ApplicationDocument document =
                createDocument(
                        100L,
                        10L,
                        DocumentStatus.UNDER_REVIEW
                );

        User reviewer = createUser();

        when(documentRepository.findById(100L))
                .thenReturn(Optional.of(document));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(reviewer));

        when(documentRepository.save(document))
                .thenReturn(document);

        ApplicationDocumentResponse response =
                documentService.rejectDocument(
                        10L,
                        100L,
                        "Document is incomplete",
                        "admin@lending.local"
                );

        assertEquals(
                DocumentStatus.REJECTED,
                response.status()
        );

        assertEquals(
                "Document is incomplete",
                response.rejectionReason()
        );

        assertEquals(
                reviewer.getId(),
                response.reviewedByUserId()
        );

        assertNotNull(response.reviewedAt());
    }

    @Test
    void rejectDocument_shouldRequireReason() {

        ApplicationDocument document =
                createDocument(
                        100L,
                        10L,
                        DocumentStatus.UNDER_REVIEW
                );

        when(documentRepository.findById(100L))
                .thenReturn(Optional.of(document));

        assertThrows(
                ResourceConflictException.class,
                () -> documentService.rejectDocument(
                        10L,
                        100L,
                        "   ",
                        "admin@lending.local"
                )
        );

        verify(userRepository, never())
                .findByEmailIgnoreCase(anyString());

        verify(documentRepository, never())
                .save(any());
    }

    @Test
    void getDocumentById_shouldRejectWrongApplication() {

        ApplicationDocument document =
                createDocument(
                        100L,
                        10L,
                        DocumentStatus.REQUESTED
                );

        when(documentRepository.findById(100L))
                .thenReturn(Optional.of(document));

        assertThrows(
                ResourceNotFoundException.class,
                () -> documentService.getDocumentById(
                        20L,
                        100L
                )
        );
    }

    private ApplicationDocument createDocument(
            Long documentId,
            Long applicationId,
            DocumentStatus status
    ) {

        LoanApplication application =
                createApplication(applicationId);

        ApplicationDocument document =
                new ApplicationDocument();

        ReflectionTestUtils.setField(
                document,
                "id",
                documentId
        );

        document.setApplication(application);
        document.setDocumentType(
                DocumentType.BUSINESS_TAX_RETURN
        );
        document.setStatus(status);

        document.onCreate();
        document.setStatus(status);

        return document;
    }

    private LoanApplication createApplication(
            Long applicationId
    ) {

        Business business = new Business();

        ReflectionTestUtils.setField(
                business,
                "id",
                1L
        );

        business.setLegalName(
                "Atlanta Logistics LLC"
        );

        LoanApplication application =
                new LoanApplication();

        ReflectionTestUtils.setField(
                application,
                "id",
                applicationId
        );

        application.setBusiness(business);
        application.setApplicationNumber(
                "APP-2026-DOCS001"
        );
        application.setLoanProduct(
                LoanProduct.SBA_7A
        );
        application.setLoanPurpose(
                LoanPurpose.EQUIPMENT_PURCHASE
        );
        application.setRequestedAmount(
                new BigDecimal("350000.00")
        );
        application.setRequestedTermMonths(120);
        application.setStatus(
                ApplicationStatus.DOCUMENT_COLLECTION
        );

        application.onCreate();
        application.setStatus(
                ApplicationStatus.DOCUMENT_COLLECTION
        );

        return application;
    }

    private User createUser() {

        User user = new User();

        ReflectionTestUtils.setField(
                user,
                "id",
                1L
        );

        user.setFirstName("System");
        user.setLastName("Administrator");
        user.setEmail("admin@lending.local");
        user.setEnabled(true);

        return user;
    }
}
