package com.lending.platform.service.impl;

import com.lending.platform.dto.request.LoanApplicationRequest;
import com.lending.platform.dto.response.LoanApplicationResponse;
import com.lending.platform.entity.ApplicationStatus;
import com.lending.platform.entity.Business;
import com.lending.platform.entity.BusinessType;
import com.lending.platform.entity.LoanApplication;
import com.lending.platform.entity.LoanProduct;
import com.lending.platform.entity.LoanPurpose;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.mapper.LoanApplicationMapper;
import com.lending.platform.repository.BusinessRepository;
import com.lending.platform.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceImplTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private BusinessRepository businessRepository;

    private LoanApplicationMapper loanApplicationMapper;
    private LoanApplicationServiceImpl loanApplicationService;

    @BeforeEach
    void setUp() {
        loanApplicationMapper = new LoanApplicationMapper();

        loanApplicationService = new LoanApplicationServiceImpl(
                loanApplicationRepository,
                businessRepository,
                loanApplicationMapper
        );
    }

    @Test
    void createApplication_shouldCreateDraftApplication() {

        Business business = createBusiness(1L);

        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));

        when(loanApplicationRepository.save(any(LoanApplication.class)))
                .thenAnswer(invocation -> {
                    LoanApplication application =
                            invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            application,
                            "id",
                            10L
                    );

                    application.onCreate();

                    return application;
                });

        LoanApplicationResponse response =
                loanApplicationService.createApplication(
                        createRequest()
                );

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals(1L, response.businessId());
        assertEquals("Atlanta Logistics LLC", response.businessName());
        assertEquals(LoanProduct.SBA_7A, response.loanProduct());
        assertEquals(
                LoanPurpose.EQUIPMENT_PURCHASE,
                response.loanPurpose()
        );
        assertEquals(
                new BigDecimal("350000.00"),
                response.requestedAmount()
        );
        assertEquals(120, response.requestedTermMonths());
        assertEquals(
                ApplicationStatus.DRAFT,
                response.status()
        );

        assertNotNull(response.applicationNumber());
        assertTrue(
                response.applicationNumber()
                        .startsWith("APP-")
        );

        verify(loanApplicationRepository)
                .save(any(LoanApplication.class));
    }

    @Test
    void createApplication_shouldThrowWhenBusinessDoesNotExist() {

        when(businessRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanApplicationService.createApplication(
                        createRequest()
                )
        );

        verify(loanApplicationRepository, never())
                .save(any());
    }

    @Test
    void getApplicationById_shouldReturnApplication() {

        LoanApplication application =
                createApplication(
                        10L,
                        ApplicationStatus.DRAFT
                );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        LoanApplicationResponse response =
                loanApplicationService.getApplicationById(
                        10L
                );

        assertEquals(10L, response.id());
        assertEquals(
                "APP-2026-TEST0001",
                response.applicationNumber()
        );
        assertEquals(
                ApplicationStatus.DRAFT,
                response.status()
        );
    }

    @Test
    void getApplicationById_shouldThrowWhenMissing() {

        when(loanApplicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanApplicationService
                        .getApplicationById(999L)
        );
    }

    @Test
    void updateApplication_shouldUpdateDraft() {

        LoanApplication application =
                createApplication(
                        10L,
                        ApplicationStatus.DRAFT
                );

        Business business = createBusiness(1L);

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));

        when(loanApplicationRepository.save(application))
                .thenReturn(application);

        LoanApplicationRequest updatedRequest =
                new LoanApplicationRequest(
                        1L,
                        LoanProduct.BUSINESS_TERM_LOAN,
                        LoanPurpose.WORKING_CAPITAL,
                        new BigDecimal("200000.00"),
                        60
                );

        LoanApplicationResponse response =
                loanApplicationService.updateApplication(
                        10L,
                        updatedRequest
                );

        assertEquals(
                LoanProduct.BUSINESS_TERM_LOAN,
                response.loanProduct()
        );
        assertEquals(
                LoanPurpose.WORKING_CAPITAL,
                response.loanPurpose()
        );
        assertEquals(
                new BigDecimal("200000.00"),
                response.requestedAmount()
        );
        assertEquals(60, response.requestedTermMonths());

        verify(loanApplicationRepository)
                .save(application);
    }

    @Test
    void updateApplication_shouldRejectNonDraft() {

        LoanApplication application =
                createApplication(
                        10L,
                        ApplicationStatus.SUBMITTED
                );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        assertThrows(
                ResourceConflictException.class,
                () -> loanApplicationService.updateApplication(
                        10L,
                        createRequest()
                )
        );

        verify(businessRepository, never())
                .findById(anyLong());

        verify(loanApplicationRepository, never())
                .save(any());
    }

    @Test
    void submitApplication_shouldMoveDraftToSubmitted() {

        LoanApplication application =
                createApplication(
                        10L,
                        ApplicationStatus.DRAFT
                );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(loanApplicationRepository.save(application))
                .thenReturn(application);

        LoanApplicationResponse response =
                loanApplicationService.submitApplication(
                        10L
                );

        assertEquals(
                ApplicationStatus.SUBMITTED,
                response.status()
        );

        assertNotNull(response.submittedAt());

        verify(loanApplicationRepository)
                .save(application);
    }

    @Test
    void submitApplication_shouldRejectNonDraft() {

        LoanApplication application =
                createApplication(
                        10L,
                        ApplicationStatus.SUBMITTED
                );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        assertThrows(
                ResourceConflictException.class,
                () -> loanApplicationService
                        .submitApplication(10L)
        );

        verify(loanApplicationRepository, never())
                .save(any());
    }

    @Test
    void deleteApplication_shouldDeleteDraft() {

        LoanApplication application =
                createApplication(
                        10L,
                        ApplicationStatus.DRAFT
                );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        loanApplicationService.deleteApplication(10L);

        verify(loanApplicationRepository)
                .delete(application);
    }

    @Test
    void deleteApplication_shouldRejectNonDraft() {

        LoanApplication application =
                createApplication(
                        10L,
                        ApplicationStatus.SUBMITTED
                );

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        assertThrows(
                ResourceConflictException.class,
                () -> loanApplicationService
                        .deleteApplication(10L)
        );

        verify(loanApplicationRepository, never())
                .delete(any());
    }

    private LoanApplicationRequest createRequest() {
        return new LoanApplicationRequest(
                1L,
                LoanProduct.SBA_7A,
                LoanPurpose.EQUIPMENT_PURCHASE,
                new BigDecimal("350000.00"),
                120
        );
    }

    private Business createBusiness(Long id) {

        Business business = new Business();

        ReflectionTestUtils.setField(
                business,
                "id",
                id
        );

        business.setLegalName(
                "Atlanta Logistics LLC"
        );

        business.setDbaName(
                "Atlanta Logistics"
        );

        business.setEin(
                "123456789"
        );

        business.setBusinessType(
                BusinessType.LLC
        );

        business.setIndustry(
                "Transportation"
        );

        business.setAddressLine1(
                "1200 Peachtree Industrial Blvd"
        );

        business.setCity(
                "Atlanta"
        );

        business.setState(
                "GA"
        );

        business.setPostalCode(
                "30309"
        );

        business.onCreate();

        return business;
    }

    private LoanApplication createApplication(
            Long id,
            ApplicationStatus status
    ) {

        LoanApplication application =
                new LoanApplication();

        ReflectionTestUtils.setField(
                application,
                "id",
                id
        );

        application.setBusiness(
                createBusiness(1L)
        );

        application.setApplicationNumber(
                "APP-2026-TEST0001"
        );

        application.setLoanProduct(
                LoanProduct.SBA_7A
        );

        application.setLoanPurpose(
                LoanPurpose.EQUIPMENT_PURCHASE
        );

        application.setRequestedAmount(
                new BigDecimal("350000.00")
        );

        application.setRequestedTermMonths(
                120
        );

        application.setStatus(status);

        if (status != ApplicationStatus.DRAFT) {
            application.setSubmittedAt(
                    LocalDateTime.now()
            );
        }

        application.onCreate();

        // onCreate preserves a non-null status.
        application.setStatus(status);

        return application;
    }
}
