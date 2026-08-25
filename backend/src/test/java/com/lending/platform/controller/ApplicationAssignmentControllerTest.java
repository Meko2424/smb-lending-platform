package com.lending.platform.controller;

import com.lending.platform.dto.request.ApplicationAssignmentRequest;
import com.lending.platform.dto.request.BusinessRequest;
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
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private ApplicationStatusHistoryRepository historyRepository;

    @Autowired
    private BusinessOwnerRepository businessOwnerRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BusinessMapper businessMapper;

    private Business business;
    private LoanApplication application;

    private User loanOfficer;
    private User underwriter;

    @BeforeEach
    void setUp() {

        // Delete child data first because of foreign-key relationships.
        historyRepository.deleteAll();
        loanApplicationRepository.deleteAll();
        businessOwnerRepository.deleteAll();
        businessRepository.deleteAll();

        loanOfficer = getOrCreateUser(
                "loan.officer@lending.local",
                "Loan",
                "Officer",
                "LOAN_OFFICER"
        );

        underwriter = getOrCreateUser(
                "underwriter@lending.local",
                "Uma",
                "Underwriter",
                "UNDERWRITER"
        );

        business = businessRepository.save(
                businessMapper.toEntity(
                        createBusinessRequest()
                )
        );

        application = saveApplication();
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void assignLoanOfficer_shouldReturn200ForAdmin()
            throws Exception {

        ApplicationAssignmentRequest request =
                new ApplicationAssignmentRequest(
                        loanOfficer.getId()
                );

        mockMvc.perform(
                        put(
                                "/api/v1/applications/{applicationId}/assignments/loan-officer",
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
                .andExpect(jsonPath("$.assignedLoanOfficerId")
                        .value(loanOfficer.getId()))
                .andExpect(jsonPath("$.assignedLoanOfficerName")
                        .value("Loan Officer"));
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void assignLoanOfficer_shouldReturn404WhenUserDoesNotExist()
            throws Exception {

        ApplicationAssignmentRequest request =
                new ApplicationAssignmentRequest(
                        999999L
                );

        mockMvc.perform(
                        put(
                                "/api/v1/applications/{applicationId}/assignments/loan-officer",
                                application.getId()
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
                                "User not found with id: 999999"
                        ));
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void assignLoanOfficer_shouldReturn400WhenUserIdIsNull()
            throws Exception {

        ApplicationAssignmentRequest request =
                new ApplicationAssignmentRequest(
                        null
                );

        mockMvc.perform(
                        put(
                                "/api/v1/applications/{applicationId}/assignments/loan-officer",
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
    @WithMockUser(
            username = "loan.officer@lending.local",
            roles = "LOAN_OFFICER"
    )
    void assignLoanOfficer_shouldReturn403ForNonAdmin()
            throws Exception {

        ApplicationAssignmentRequest request =
                new ApplicationAssignmentRequest(
                        loanOfficer.getId()
                );

        mockMvc.perform(
                        put(
                                "/api/v1/applications/{applicationId}/assignments/loan-officer",
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

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void unassignLoanOfficer_shouldReturn200ForAdmin()
            throws Exception {

        application.setAssignedLoanOfficer(
                loanOfficer
        );

        loanApplicationRepository.save(
                application
        );

        mockMvc.perform(
                        delete(
                                "/api/v1/applications/{applicationId}/assignments/loan-officer",
                                application.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedLoanOfficerId")
                        .doesNotExist())
                .andExpect(jsonPath("$.assignedLoanOfficerName")
                        .doesNotExist());
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void assignUnderwriter_shouldReturn200ForAdmin()
            throws Exception {

        ApplicationAssignmentRequest request =
                new ApplicationAssignmentRequest(
                        underwriter.getId()
                );

        mockMvc.perform(
                        put(
                                "/api/v1/applications/{applicationId}/assignments/underwriter",
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
                .andExpect(jsonPath("$.assignedUnderwriterId")
                        .value(underwriter.getId()))
                .andExpect(jsonPath("$.assignedUnderwriterName")
                        .value("Uma Underwriter"));
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void unassignUnderwriter_shouldReturn200ForAdmin()
            throws Exception {

        application.setAssignedUnderwriter(
                underwriter
        );

        loanApplicationRepository.save(
                application
        );

        mockMvc.perform(
                        delete(
                                "/api/v1/applications/{applicationId}/assignments/underwriter",
                                application.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUnderwriterId")
                        .doesNotExist())
                .andExpect(jsonPath("$.assignedUnderwriterName")
                        .doesNotExist());
    }

    @Test
    void assignLoanOfficer_shouldReturn401WithoutAuthentication()
            throws Exception {

        ApplicationAssignmentRequest request =
                new ApplicationAssignmentRequest(
                        loanOfficer.getId()
                );

        mockMvc.perform(
                        put(
                                "/api/v1/applications/{applicationId}/assignments/loan-officer",
                                application.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401))
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"));
    }

    private User getOrCreateUser(
            String email,
            String firstName,
            String lastName,
            String roleName
    ) {

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseGet(() -> {

                    Role role =
                            roleRepository.findByName(roleName)
                                    .orElseThrow();

                    User user = new User();

                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setEmail(email);

                    // A valid BCrypt hash is not required for these
                    // @WithMockUser controller tests, but the DB column
                    // itself is NOT NULL.
                    user.setPasswordHash(
                            "test-password-hash"
                    );

                    user.setEnabled(true);
                    user.setRoles(Set.of(role));

                    return userRepository.save(user);
                });
    }

    private LoanApplication saveApplication() {

        LoanApplication loanApplication =
                new LoanApplication();

        loanApplication.setBusiness(
                business
        );

        loanApplication.setApplicationNumber(
                "APP-ASSIGN-" + System.nanoTime()
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
                ApplicationStatus.SUBMITTED
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
}
