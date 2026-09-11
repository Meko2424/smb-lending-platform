package com.lending.platform.credit.controller;

import com.lending.platform.credit.dto.request.CreditApprovalRequest;
import com.lending.platform.credit.dto.request.CreditDeclineRequest;
import com.lending.platform.credit.dto.response.CreditDecisionResponse;
import com.lending.platform.credit.service.CreditDecisionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/credit-decision")
public class CreditDecisionController {

    private final CreditDecisionService creditDecisionService;

    public CreditDecisionController(
            CreditDecisionService creditDecisionService
    ) {
        this.creditDecisionService = creditDecisionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CREDIT_MANAGER')")
    public CreditDecisionResponse createDecision(
            @PathVariable Long applicationId
    ) {
        return creditDecisionService.createDecision(applicationId);
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', 'UNDERWRITER', 'CREDIT_MANAGER')"
    )
    public CreditDecisionResponse getDecision(
            @PathVariable Long applicationId
    ) {
        return creditDecisionService.getDecision(applicationId);
    }

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREDIT_MANAGER')")
    public CreditDecisionResponse startReview(
            @PathVariable Long applicationId,
            Authentication authentication
    ) {
        return creditDecisionService.startReview(
                applicationId,
                authentication.getName()
        );
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREDIT_MANAGER')")
    public CreditDecisionResponse approve(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreditApprovalRequest request,
            Authentication authentication
    ) {
        return creditDecisionService.approve(
                applicationId,
                request,
                authentication.getName()
        );
    }

    @PostMapping("/decline")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREDIT_MANAGER')")
    public CreditDecisionResponse decline(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreditDeclineRequest request,
            Authentication authentication
    ) {
        return creditDecisionService.decline(
                applicationId,
                request,
                authentication.getName()
        );
    }
}
