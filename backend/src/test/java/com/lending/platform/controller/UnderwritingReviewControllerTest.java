package com.lending.platform.controller;


import com.lending.platform.AbstractIntegrationTest;
import com.lending.platform.underwriting.dto.request.UnderwritingReviewRequest;
import com.lending.platform.underwriting.dto.response.UnderwritingReviewResponse;
import com.lending.platform.underwriting.entity.RiskRating;
import com.lending.platform.underwriting.entity.UnderwritingReviewStatus;
import com.lending.platform.underwriting.service.UnderwritingReviewService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UnderwritingReviewControllerTest
        extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UnderwritingReviewService underwritingReviewService;

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void createReview_shouldReturn201() throws Exception {

        when(underwritingReviewService.createReview(10L))
                .thenReturn(
                        response(
                                UnderwritingReviewStatus.PENDING
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/10/underwriting-review"
                        )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.applicationId").value(10))
                .andExpect(
                        jsonPath("$.applicationNumber")
                                .value("APP-2026-UW001")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("PENDING")
                );
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void getReview_shouldReturn200() throws Exception {

        when(underwritingReviewService.getReview(10L))
                .thenReturn(
                        response(
                                UnderwritingReviewStatus.IN_REVIEW
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/applications/10/underwriting-review"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(10))
                .andExpect(
                        jsonPath("$.status")
                                .value("IN_REVIEW")
                );
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void startReview_shouldReturn200() throws Exception {

        when(underwritingReviewService.startReview(
                10L,
                "admin@lending.local"
        )).thenReturn(
                response(
                        UnderwritingReviewStatus.IN_REVIEW
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/10/" +
                                        "underwriting-review/start"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("IN_REVIEW")
                );

        verify(underwritingReviewService)
                .startReview(
                        10L,
                        "admin@lending.local"
                );
    }

    @Test
    @WithMockUser(
            username = "underwriter@lending.local",
            roles = "UNDERWRITER"
    )
    void updateAnalysis_shouldReturn200() throws Exception {

        UnderwritingReviewRequest request =
                createRequest();

        when(underwritingReviewService.updateAnalysis(
                eq(10L),
                any(UnderwritingReviewRequest.class)
        )).thenReturn(
                response(
                        UnderwritingReviewStatus.IN_REVIEW
                )
        );

        mockMvc.perform(
                        put(
                                "/api/v1/applications/10/" +
                                        "underwriting-review/analysis"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("IN_REVIEW")
                )
                .andExpect(
                        jsonPath("$.debtServiceCoverageRatio")
                                .value(1.25)
                );
    }

    @Test
    @WithMockUser(
            username = "underwriter@lending.local",
            roles = "UNDERWRITER"
    )
    void requestMoreInformation_shouldReturn200()
            throws Exception {

        UnderwritingReviewRequest request =
                createRequest();

        when(
                underwritingReviewService.requestMoreInformation(
                        eq(10L),
                        any(UnderwritingReviewRequest.class),
                        eq("underwriter@lending.local")
                )
        ).thenReturn(
                response(
                        UnderwritingReviewStatus
                                .NEEDS_MORE_INFORMATION
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/10/" +
                                        "underwriting-review/more-information"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("NEEDS_MORE_INFORMATION")
                );

        verify(underwritingReviewService)
                .requestMoreInformation(
                        eq(10L),
                        any(UnderwritingReviewRequest.class),
                        eq("underwriter@lending.local")
                );
    }

    @Test
    @WithMockUser(
            username = "underwriter@lending.local",
            roles = "UNDERWRITER"
    )
    void resumeReview_shouldReturn200()
            throws Exception {

        when(underwritingReviewService.resumeReview(
                10L,
                "underwriter@lending.local"
        )).thenReturn(
                response(
                        UnderwritingReviewStatus.IN_REVIEW
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/10/" +
                                        "underwriting-review/resume"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("IN_REVIEW")
                );

        verify(underwritingReviewService)
                .resumeReview(
                        10L,
                        "underwriter@lending.local"
                );
    }

    @Test
    @WithMockUser(
            username = "underwriter@lending.local",
            roles = "UNDERWRITER"
    )
    void recommendApproval_shouldReturn200()
            throws Exception {

        UnderwritingReviewRequest request =
                createRequest();

        when(underwritingReviewService.recommendApproval(
                eq(10L),
                any(UnderwritingReviewRequest.class),
                eq("underwriter@lending.local")
        )).thenReturn(
                response(
                        UnderwritingReviewStatus
                                .RECOMMEND_APPROVAL
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/10/" +
                                        "underwriting-review/" +
                                        "recommend-approval"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("RECOMMEND_APPROVAL")
                );
    }

    @Test
    @WithMockUser(
            username = "underwriter@lending.local",
            roles = "UNDERWRITER"
    )
    void recommendDecline_shouldReturn200()
            throws Exception {

        UnderwritingReviewRequest request =
                createRequest();

        when(underwritingReviewService.recommendDecline(
                eq(10L),
                any(UnderwritingReviewRequest.class),
                eq("underwriter@lending.local")
        )).thenReturn(
                response(
                        UnderwritingReviewStatus
                                .RECOMMEND_DECLINE
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/applications/10/" +
                                        "underwriting-review/" +
                                        "recommend-decline"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("RECOMMEND_DECLINE")
                );
    }

    @Test
    @WithMockUser(
            username = "underwriter@lending.local",
            roles = "UNDERWRITER"
    )
    void updateAnalysis_shouldReturn400ForNegativeDebt()
            throws Exception {

        UnderwritingReviewRequest request =
                new UnderwritingReviewRequest(
                        new BigDecimal("-1000.00"),
                        new BigDecimal("10000.00"),
                        new BigDecimal("12500.00"),
                        RiskRating.MODERATE,
                        "Experienced management team",
                        "Customer concentration",
                        "Industry volatility",
                        "Strong collateral",
                        "Financial analysis",
                        "Recommendation notes"
                );

        mockMvc.perform(
                        put(
                                "/api/v1/applications/10/" +
                                        "underwriting-review/analysis"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status").value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                );
    }

    @Test
    @WithMockUser(
            username = "loan.officer@lending.local",
            roles = "LOAN_OFFICER"
    )
    void createReview_shouldReturn403ForUnauthorizedRole()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/applications/10/" +
                                        "underwriting-review"
                        )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    @WithMockUser(
            username = "processor@lending.local",
            roles = "PROCESSOR"
    )
    void updateAnalysis_shouldReturn403ForProcessor()
            throws Exception {

        mockMvc.perform(
                        put(
                                "/api/v1/applications/10/" +
                                        "underwriting-review/analysis"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest()
                                        )
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void getReview_shouldReturn401WithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/applications/10/" +
                                        "underwriting-review"
                        )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        jsonPath("$.status").value(401)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Unauthorized")
                );
    }

    private UnderwritingReviewRequest createRequest() {

        return new UnderwritingReviewRequest(
                new BigDecimal("100000.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("12500.00"),
                RiskRating.MODERATE,
                "Experienced management team",
                "Customer concentration",
                "Industry volatility",
                "Strong collateral and liquidity",
                "Cash flow supports repayment capacity",
                "Recommendable credit profile"
        );
    }

    private UnderwritingReviewResponse response(
            UnderwritingReviewStatus status
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        boolean started =
                status != UnderwritingReviewStatus.PENDING;

        boolean completed =
                status
                        == UnderwritingReviewStatus
                        .RECOMMEND_APPROVAL
                        ||
                        status
                                == UnderwritingReviewStatus
                                .RECOMMEND_DECLINE;

        return new UnderwritingReviewResponse(
                20L,
                10L,
                "APP-2026-UW001",

                status,

                started ? 1L : null,
                started
                        ? "System Administrator"
                        : null,

                new BigDecimal("350000.00"),
                new BigDecimal("2400000.00"),
                new BigDecimal("100000.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("12500.00"),
                new BigDecimal("1.2500"),

                RiskRating.MODERATE,

                "Experienced management team",
                "Customer concentration",
                "Industry volatility",
                "Strong collateral and liquidity",
                "Cash flow supports repayment capacity",
                "Recommendable credit profile",

                started ? now : null,
                completed ? now : null,

                now,
                now
        );
    }
}