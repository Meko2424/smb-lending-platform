package com.lending.platform.controller;

import com.lending.platform.AbstractIntegrationTest;
import com.lending.platform.dto.request.BusinessRequest;
import com.lending.platform.dto.request.LoanApplicationRequest;
import com.lending.platform.entity.*;
import com.lending.platform.mapper.BusinessMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoanApplicationControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private BusinessMapper businessMapper;

    private Business business;

    @BeforeEach
    void setUp() {
        loanApplicationRepository.deleteAll();
        businessRepository.deleteAll();

        business = businessRepository.save(
                businessMapper.toEntity(createBusinessRequest())
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createApplication_shouldReturn201ForAdmin() throws Exception {

        LoanApplicationRequest request = createLoanRequest();

        mockMvc.perform(
                        post("/api/v1/applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.businessId")
                        .value(business.getId()))
                .andExpect(jsonPath("$.businessName")
                        .value("Atlanta Logistics LLC"))
                .andExpect(jsonPath("$.loanProduct")
                        .value("SBA_7A"))
                .andExpect(jsonPath("$.loanPurpose")
                        .value("EQUIPMENT_PURCHASE"))
                .andExpect(jsonPath("$.requestedAmount")
                        .value(350000.00))
                .andExpect(jsonPath("$.requestedTermMonths")
                        .value(120))
                .andExpect(jsonPath("$.status")
                        .value("DRAFT"))
                .andExpect(jsonPath("$.applicationNumber")
                        .isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createApplication_shouldReturn400ForInvalidRequest()
            throws Exception {

        LoanApplicationRequest request =
                new LoanApplicationRequest(
                        null,
                        null,
                        null,
                        new BigDecimal("-1000"),
                        0
                );

        mockMvc.perform(
                        post("/api/v1/applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createApplication_shouldReturn404WhenBusinessDoesNotExist()
            throws Exception {

        LoanApplicationRequest request =
                new LoanApplicationRequest(
                        999999L,
                        LoanProduct.SBA_7A,
                        LoanPurpose.EQUIPMENT_PURCHASE,
                        new BigDecimal("350000.00"),
                        120
                );

        mockMvc.perform(
                        post("/api/v1/applications")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Business not found with id: 999999"));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void updateApplication_shouldReturn409ForSubmittedApplication()
            throws Exception {

        LoanApplication application =
                saveApplication(ApplicationStatus.SUBMITTED);

        LoanApplicationRequest request =
                new LoanApplicationRequest(
                        business.getId(),
                        LoanProduct.BUSINESS_TERM_LOAN,
                        LoanPurpose.WORKING_CAPITAL,
                        new BigDecimal("200000.00"),
                        60
                );

        mockMvc.perform(
                        put(
                                "/api/v1/applications/{id}",
                                application.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Only draft applications can be edited"
                        ));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteApplication_shouldReturn204ForDraft()
            throws Exception {

        LoanApplication application =
                saveApplication(ApplicationStatus.DRAFT);

        mockMvc.perform(
                        delete(
                                "/api/v1/applications/{id}",
                                application.getId()
                        )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void deleteApplication_shouldReturn403ForUnauthorizedRole()
            throws Exception {

        LoanApplication application =
                saveApplication(ApplicationStatus.DRAFT);

        mockMvc.perform(
                        delete(
                                "/api/v1/applications/{id}",
                                application.getId()
                        )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteApplication_shouldReturn409ForSubmittedApplication()
            throws Exception {

        LoanApplication application =
                saveApplication(ApplicationStatus.SUBMITTED);

        mockMvc.perform(
                        delete(
                                "/api/v1/applications/{id}",
                                application.getId()
                        )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(
                                "Only draft applications can be deleted"
                        ));
    }

    @Test
    void getAllApplications_shouldReturn401WithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/applications")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401))
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"));
    }

    private LoanApplication saveApplication(
            ApplicationStatus status
    ) {

        LoanApplication application =
                new LoanApplication();

        application.setBusiness(business);
        application.setApplicationNumber(
                "APP-TEST-" + System.nanoTime()
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

        return loanApplicationRepository.save(application);
    }

    private LoanApplicationRequest createLoanRequest() {
        return new LoanApplicationRequest(
                business.getId(),
                LoanProduct.SBA_7A,
                LoanPurpose.EQUIPMENT_PURCHASE,
                new BigDecimal("350000.00"),
                120
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
