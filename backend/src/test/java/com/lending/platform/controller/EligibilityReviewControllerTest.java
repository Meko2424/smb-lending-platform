package com.lending.platform.controller;

import tools.jackson.databind.ObjectMapper;
import com.lending.platform.AbstractIntegrationTest;
import com.lending.platform.dto.eligibility.EligibilityCriterionRequest;
import com.lending.platform.dto.eligibility.EligibilityReviewRequest;
import com.lending.platform.dto.eligibility.EligibilityReviewResponse;
import com.lending.platform.entity.EligibilityCriterionStatus;
import com.lending.platform.entity.EligibilityCriterionType;
import com.lending.platform.entity.EligibilityReviewStatus;
import com.lending.platform.service.EligibilityReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EligibilityReviewControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EligibilityReviewService eligibilityReviewService;

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void createReview_shouldReturn201() throws Exception {

        when(eligibilityReviewService.createReview(
                eq(10L),
                any(EligibilityReviewRequest.class)
        )).thenReturn(response(
                EligibilityReviewStatus.PENDING
        ));

        EligibilityReviewRequest request =
                new EligibilityReviewRequest(
                        "Initial eligibility screening"
                );

        mockMvc.perform(
                        post("/api/v1/applications/10/eligibility-review")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationId").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(
                        jsonPath("$.summary")
                                .value("Initial eligibility screening")
                );
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void getReview_shouldReturn200() throws Exception {

        when(eligibilityReviewService.getReview(10L))
                .thenReturn(response(
                        EligibilityReviewStatus.IN_REVIEW
                ));

        mockMvc.perform(
                        get("/api/v1/applications/10/eligibility-review")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicationId").value(10))
                .andExpect(jsonPath("$.status").value("IN_REVIEW"));
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void startReview_shouldReturn200() throws Exception {

        when(eligibilityReviewService.startReview(
                10L,
                "admin@lending.local"
        )).thenReturn(response(
                EligibilityReviewStatus.IN_REVIEW
        ));

        mockMvc.perform(
                        post(
                                "/api/v1/applications/10/" +
                                        "eligibility-review/start"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_REVIEW"));

        verify(eligibilityReviewService)
                .startReview(
                        10L,
                        "admin@lending.local"
                );
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void updateCriterion_shouldReturn200() throws Exception {

        EligibilityCriterionRequest request =
                new EligibilityCriterionRequest(
                        EligibilityCriterionStatus.PASSED,
                        "Business satisfies operating history requirement"
                );

        when(eligibilityReviewService.updateCriterion(
                eq(10L),
                eq(
                        EligibilityCriterionType
                                .BUSINESS_OPERATING_HISTORY
                ),
                any(EligibilityCriterionRequest.class)
        )).thenReturn(response(
                EligibilityReviewStatus.IN_REVIEW
        ));

        mockMvc.perform(
                        put(
                                "/api/v1/applications/10/" +
                                        "eligibility-review/criteria/" +
                                        "BUSINESS_OPERATING_HISTORY"
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_REVIEW"));
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void updateCriterion_shouldReturn400WhenStatusMissing()
            throws Exception {

        String request = """
                {
                  "notes": "Missing criterion status"
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/v1/applications/10/" +
                                        "eligibility-review/criteria/" +
                                        "REVENUE_REQUIREMENT"
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void completeAsEligible_shouldReturn200()
            throws Exception {

        when(eligibilityReviewService.completeAsEligible(
                10L,
                "admin@lending.local"
        )).thenReturn(response(
                EligibilityReviewStatus.ELIGIBLE
        ));

        mockMvc.perform(
                        post(
                                "/api/v1/applications/10/" +
                                        "eligibility-review/eligible"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ELIGIBLE"));

        verify(eligibilityReviewService)
                .completeAsEligible(
                        10L,
                        "admin@lending.local"
                );
    }

    @Test
    @WithMockUser(
            username = "admin@lending.local",
            roles = "ADMIN"
    )
    void completeAsIneligible_shouldReturn200()
            throws Exception {

        EligibilityReviewRequest request =
                new EligibilityReviewRequest(
                        "Revenue requirement was not satisfied"
                );

        when(eligibilityReviewService.completeAsIneligible(
                eq(10L),
                any(EligibilityReviewRequest.class),
                eq("admin@lending.local")
        )).thenReturn(response(
                EligibilityReviewStatus.INELIGIBLE
        ));

        mockMvc.perform(
                        post(
                                "/api/v1/applications/10/" +
                                        "eligibility-review/ineligible"
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("INELIGIBLE")
                );
    }

    @Test
    @WithMockUser(
            username = "viewer@lending.local",
            roles = "VIEWER"
    )
    void createReview_shouldReturn403ForUnauthorizedRole()
            throws Exception {

        EligibilityReviewRequest request =
                new EligibilityReviewRequest(null);

        mockMvc.perform(
                        post("/api/v1/applications/10/eligibility-review")
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
            username = "loanofficer@lending.local",
            roles = "LOAN_OFFICER"
    )
    void startReview_shouldReturn403ForLoanOfficer()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/applications/10/" +
                                        "eligibility-review/start"
                        )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getReview_shouldReturn401WithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/applications/10/eligibility-review")
                )
                .andExpect(status().isUnauthorized());
    }

    private EligibilityReviewResponse response(
            EligibilityReviewStatus status
    ) {

        LocalDateTime now = LocalDateTime.now();

        return new EligibilityReviewResponse(
                20L,
                10L,
                status,
                1L,
                "System Administrator",
                status == EligibilityReviewStatus.PENDING
                        ? null
                        : now,
                status == EligibilityReviewStatus.ELIGIBLE
                        || status
                        == EligibilityReviewStatus.INELIGIBLE
                        ? now
                        : null,
                status == EligibilityReviewStatus.INELIGIBLE
                        ? "Revenue requirement was not satisfied"
                        : "Initial eligibility screening",
                List.of(),
                now,
                now
        );
    }
}
