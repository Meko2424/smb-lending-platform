package com.lending.platform.controller;

import com.lending.platform.dto.eligibility.EligibilityCriterionRequest;
import com.lending.platform.dto.eligibility.EligibilityReviewRequest;
import com.lending.platform.dto.eligibility.EligibilityReviewResponse;
import com.lending.platform.entity.EligibilityCriterionType;
import com.lending.platform.service.EligibilityReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/eligibility-review")
public class EligibilityReviewController {

    private final EligibilityReviewService eligibilityReviewService;

    public EligibilityReviewController(
            EligibilityReviewService eligibilityReviewService
    ) {
        this.eligibilityReviewService = eligibilityReviewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', 'UNDERWRITER')"
    )
    public EligibilityReviewResponse createReview(
            @PathVariable Long applicationId,
            @Valid @RequestBody EligibilityReviewRequest request
    ) {
        return eligibilityReviewService.createReview(
                applicationId,
                request
        );
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', " +
                    "'UNDERWRITER', 'CREDIT_MANAGER')"
    )
    public EligibilityReviewResponse getReview(
            @PathVariable Long applicationId
    ) {
        return eligibilityReviewService.getReview(applicationId);
    }

    @PostMapping("/start")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'PROCESSOR', 'UNDERWRITER')"
    )
    public EligibilityReviewResponse startReview(
            @PathVariable Long applicationId,
            Authentication authentication
    ) {
        return eligibilityReviewService.startReview(
                applicationId,
                authentication.getName()
        );
    }

    @PutMapping("/criteria/{criterionType}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'PROCESSOR', 'UNDERWRITER')"
    )
    public EligibilityReviewResponse updateCriterion(
            @PathVariable Long applicationId,
            @PathVariable EligibilityCriterionType criterionType,
            @Valid @RequestBody EligibilityCriterionRequest request
    ) {
        return eligibilityReviewService.updateCriterion(
                applicationId,
                criterionType,
                request
        );
    }

    @PostMapping("/eligible")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'PROCESSOR', 'UNDERWRITER')"
    )
    public EligibilityReviewResponse completeAsEligible(
            @PathVariable Long applicationId,
            Authentication authentication
    ) {
        return eligibilityReviewService.completeAsEligible(
                applicationId,
                authentication.getName()
        );
    }

    @PostMapping("/ineligible")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'PROCESSOR', 'UNDERWRITER')"
    )
    public EligibilityReviewResponse completeAsIneligible(
            @PathVariable Long applicationId,
            @Valid @RequestBody EligibilityReviewRequest request,
            Authentication authentication
    ) {
        return eligibilityReviewService.completeAsIneligible(
                applicationId,
                request,
                authentication.getName()
        );
    }
}
