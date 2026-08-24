package com.lending.platform.controller;

import com.lending.platform.dto.request.BusinessOwnerRequest;
import com.lending.platform.dto.response.BusinessOwnerResponse;
import com.lending.platform.service.BusinessOwnerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/businesses/{businessId}/owners")
public class BusinessOwnerController {

    private final BusinessOwnerService businessOwnerService;

    public BusinessOwnerController(
            BusinessOwnerService businessOwnerService
    ) {
        this.businessOwnerService = businessOwnerService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR')")
    public ResponseEntity<BusinessOwnerResponse> createOwner(
            @PathVariable Long businessId,
            @Valid @RequestBody BusinessOwnerRequest request
    ) {
        BusinessOwnerResponse response =
                businessOwnerService.createOwner(
                        businessId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', 'UNDERWRITER', 'CREDIT_MANAGER', 'CLOSING_SPECIALIST')")
    public ResponseEntity<List<BusinessOwnerResponse>> getOwners(
            @PathVariable Long businessId
    ) {
        return ResponseEntity.ok(
                businessOwnerService.getOwnersByBusiness(
                        businessId
                )
        );
    }

    @GetMapping("/{ownerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR', 'UNDERWRITER', 'CREDIT_MANAGER', 'CLOSING_SPECIALIST')")
    public ResponseEntity<BusinessOwnerResponse> getOwnerById(
            @PathVariable Long businessId,
            @PathVariable Long ownerId
    ) {
        return ResponseEntity.ok(
                businessOwnerService.getOwnerById(
                        businessId,
                        ownerId
                )
        );
    }

    @PutMapping("/{ownerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LOAN_OFFICER', 'PROCESSOR')")
    public ResponseEntity<BusinessOwnerResponse> updateOwner(
            @PathVariable Long businessId,
            @PathVariable Long ownerId,
            @Valid @RequestBody BusinessOwnerRequest request
    ) {
        return ResponseEntity.ok(
                businessOwnerService.updateOwner(
                        businessId,
                        ownerId,
                        request
                )
        );
    }

    @DeleteMapping("/{ownerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOwner(
            @PathVariable Long businessId,
            @PathVariable Long ownerId
    ) {
        businessOwnerService.deleteOwner(
                businessId,
                ownerId
        );

        return ResponseEntity.noContent().build();
    }
}
