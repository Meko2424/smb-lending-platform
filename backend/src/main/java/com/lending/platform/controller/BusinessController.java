package com.lending.platform.controller;

import com.lending.platform.dto.request.BusinessRequest;
import com.lending.platform.dto.response.BusinessResponse;
import com.lending.platform.service.BusinessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/businesses")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR')")
    public ResponseEntity<BusinessResponse> createBusiness(
            @Valid @RequestBody BusinessRequest request
    ) {
        BusinessResponse response =
                businessService.createBusiness(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', 'UNDERWRITER', 'CREDIT_MANAGER', 'CLOSING_SPECIALIST')")
    public ResponseEntity<BusinessResponse> getBusinessById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                businessService.getBusinessById(id)
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', 'UNDERWRITER', 'CREDIT_MANAGER', 'CLOSING_SPECIALIST')")
    public ResponseEntity<List<BusinessResponse>> getAllBusinesses() {
        return ResponseEntity.ok(
                businessService.getAllBusinesses()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR')")
    public ResponseEntity<BusinessResponse> updateBusiness(
            @PathVariable Long id,
            @Valid @RequestBody BusinessRequest request
    ) {
        return ResponseEntity.ok(
                businessService.updateBusiness(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBusiness(
            @PathVariable Long id
    ) {
        businessService.deleteBusiness(id);
        return ResponseEntity.noContent().build();
    }
}
