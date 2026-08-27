package com.lending.platform.controller;

import com.lending.platform.AbstractIntegrationTest;
import com.lending.platform.dto.request.ApplicationStatusTransitionRequest;
import com.lending.platform.dto.request.BusinessRequest;
import com.lending.platform.entity.ApplicationStatus;
import com.lending.platform.entity.Business;
import com.lending.platform.entity.BusinessType;
import com.lending.platform.entity.LoanApplication;
import com.lending.platform.entity.LoanProduct;
import com.lending.platform.entity.LoanPurpose;
import com.lending.platform.mapper.BusinessMapper;
import com.lending.platform.repository.ApplicationStatusHistoryRepository;
import com.lending.platform.repository.BusinessOwnerRepository;
import com.lending.platform.repository.BusinessRepository;
import com.lending.platform.repository.LoanApplicationRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationWorkflowControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @BeforeEach
    void setUp() {

        /*
         * Delete child records before parent records because of
         * foreign-key relationships.
         */
        historyRepository.deleteAll();
        loanApplicationRepository.deleteAll();
        businessOwnerRepository.deleteAll();
        businessRepository.deleteAll();

        business = businessRepository.save(
                businessMapper.toEntity(
                        createBusinessRequest()
                )
        );
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void transitionStatus_shouldMoveDraftToSubmitted()
            throws Exception {

        LoanApplication application =
                saveApplication(ApplicationStatus.DRAFT);

        ApplicationStatusTransitionRequest request =
                new ApplicationStatusTransitionRequest(
                        ApplicationStatus.SUBMITTED,
                        "Application ready for processing"
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/workflow/transition",
                                application.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("SUBMITTED"))
                .andExpect(jsonPath("$.submittedAt")
                        .isNotEmpty());
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void transitionStatus_shouldMoveSubmittedToDocumentCollection()
            throws Exception {

        LoanApplication application =
                saveApplication(
                        ApplicationStatus.SUBMITTED
                );

        ApplicationStatusTransitionRequest request =
                new ApplicationStatusTransitionRequest(
                        ApplicationStatus.DOCUMENT_COLLECTION,
                        "Begin document collection"
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/workflow/transition",
                                application.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("DOCUMENT_COLLECTION"));
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void transitionStatus_shouldReturn409ForInvalidTransition()
            throws Exception {

        LoanApplication application =
                saveApplication(ApplicationStatus.DRAFT);

        ApplicationStatusTransitionRequest request =
                new ApplicationStatusTransitionRequest(
                        ApplicationStatus.UNDERWRITING,
                        "Skip directly to underwriting"
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/workflow/transition",
                                application.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409))
                .andExpect(jsonPath("$.error")
                        .value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Invalid application status transition from DRAFT to UNDERWRITING"
                        ));
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void getHistory_shouldReturnWorkflowHistory()
            throws Exception {

        LoanApplication application =
                saveApplication(ApplicationStatus.DRAFT);

        transition(
                application.getId(),
                ApplicationStatus.SUBMITTED,
                "Application submitted"
        );

        transition(
                application.getId(),
                ApplicationStatus.DOCUMENT_COLLECTION,
                "Document collection started"
        );

        mockMvc.perform(
                        get(
                                "/api/v1/applications/{applicationId}/workflow/history",
                                application.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))

                .andExpect(jsonPath("$[0].fromStatus")
                        .value("DRAFT"))
                .andExpect(jsonPath("$[0].toStatus")
                        .value("SUBMITTED"))
                .andExpect(jsonPath("$[0].comment")
                        .value("Application submitted"))
                .andExpect(jsonPath("$[0].changedByUserId")
                        .exists())
                .andExpect(jsonPath("$[0].changedByUserName")
                        .value("System Administrator"))

                .andExpect(jsonPath("$[1].fromStatus")
                        .value("SUBMITTED"))
                .andExpect(jsonPath("$[1].toStatus")
                        .value("DOCUMENT_COLLECTION"))
                .andExpect(jsonPath("$[1].comment")
                        .value("Document collection started"));
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void transitionStatus_shouldReturn404WhenApplicationDoesNotExist()
            throws Exception {

        ApplicationStatusTransitionRequest request =
                new ApplicationStatusTransitionRequest(
                        ApplicationStatus.SUBMITTED,
                        null
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/workflow/transition",
                                999999L
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Loan application not found with id: 999999"
                        ));
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void transitionStatus_shouldReturn400ForInvalidRequest()
            throws Exception {

        LoanApplication application =
                saveApplication(ApplicationStatus.DRAFT);

        ApplicationStatusTransitionRequest request =
                new ApplicationStatusTransitionRequest(
                        null,
                        "Missing target status"
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/workflow/transition",
                                application.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"));
    }

    @Test
    void getHistory_shouldReturn401WithoutAuthentication()
            throws Exception {

        LoanApplication application =
                saveApplication(ApplicationStatus.DRAFT);

        mockMvc.perform(
                        get(
                                "/api/v1/applications/{applicationId}/workflow/history",
                                application.getId()
                        )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401))
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"));
    }

    @Test
    @WithMockUser(
            username = "unauthorized@lending.local",
            roles = "VIEWER"
    )
    void transitionStatus_shouldReturn403ForUnauthorizedRole()
            throws Exception {

        LoanApplication application =
                saveApplication(ApplicationStatus.DRAFT);

        ApplicationStatusTransitionRequest request =
                new ApplicationStatusTransitionRequest(
                        ApplicationStatus.SUBMITTED,
                        null
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/workflow/transition",
                                application.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isForbidden());
    }

    private void transition(
            Long applicationId,
            ApplicationStatus targetStatus,
            String comment
    ) throws Exception {

        ApplicationStatusTransitionRequest request =
                new ApplicationStatusTransitionRequest(
                        targetStatus,
                        comment
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/workflow/transition",
                                applicationId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk());
    }

    private LoanApplication saveApplication(
            ApplicationStatus status
    ) {

        LoanApplication application =
                new LoanApplication();

        application.setBusiness(business);

        application.setApplicationNumber(
                "APP-WORKFLOW-" + System.nanoTime()
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

        application.setStatus(status);

        if (status != ApplicationStatus.DRAFT) {
            application.setSubmittedAt(
                    java.time.LocalDateTime.now()
            );
        }

        return loanApplicationRepository.save(
                application
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
}
