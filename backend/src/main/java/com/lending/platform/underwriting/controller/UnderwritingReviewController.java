package com.lending.platform.underwriting.controller;

import com.lending.platform.underwriting.dto.request.UnderwritingReviewRequest;
import com.lending.platform.underwriting.dto.response.UnderwritingReviewResponse;
import com.lending.platform.underwriting.service.UnderwritingReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/underwriting-review")
public class UnderwritingReviewController {

    private final UnderwritingReviewService underwritingReviewService;

    public UnderwritingReviewController(
            UnderwritingReviewService underwritingReviewService
    ) {
        this.underwritingReviewService = underwritingReviewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
    public UnderwritingReviewResponse createReview(
            @PathVariable Long applicationId
    ) {
        return underwritingReviewService.createReview(applicationId);
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', " +
                    "'UNDERWRITER', 'CREDIT_MANAGER')"
    )
    public UnderwritingReviewResponse getReview(
            @PathVariable Long applicationId
    ) {
        return underwritingReviewService.getReview(applicationId);
    }

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
    public UnderwritingReviewResponse startReview(
            @PathVariable Long applicationId,
            Authentication authentication
    ) {
        return underwritingReviewService.startReview(
                applicationId,
                authentication.getName()
        );
    }

    @PutMapping("/analysis")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
    public UnderwritingReviewResponse updateAnalysis(
            @PathVariable Long applicationId,
            @Valid @RequestBody UnderwritingReviewRequest request
    ) {
        return underwritingReviewService.updateAnalysis(
                applicationId,
                request
        );
    }

    @PostMapping("/more-information")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
    public UnderwritingReviewResponse requestMoreInformation(
            @PathVariable Long applicationId,
            @Valid @RequestBody UnderwritingReviewRequest request,
            Authentication authentication
    ) {
        return underwritingReviewService.requestMoreInformation(
                applicationId,
                request,
                authentication.getName()
        );
    }

    @PostMapping("/resume")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
    public UnderwritingReviewResponse resumeReview(
            @PathVariable Long applicationId,
            Authentication authentication
    ) {
        return underwritingReviewService.resumeReview(
                applicationId,
                authentication.getName()
        );
    }

    @PostMapping("/recommend-approval")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
    public UnderwritingReviewResponse recommendApproval(
            @PathVariable Long applicationId,
            @Valid @RequestBody UnderwritingReviewRequest request,
            Authentication authentication
    ) {
        return underwritingReviewService.recommendApproval(
                applicationId,
                request,
                authentication.getName()
        );
    }

    @PostMapping("/recommend-decline")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
    public UnderwritingReviewResponse recommendDecline(
            @PathVariable Long applicationId,
            @Valid @RequestBody UnderwritingReviewRequest request,
            Authentication authentication
    ) {
        return underwritingReviewService.recommendDecline(
                applicationId,
                request,
                authentication.getName()
        );
    }
}
