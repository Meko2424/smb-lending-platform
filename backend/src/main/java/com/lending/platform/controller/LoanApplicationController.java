package com.lending.platform.controller;

import com.lending.platform.dto.request.LoanApplicationRequest;
import com.lending.platform.dto.response.LoanApplicationResponse;
import com.lending.platform.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(
            LoanApplicationService loanApplicationService
    ) {
        this.loanApplicationService = loanApplicationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR')")
    public ResponseEntity<LoanApplicationResponse> createApplication(
            @Valid @RequestBody LoanApplicationRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        loanApplicationService.createApplication(request)
                );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', 'UNDERWRITER', 'CREDIT_MANAGER', 'CLOSING_SPECIALIST')")
    public ResponseEntity<List<LoanApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(
                loanApplicationService.getAllApplications()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', 'UNDERWRITER', 'CREDIT_MANAGER', 'CLOSING_SPECIALIST')")
    public ResponseEntity<LoanApplicationResponse> getApplicationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                loanApplicationService.getApplicationById(id)
        );
    }

    @GetMapping("/business/{businessId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', 'UNDERWRITER', 'CREDIT_MANAGER', 'CLOSING_SPECIALIST')")
    public ResponseEntity<List<LoanApplicationResponse>> getApplicationsByBusiness(
            @PathVariable Long businessId
    ) {
        return ResponseEntity.ok(
                loanApplicationService.getApplicationsByBusiness(
                        businessId
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR')")
    public ResponseEntity<LoanApplicationResponse> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody LoanApplicationRequest request
    ) {
        return ResponseEntity.ok(
                loanApplicationService.updateApplication(
                        id,
                        request
                )
        );
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER')")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable Long id
    ) {
        loanApplicationService.deleteApplication(id);

        return ResponseEntity.noContent().build();
    }
}
