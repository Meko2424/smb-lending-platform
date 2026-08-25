package com.lending.platform.controller;

import com.lending.platform.dto.request.ApplicationStatusTransitionRequest;
import com.lending.platform.dto.response.ApplicationStatusHistoryResponse;
import com.lending.platform.dto.response.LoanApplicationResponse;
import com.lending.platform.service.ApplicationWorkflowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/workflow")
public class ApplicationWorkflowController {

    private final ApplicationWorkflowService applicationWorkflowService;

    public ApplicationWorkflowController(
            ApplicationWorkflowService applicationWorkflowService
    ) {
        this.applicationWorkflowService = applicationWorkflowService;
    }

    @PostMapping("/transition")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', 'UNDERWRITER', 'CREDIT_MANAGER', 'CLOSING_SPECIALIST')")
    public ResponseEntity<LoanApplicationResponse> transitionStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationStatusTransitionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                applicationWorkflowService.transitionStatus(
                        applicationId,
                        request,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', 'UNDERWRITER', 'CREDIT_MANAGER', 'CLOSING_SPECIALIST')")
    public ResponseEntity<List<ApplicationStatusHistoryResponse>> getHistory(
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(
                applicationWorkflowService.getStatusHistory(
                        applicationId
                )
        );
    }
}
