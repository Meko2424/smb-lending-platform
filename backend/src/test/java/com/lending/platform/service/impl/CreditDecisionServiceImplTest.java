package com.lending.platform.service.impl;


import com.lending.platform.credit.dto.request.CreditApprovalRequest;
import com.lending.platform.credit.dto.request.CreditDeclineRequest;
import com.lending.platform.credit.dto.response.CreditDecisionResponse;
import com.lending.platform.credit.entity.CreditDecision;
import com.lending.platform.credit.entity.CreditDecisionStatus;
import com.lending.platform.credit.entity.CreditDecisionType;
import com.lending.platform.credit.mapper.CreditDecisionMapper;
import com.lending.platform.credit.repository.CreditDecisionRepository;
import com.lending.platform.credit.service.impl.CreditDecisionServiceImpl;
import com.lending.platform.entity.LoanApplication;
import com.lending.platform.entity.User;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.repository.LoanApplicationRepository;
import com.lending.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditDecisionServiceImplTest {

    @Mock
    private CreditDecisionRepository creditDecisionRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CreditDecisionMapper mapper;

    private CreditDecisionServiceImpl service;

    private LoanApplication application;
    private User creditManager;

    @BeforeEach
    void setUp() {
        service = new CreditDecisionServiceImpl(
                creditDecisionRepository,
                loanApplicationRepository,
                userRepository,
                mapper
        );

        application = new LoanApplication();
        application.setRequestedAmount(
                new BigDecimal("350000.00")
        );
        application.setRequestedTermMonths(120);

        creditManager = new User();
        creditManager.setFirstName("System");
        creditManager.setLastName("Administrator");
        creditManager.setEmail("admin@lending.local");
    }

    @Test
    void createDecision_shouldCreatePendingDecisionWithSnapshots() {

        when(loanApplicationRepository.findById(279L))
                .thenReturn(Optional.of(application));

        when(creditDecisionRepository.existsByApplicationId(279L))
                .thenReturn(false);

        when(creditDecisionRepository.save(any(CreditDecision.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(CreditDecision.class)))
                .thenReturn(mock(CreditDecisionResponse.class));

        service.createDecision(279L);

        ArgumentCaptor<CreditDecision> captor =
                ArgumentCaptor.forClass(CreditDecision.class);

        verify(creditDecisionRepository).save(captor.capture());

        CreditDecision saved = captor.getValue();

        assertSame(application, saved.getApplication());

        assertEquals(
                CreditDecisionStatus.PENDING,
                saved.getStatus()
        );

        assertEquals(
                new BigDecimal("350000.00"),
                saved.getRequestedAmount()
        );

        assertEquals(
                120,
                saved.getRequestedTermMonths()
        );
    }

    @Test
    void createDecision_shouldRejectDuplicateDecision() {

        when(loanApplicationRepository.findById(279L))
                .thenReturn(Optional.of(application));

        when(creditDecisionRepository.existsByApplicationId(279L))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> service.createDecision(279L)
        );

        verify(
                creditDecisionRepository,
                never()
        ).save(any());
    }

    @Test
    void startReview_shouldMovePendingDecisionToInReview() {

        CreditDecision decision =
                pendingDecision();

        when(creditDecisionRepository.findByApplicationId(279L))
                .thenReturn(Optional.of(decision));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(creditManager));

        when(creditDecisionRepository.save(any(CreditDecision.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(CreditDecision.class)))
                .thenReturn(mock(CreditDecisionResponse.class));

        service.startReview(
                279L,
                "admin@lending.local"
        );

        assertEquals(
                CreditDecisionStatus.IN_REVIEW,
                decision.getStatus()
        );

        assertSame(
                creditManager,
                decision.getCreditManagerUser()
        );

        assertNotNull(
                decision.getStartedAt()
        );
    }

    @Test
    void startReview_shouldRejectDecisionThatIsNotPending() {

        CreditDecision decision =
                pendingDecision();

        decision.setStatus(
                CreditDecisionStatus.IN_REVIEW
        );

        when(creditDecisionRepository.findByApplicationId(279L))
                .thenReturn(Optional.of(decision));

        assertThrows(
                ResourceConflictException.class,
                () -> service.startReview(
                        279L,
                        "admin@lending.local"
                )
        );

        verify(
                userRepository,
                never()
        ).findByEmailIgnoreCase(anyString());
    }

    @Test
    void approve_shouldApproveAsRequestedWhenTermsMatch() {

        CreditDecision decision =
                inReviewDecision();

        when(creditDecisionRepository.findByApplicationId(279L))
                .thenReturn(Optional.of(decision));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(creditManager));

        when(creditDecisionRepository.save(any(CreditDecision.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(CreditDecision.class)))
                .thenReturn(mock(CreditDecisionResponse.class));

        CreditApprovalRequest request =
                new CreditApprovalRequest(
                        new BigDecimal("350000.00"),
                        120,
                        new BigDecimal("8.2500"),
                        "Standard closing conditions",
                        "Approved based on underwriting recommendation"
                );

        service.approve(
                279L,
                request,
                "admin@lending.local"
        );

        assertEquals(
                CreditDecisionStatus.APPROVED,
                decision.getStatus()
        );

        assertEquals(
                CreditDecisionType.APPROVE_AS_REQUESTED,
                decision.getDecisionType()
        );

        assertEquals(
                new BigDecimal("350000.00"),
                decision.getApprovedAmount()
        );

        assertEquals(
                120,
                decision.getApprovedTermMonths()
        );

        assertEquals(
                new BigDecimal("8.2500"),
                decision.getInterestRate()
        );

        assertSame(
                creditManager,
                decision.getCreditManagerUser()
        );

        assertNotNull(
                decision.getDecidedAt()
        );
    }

    @Test
    void approve_shouldApproveWithChangesWhenAmountChanges() {

        CreditDecision decision =
                inReviewDecision();

        when(creditDecisionRepository.findByApplicationId(279L))
                .thenReturn(Optional.of(decision));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(creditManager));

        when(creditDecisionRepository.save(any(CreditDecision.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(CreditDecision.class)))
                .thenReturn(mock(CreditDecisionResponse.class));

        CreditApprovalRequest request =
                new CreditApprovalRequest(
                        new BigDecimal("300000.00"),
                        120,
                        new BigDecimal("8.5000"),
                        "Reduced loan amount",
                        "Approved with reduced exposure"
                );

        service.approve(
                279L,
                request,
                "admin@lending.local"
        );

        assertEquals(
                CreditDecisionType.APPROVE_WITH_CHANGES,
                decision.getDecisionType()
        );

        assertEquals(
                new BigDecimal("300000.00"),
                decision.getApprovedAmount()
        );
    }

    @Test
    void approve_shouldApproveWithChangesWhenTermChanges() {

        CreditDecision decision =
                inReviewDecision();

        when(creditDecisionRepository.findByApplicationId(279L))
                .thenReturn(Optional.of(decision));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(creditManager));

        when(creditDecisionRepository.save(any(CreditDecision.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(CreditDecision.class)))
                .thenReturn(mock(CreditDecisionResponse.class));

        CreditApprovalRequest request =
                new CreditApprovalRequest(
                        new BigDecimal("350000.00"),
                        84,
                        new BigDecimal("8.5000"),
                        null,
                        "Shorter approved term"
                );

        service.approve(
                279L,
                request,
                "admin@lending.local"
        );

        assertEquals(
                CreditDecisionType.APPROVE_WITH_CHANGES,
                decision.getDecisionType()
        );
    }

    @Test
    void approve_shouldRejectDecisionThatIsNotInReview() {

        CreditDecision decision =
                pendingDecision();

        when(creditDecisionRepository.findByApplicationId(279L))
                .thenReturn(Optional.of(decision));

        CreditApprovalRequest request =
                new CreditApprovalRequest(
                        new BigDecimal("350000.00"),
                        120,
                        new BigDecimal("8.2500"),
                        null,
                        null
                );

        assertThrows(
                ResourceConflictException.class,
                () -> service.approve(
                        279L,
                        request,
                        "admin@lending.local"
                )
        );

        verify(
                creditDecisionRepository,
                never()
        ).save(any());
    }

    @Test
    void decline_shouldCompleteDecisionAsDeclined() {

        CreditDecision decision =
                inReviewDecision();

        decision.setApprovedAmount(
                new BigDecimal("300000.00")
        );
        decision.setApprovedTermMonths(84);
        decision.setInterestRate(
                new BigDecimal("8.5000")
        );
        decision.setConditions("Old conditions");

        when(creditDecisionRepository.findByApplicationId(279L))
                .thenReturn(Optional.of(decision));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(creditManager));

        when(creditDecisionRepository.save(any(CreditDecision.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(CreditDecision.class)))
                .thenReturn(mock(CreditDecisionResponse.class));

        CreditDeclineRequest request =
                new CreditDeclineRequest(
                        "Insufficient repayment capacity",
                        "Credit risk exceeds acceptable threshold"
                );

        service.decline(
                279L,
                request,
                "admin@lending.local"
        );

        assertEquals(
                CreditDecisionStatus.DECLINED,
                decision.getStatus()
        );

        assertEquals(
                CreditDecisionType.DECLINE,
                decision.getDecisionType()
        );

        assertEquals(
                "Insufficient repayment capacity",
                decision.getDeclineReason()
        );

        assertNull(
                decision.getApprovedAmount()
        );

        assertNull(
                decision.getApprovedTermMonths()
        );

        assertNull(
                decision.getInterestRate()
        );

        assertNull(
                decision.getConditions()
        );

        assertNotNull(
                decision.getDecidedAt()
        );

        assertSame(
                creditManager,
                decision.getCreditManagerUser()
        );
    }

    @Test
    void decline_shouldRejectDecisionThatIsNotInReview() {

        CreditDecision decision =
                pendingDecision();

        when(creditDecisionRepository.findByApplicationId(279L))
                .thenReturn(Optional.of(decision));

        CreditDeclineRequest request =
                new CreditDeclineRequest(
                        "Insufficient repayment capacity",
                        null
                );

        assertThrows(
                ResourceConflictException.class,
                () -> service.decline(
                        279L,
                        request,
                        "admin@lending.local"
                )
        );

        verify(
                creditDecisionRepository,
                never()
        ).save(any());
    }

    @Test
    void approve_shouldRejectAlreadyApprovedDecision() {

        CreditDecision decision = inReviewDecision();
        decision.setStatus(CreditDecisionStatus.APPROVED);

        when(creditDecisionRepository.findByApplicationId(279L))
                .thenReturn(Optional.of(decision));

        CreditApprovalRequest request =
                new CreditApprovalRequest(
                        new BigDecimal("350000.00"),
                        120,
                        new BigDecimal("8.2500"),
                        null,
                        null
                );

        assertThrows(
                ResourceConflictException.class,
                () -> service.approve(
                        279L,
                        request,
                        "admin@lending.local"
                )
        );

        verify(
                creditDecisionRepository,
                never()
        ).save(any());
    }

    @Test
    void decline_shouldRejectAlreadyDeclinedDecision() {

        CreditDecision decision = inReviewDecision();
        decision.setStatus(CreditDecisionStatus.DECLINED);

        when(creditDecisionRepository.findByApplicationId(279L))
                .thenReturn(Optional.of(decision));

        CreditDeclineRequest request =
                new CreditDeclineRequest(
                        "Insufficient repayment capacity",
                        null
                );

        assertThrows(
                ResourceConflictException.class,
                () -> service.decline(
                        279L,
                        request,
                        "admin@lending.local"
                )
        );

        verify(
                creditDecisionRepository,
                never()
        ).save(any());
    }

    @Test
    void approve_shouldUseCompareToForEquivalentMoneyValues() {

        CreditDecision decision = inReviewDecision();
        decision.setRequestedAmount(
                new BigDecimal("350000.00")
        );

        when(creditDecisionRepository.findByApplicationId(279L))
                .thenReturn(Optional.of(decision));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(creditManager));

        when(creditDecisionRepository.save(any(CreditDecision.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mapper.toResponse(any(CreditDecision.class)))
                .thenReturn(mock(CreditDecisionResponse.class));

        CreditApprovalRequest request =
                new CreditApprovalRequest(
                        new BigDecimal("350000.000"),
                        120,
                        new BigDecimal("8.2500"),
                        null,
                        null
                );

        service.approve(
                279L,
                request,
                "admin@lending.local"
        );

        assertEquals(
                CreditDecisionType.APPROVE_AS_REQUESTED,
                decision.getDecisionType()
        );
    }

    private CreditDecision pendingDecision() {

        CreditDecision decision =
                new CreditDecision();

        decision.setApplication(application);

        decision.setStatus(
                CreditDecisionStatus.PENDING
        );

        decision.setRequestedAmount(
                new BigDecimal("350000.00")
        );

        decision.setRequestedTermMonths(120);

        return decision;
    }

    private CreditDecision inReviewDecision() {

        CreditDecision decision =
                pendingDecision();

        decision.setStatus(
                CreditDecisionStatus.IN_REVIEW
        );

        decision.setCreditManagerUser(
                creditManager
        );

        return decision;
    }
}
