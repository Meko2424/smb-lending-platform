package com.lending.platform.controller;

import com.lending.platform.dto.request.ApplicationAssignmentRequest;
import com.lending.platform.dto.response.LoanApplicationResponse;
import com.lending.platform.service.ApplicationAssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/assignments")
public class ApplicationAssignmentController {

    private final ApplicationAssignmentService applicationAssignmentService;

    public ApplicationAssignmentController(
            ApplicationAssignmentService applicationAssignmentService
    ) {
        this.applicationAssignmentService = applicationAssignmentService;
    }

    @PutMapping("/loan-officer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanApplicationResponse> assignLoanOfficer(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationAssignmentRequest request
    ) {
        return ResponseEntity.ok(
                applicationAssignmentService.assignLoanOfficer(
                        applicationId,
                        request
                )
        );
    }

    @DeleteMapping("/loan-officer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanApplicationResponse> unassignLoanOfficer(
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(
                applicationAssignmentService.unassignLoanOfficer(
                        applicationId
                )
        );
    }

    @PutMapping("/underwriter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanApplicationResponse> assignUnderwriter(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationAssignmentRequest request
    ) {
        return ResponseEntity.ok(
                applicationAssignmentService.assignUnderwriter(
                        applicationId,
                        request
                )
        );
    }

    @DeleteMapping("/underwriter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoanApplicationResponse> unassignUnderwriter(
            @PathVariable Long applicationId
    ) {
        return ResponseEntity.ok(
                applicationAssignmentService.unassignUnderwriter(
                        applicationId
                )
        );
    }
}
