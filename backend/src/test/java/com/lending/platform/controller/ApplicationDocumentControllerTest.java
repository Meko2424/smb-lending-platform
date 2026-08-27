package com.lending.platform.controller;

import com.lending.platform.AbstractIntegrationTest;
import com.lending.platform.dto.request.ApplicationDocumentRequest;
import com.lending.platform.dto.request.BusinessRequest;
import com.lending.platform.dto.request.DocumentReceivedRequest;
import com.lending.platform.dto.request.DocumentRejectionRequest;
import com.lending.platform.entity.*;
import com.lending.platform.mapper.BusinessMapper;
import com.lending.platform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationDocumentControllerTest
        extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationDocumentRepository documentRepository;

    @Autowired
    private ApplicationStatusHistoryRepository historyRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private BusinessOwnerRepository businessOwnerRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private BusinessMapper businessMapper;

    private Business business;
    private LoanApplication application;

    @BeforeEach
    void setUp() {
        documentRepository.deleteAll();
        historyRepository.deleteAll();
        loanApplicationRepository.deleteAll();
        businessOwnerRepository.deleteAll();
        businessRepository.deleteAll();

        business = businessRepository.save(
                businessMapper.toEntity(createBusinessRequest())
        );

        application = saveApplication();
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void requestDocument_shouldReturn201() throws Exception {

        ApplicationDocumentRequest request =
                new ApplicationDocumentRequest(
                        DocumentType.BUSINESS_TAX_RETURN,
                        null,
                        null
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/documents",
                                application.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.applicationId")
                        .value(application.getId()))
                .andExpect(jsonPath("$.documentType")
                        .value("BUSINESS_TAX_RETURN"))
                .andExpect(jsonPath("$.status")
                        .value("REQUESTED"))
                .andExpect(jsonPath("$.requestedAt")
                        .isNotEmpty());
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void requestDocument_shouldReturn409ForDuplicateType()
            throws Exception {

        saveDocument(
                application,
                DocumentType.BANK_STATEMENTS,
                DocumentStatus.REQUESTED
        );

        ApplicationDocumentRequest request =
                new ApplicationDocumentRequest(
                        DocumentType.BANK_STATEMENTS,
                        null,
                        null
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/documents",
                                application.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409))
                .andExpect(jsonPath("$.error")
                        .value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Document type already exists for this application"
                        ));
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void markReceived_shouldReturn200() throws Exception {

        ApplicationDocument document =
                saveDocument(
                        application,
                        DocumentType.BUSINESS_TAX_RETURN,
                        DocumentStatus.REQUESTED
                );

        DocumentReceivedRequest request =
                new DocumentReceivedRequest(
                        "2025-business-tax-return.pdf",
                        "applications/"
                                + application.getId()
                                + "/documents/2025-business-tax-return.pdf"
                );

        mockMvc.perform(
                        put(
                                "/api/v1/applications/{applicationId}/documents/{documentId}/received",
                                application.getId(),
                                document.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("RECEIVED"))
                .andExpect(jsonPath("$.fileName")
                        .value("2025-business-tax-return.pdf"))
                .andExpect(jsonPath("$.receivedAt")
                        .isNotEmpty())
                .andExpect(jsonPath("$.updatedAt")
                        .isNotEmpty());
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void beginReview_shouldReturn200() throws Exception {

        ApplicationDocument document =
                saveDocument(
                        application,
                        DocumentType.BUSINESS_TAX_RETURN,
                        DocumentStatus.RECEIVED
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/documents/{documentId}/review",
                                application.getId(),
                                document.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("UNDER_REVIEW"));
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void acceptDocument_shouldReturn200() throws Exception {

        ApplicationDocument document =
                saveDocument(
                        application,
                        DocumentType.BUSINESS_TAX_RETURN,
                        DocumentStatus.UNDER_REVIEW
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/documents/{documentId}/accept",
                                application.getId(),
                                document.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("ACCEPTED"))
                .andExpect(jsonPath("$.reviewedByUserId")
                        .exists())
                .andExpect(jsonPath("$.reviewedByUserName")
                        .value("System Administrator"))
                .andExpect(jsonPath("$.reviewedAt")
                        .isNotEmpty())
                .andExpect(jsonPath("$.rejectionReason")
                        .isEmpty());
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void rejectDocument_shouldReturn400ForBlankReason()
            throws Exception {

        ApplicationDocument document =
                saveDocument(
                        application,
                        DocumentType.BUSINESS_TAX_RETURN,
                        DocumentStatus.UNDER_REVIEW
                );

        DocumentRejectionRequest request =
                new DocumentRejectionRequest("");

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/documents/{documentId}/reject",
                                application.getId(),
                                document.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"));
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void getDocumentById_shouldReturn404ForWrongApplication()
            throws Exception {

        Business otherBusiness =
                businessRepository.save(
                        businessMapper.toEntity(
                                createSecondBusinessRequest()
                        )
                );

        LoanApplication otherApplication =
                saveApplicationForBusiness(
                        otherBusiness,
                        "APP-DOC-OTHER-"
                                + System.nanoTime()
                );

        ApplicationDocument document =
                saveDocument(
                        application,
                        DocumentType.BUSINESS_TAX_RETURN,
                        DocumentStatus.REQUESTED
                );

        mockMvc.perform(
                        get(
                                "/api/v1/applications/{applicationId}/documents/{documentId}",
                                otherApplication.getId(),
                                document.getId()
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"));
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void getDocumentById_shouldReturn404WhenDocumentMissing()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/applications/{applicationId}/documents/{documentId}",
                                application.getId(),
                                999999L
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Application document not found with id: 999999"
                        ));
    }

    @Test
    @WithMockUser(
            username = "viewer@lending.local",
            roles = "VIEWER"
    )
    void requestDocument_shouldReturn403ForUnauthorizedRole()
            throws Exception {

        ApplicationDocumentRequest request =
                new ApplicationDocumentRequest(
                        DocumentType.BANK_STATEMENTS,
                        null,
                        null
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/documents",
                                application.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getDocuments_shouldReturn401WithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/applications/{applicationId}/documents",
                                application.getId()
                        )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401))
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"));
    }

    private ApplicationDocument saveDocument(
            LoanApplication targetApplication,
            DocumentType type,
            DocumentStatus status
    ) {

        ApplicationDocument document =
                new ApplicationDocument();

        document.setApplication(targetApplication);
        document.setDocumentType(type);
        document.setStatus(status);

        if (status != DocumentStatus.REQUESTED) {
            document.setFileName(
                    "uploaded-document.pdf"
            );

            document.setStorageKey(
                    "applications/"
                            + targetApplication.getId()
                            + "/documents/uploaded-document.pdf"
            );

            document.setReceivedAt(
                    java.time.LocalDateTime.now()
            );
        }

        return documentRepository.save(document);
    }

    private LoanApplication saveApplication() {

        return saveApplicationForBusiness(
                business,
                "APP-DOC-" + System.nanoTime()
        );
    }

    private LoanApplication saveApplicationForBusiness(
            Business targetBusiness,
            String applicationNumber
    ) {

        LoanApplication loanApplication =
                new LoanApplication();

        loanApplication.setBusiness(targetBusiness);
        loanApplication.setApplicationNumber(
                applicationNumber
        );
        loanApplication.setLoanProduct(
                LoanProduct.SBA_7A
        );
        loanApplication.setLoanPurpose(
                LoanPurpose.EQUIPMENT_PURCHASE
        );
        loanApplication.setRequestedAmount(
                new BigDecimal("350000.00")
        );
        loanApplication.setRequestedTermMonths(
                120
        );
        loanApplication.setStatus(
                ApplicationStatus.DOCUMENT_COLLECTION
        );
        loanApplication.setSubmittedAt(
                java.time.LocalDateTime.now()
        );

        return loanApplicationRepository.save(
                loanApplication
        );
    }

    private BusinessRequest createBusinessRequest() {

        return new BusinessRequest(
                "Atlanta Logistics LLC",
                "Atlanta Logistics",
                "12-3456789",
                BusinessType.LLC,
                "Transportation",
                "484121",
                LocalDate.of(2019, 4, 15),
                "404-555-0188",
                "operations@atlantalogistics.com",
                "https://atlantalogistics.com",
                "1200 Peachtree Industrial Blvd",
                null,
                "Atlanta",
                "GA",
                "30309",
                new BigDecimal("2400000.00"),
                18
        );
    }

    private BusinessRequest createSecondBusinessRequest() {

        return new BusinessRequest(
                "Sunrise Equipment LLC",
                "Sunrise Equipment",
                "98-7654321",
                BusinessType.LLC,
                "Equipment Rental",
                "532412",
                LocalDate.of(2017, 8, 10),
                "305-555-0200",
                "operations@sunriseequipment.com",
                "https://sunriseequipment.com",
                "800 Industrial Way",
                null,
                "Miami",
                "FL",
                "33101",
                new BigDecimal("1800000.00"),
                12
        );
    }
}