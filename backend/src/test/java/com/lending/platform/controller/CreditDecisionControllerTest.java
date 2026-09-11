package com.lending.platform.controller;


import com.lending.platform.AbstractIntegrationTest;
import com.lending.platform.credit.dto.request.CreditApprovalRequest;
import com.lending.platform.credit.dto.request.CreditDeclineRequest;
import com.lending.platform.credit.dto.response.CreditDecisionResponse;
import com.lending.platform.credit.entity.CreditDecisionStatus;
import com.lending.platform.credit.entity.CreditDecisionType;
import com.lending.platform.credit.service.CreditDecisionService;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CreditDecisionControllerTest
        extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreditDecisionService creditDecisionService;

    private static final Long APPLICATION_ID = 279L;

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void createDecision_shouldReturn201ForAdmin()
            throws Exception {

        when(creditDecisionService.createDecision(APPLICATION_ID))
                .thenReturn(pendingResponse());

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/credit-decision",
                                APPLICATION_ID
                        )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.applicationId")
                                .value(APPLICATION_ID)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("PENDING")
                )
                .andExpect(
                        jsonPath("$.requestedAmount")
                                .value(350000.00)
                )
                .andExpect(
                        jsonPath("$.requestedTermMonths")
                                .value(120)
                );

        verify(creditDecisionService)
                .createDecision(APPLICATION_ID);
    }

    @Test
    @WithMockUser(
            username = "officer@lending.local",
            roles = "LOAN_OFFICER"
    )
    void createDecision_shouldReturn403ForLoanOfficer()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/credit-decision",
                                APPLICATION_ID
                        )
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(creditDecisionService);
    }

    @Test
    @WithMockUser(
            username = "processor@lending.local",
            roles = "PROCESSOR"
    )
    void getDecision_shouldAllowProcessor()
            throws Exception {

        when(creditDecisionService.getDecision(APPLICATION_ID))
                .thenReturn(pendingResponse());

        mockMvc.perform(
                        get(
                                "/api/v1/applications/{applicationId}/credit-decision",
                                APPLICATION_ID
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("PENDING")
                );

        verify(creditDecisionService)
                .getDecision(APPLICATION_ID);
    }

    @Test
    @WithMockUser(
            username = "underwriter@lending.local",
            roles = "UNDERWRITER"
    )
    void getDecision_shouldAllowUnderwriter()
            throws Exception {

        when(creditDecisionService.getDecision(APPLICATION_ID))
                .thenReturn(pendingResponse());

        mockMvc.perform(
                        get(
                                "/api/v1/applications/{applicationId}/credit-decision",
                                APPLICATION_ID
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.applicationNumber")
                                .value("APP-2026-E22ADB4E")
                );
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void getDecision_shouldReturn404WhenDecisionDoesNotExist()
            throws Exception {

        when(creditDecisionService.getDecision(APPLICATION_ID))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Credit decision not found for application id: "
                                        + APPLICATION_ID
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/applications/{applicationId}/credit-decision",
                                APPLICATION_ID
                        )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void startReview_shouldReturn200AndMoveToInReview()
            throws Exception {

        when(
                creditDecisionService.startReview(
                        APPLICATION_ID,
                        "admin@lending.local"
                )
        ).thenReturn(inReviewResponse());

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/credit-decision/start",
                                APPLICATION_ID
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("IN_REVIEW")
                )
                .andExpect(
                        jsonPath("$.creditManagerUserId")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.creditManagerUserName")
                                .value("System Administrator")
                );

        verify(creditDecisionService)
                .startReview(
                        APPLICATION_ID,
                        "admin@lending.local"
                );
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void startReview_shouldReturn409ForInvalidLifecycle()
            throws Exception {

        when(
                creditDecisionService.startReview(
                        APPLICATION_ID,
                        "admin@lending.local"
                )
        ).thenThrow(
                new ResourceConflictException(
                        "Only pending credit decisions can be started"
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/credit-decision/start",
                                APPLICATION_ID
                        )
                )
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void approve_shouldReturn200ForValidRequest()
            throws Exception {

        CreditApprovalRequest request =
                new CreditApprovalRequest(
                        new BigDecimal("350000.00"),
                        120,
                        new BigDecimal("8.2500"),
                        "Standard closing conditions",
                        "Approved as requested"
                );

        when(
                creditDecisionService.approve(
                        eq(APPLICATION_ID),
                        any(CreditApprovalRequest.class),
                        eq("admin@lending.local")
                )
        ).thenReturn(approvedResponse());

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/credit-decision/approve",
                                APPLICATION_ID
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("APPROVED")
                )
                .andExpect(
                        jsonPath("$.decisionType")
                                .value("APPROVE_AS_REQUESTED")
                )
                .andExpect(
                        jsonPath("$.approvedAmount")
                                .value(350000.00)
                )
                .andExpect(
                        jsonPath("$.approvedTermMonths")
                                .value(120)
                )
                .andExpect(
                        jsonPath("$.interestRate")
                                .value(8.2500)
                );
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void approve_shouldReturn400WhenApprovedAmountMissing()
            throws Exception {

        String request = """
                {
                  "approvedTermMonths": 120,
                  "interestRate": 8.2500,
                  "decisionNotes": "Approved"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/credit-decision/approve",
                                APPLICATION_ID
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(
                creditDecisionService,
                never()
        ).approve(
                anyLong(),
                any(),
                anyString()
        );
    }

    @Test
    @WithMockUser(
            username = "loanofficer@lending.local",
            roles = "LOAN_OFFICER"
    )
    void approve_shouldReturn403ForLoanOfficer()
            throws Exception {

        CreditApprovalRequest request =
                new CreditApprovalRequest(
                        new BigDecimal("350000.00"),
                        120,
                        new BigDecimal("8.2500"),
                        null,
                        null
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/credit-decision/approve",
                                APPLICATION_ID
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(creditDecisionService);
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void decline_shouldReturn200ForValidRequest()
            throws Exception {

        CreditDeclineRequest request =
                new CreditDeclineRequest(
                        "Insufficient repayment capacity",
                        "Risk exceeds acceptable credit tolerance"
                );

        when(
                creditDecisionService.decline(
                        eq(APPLICATION_ID),
                        any(CreditDeclineRequest.class),
                        eq("admin@lending.local")
                )
        ).thenReturn(declinedResponse());

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/credit-decision/decline",
                                APPLICATION_ID
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("DECLINED")
                )
                .andExpect(
                        jsonPath("$.decisionType")
                                .value("DECLINE")
                )
                .andExpect(
                        jsonPath("$.declineReason")
                                .value(
                                        "Insufficient repayment capacity"
                                )
                );
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void decline_shouldReturn400WhenReasonIsBlank()
            throws Exception {

        String request = """
                {
                  "declineReason": "   ",
                  "decisionNotes": "Declined"
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/v1/applications/{applicationId}/credit-decision/decline",
                                APPLICATION_ID
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verify(
                creditDecisionService,
                never()
        ).decline(
                anyLong(),
                any(),
                anyString()
        );
    }

    private CreditDecisionResponse pendingResponse() {
        LocalDateTime now = LocalDateTime.now();

        return new CreditDecisionResponse(
                1L,
                APPLICATION_ID,
                "APP-2026-E22ADB4E",
                CreditDecisionStatus.PENDING,
                null,
                null,
                null,
                new BigDecimal("350000.00"),
                null,
                120,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                now,
                now
        );
    }

    private CreditDecisionResponse inReviewResponse() {
        LocalDateTime now = LocalDateTime.now();

        return new CreditDecisionResponse(
                1L,
                APPLICATION_ID,
                "APP-2026-E22ADB4E",
                CreditDecisionStatus.IN_REVIEW,
                null,
                1L,
                "System Administrator",
                new BigDecimal("350000.00"),
                null,
                120,
                null,
                null,
                null,
                null,
                null,
                now,
                null,
                now,
                now
        );
    }

    private CreditDecisionResponse approvedResponse() {
        LocalDateTime now = LocalDateTime.now();

        return new CreditDecisionResponse(
                1L,
                APPLICATION_ID,
                "APP-2026-E22ADB4E",
                CreditDecisionStatus.APPROVED,
                CreditDecisionType.APPROVE_AS_REQUESTED,
                1L,
                "System Administrator",
                new BigDecimal("350000.00"),
                new BigDecimal("350000.00"),
                120,
                120,
                new BigDecimal("8.2500"),
                "Standard closing conditions",
                null,
                "Approved as requested",
                now,
                now,
                now,
                now
        );
    }

    private CreditDecisionResponse declinedResponse() {
        LocalDateTime now = LocalDateTime.now();

        return new CreditDecisionResponse(
                1L,
                APPLICATION_ID,
                "APP-2026-E22ADB4E",
                CreditDecisionStatus.DECLINED,
                CreditDecisionType.DECLINE,
                1L,
                "System Administrator",
                new BigDecimal("350000.00"),
                null,
                120,
                null,
                null,
                null,
                "Insufficient repayment capacity",
                "Risk exceeds acceptable credit tolerance",
                now,
                now,
                now,
                now
        );
    }
}