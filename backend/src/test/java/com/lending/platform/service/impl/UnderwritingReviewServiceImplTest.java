package com.lending.platform.service.impl;


import com.lending.platform.entity.Business;
import com.lending.platform.entity.LoanApplication;
import com.lending.platform.entity.LoanProduct;
import com.lending.platform.entity.LoanPurpose;
import com.lending.platform.entity.User;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.repository.LoanApplicationRepository;
import com.lending.platform.repository.UserRepository;
import com.lending.platform.underwriting.dto.request.UnderwritingReviewRequest;
import com.lending.platform.underwriting.entity.RiskRating;
import com.lending.platform.underwriting.entity.UnderwritingReview;
import com.lending.platform.underwriting.entity.UnderwritingReviewStatus;
import com.lending.platform.underwriting.mapper.UnderwritingReviewMapper;
import com.lending.platform.underwriting.repository.UnderwritingReviewRepository;
import com.lending.platform.underwriting.service.impl.UnderwritingReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnderwritingReviewServiceImplTest {

    @Mock
    private UnderwritingReviewRepository underwritingReviewRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private UserRepository userRepository;

    private UnderwritingReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UnderwritingReviewServiceImpl(
                underwritingReviewRepository,
                loanApplicationRepository,
                userRepository,
                new UnderwritingReviewMapper()
        );
    }

    @Test
    void createReview_shouldCreatePendingSnapshot() {

        LoanApplication application = createApplication();

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(underwritingReviewRepository.existsByApplicationId(10L))
                .thenReturn(false);

        when(underwritingReviewRepository.save(any(UnderwritingReview.class)))
                .thenAnswer(invocation -> {
                    UnderwritingReview review = invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            review,
                            "id",
                            20L
                    );

                    review.prePersist();

                    return review;
                });

        var response = service.createReview(10L);

        assertEquals(20L, response.id());
        assertEquals(10L, response.applicationId());
        assertEquals(
                UnderwritingReviewStatus.PENDING,
                response.status()
        );

        assertEquals(
                new BigDecimal("350000.00"),
                response.requestedAmount()
        );

        assertEquals(
                new BigDecimal("2400000.00"),
                response.annualRevenue()
        );

        assertNull(response.debtServiceCoverageRatio());

        verify(underwritingReviewRepository)
                .save(any(UnderwritingReview.class));
    }

    @Test
    void createReview_shouldRejectDuplicateReview() {

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(createApplication()));

        when(underwritingReviewRepository.existsByApplicationId(10L))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> service.createReview(10L)
        );

        verify(underwritingReviewRepository, never())
                .save(any());
    }

    @Test
    void startReview_shouldMovePendingToInReview() {

        UnderwritingReview review =
                createReview(
                        UnderwritingReviewStatus.PENDING
                );

        User user = createUser();

        when(underwritingReviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(user));

        when(underwritingReviewRepository.save(review))
                .thenReturn(review);

        var response = service.startReview(
                10L,
                "admin@lending.local"
        );

        assertEquals(
                UnderwritingReviewStatus.IN_REVIEW,
                response.status()
        );

        assertNotNull(response.startedAt());

        assertEquals(
                1L,
                response.underwriterUserId()
        );

        assertEquals(
                "System Administrator",
                response.underwriterUserName()
        );
    }

    @Test
    void updateAnalysis_shouldCalculateDscr() {

        UnderwritingReview review =
                createReview(
                        UnderwritingReviewStatus.IN_REVIEW
                );

        when(underwritingReviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(underwritingReviewRepository.save(review))
                .thenReturn(review);

        UnderwritingReviewRequest request =
                createRequest(
                        new BigDecimal("12500.00"),
                        new BigDecimal("10000.00"),
                        RiskRating.MODERATE,
                        "Recommendable credit"
                );

        var response = service.updateAnalysis(
                10L,
                request
        );

        assertEquals(
                new BigDecimal("1.2500"),
                response.debtServiceCoverageRatio()
        );

        assertEquals(
                new BigDecimal("12500.00"),
                response.monthlyCashFlow()
        );

        assertEquals(
                new BigDecimal("10000.00"),
                response.monthlyDebtService()
        );
    }

    @Test
    void updateAnalysis_shouldLeaveDscrNullWhenDebtServiceIsZero() {

        UnderwritingReview review =
                createReview(
                        UnderwritingReviewStatus.IN_REVIEW
                );

        when(underwritingReviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(underwritingReviewRepository.save(review))
                .thenReturn(review);

        UnderwritingReviewRequest request =
                createRequest(
                        new BigDecimal("12500.00"),
                        BigDecimal.ZERO,
                        RiskRating.LOW,
                        "No debt service"
                );

        var response = service.updateAnalysis(
                10L,
                request
        );

        assertNull(
                response.debtServiceCoverageRatio()
        );
    }

    @Test
    void updateAnalysis_shouldRejectWhenNotInReview() {

        UnderwritingReview review =
                createReview(
                        UnderwritingReviewStatus.PENDING
                );

        when(underwritingReviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        assertThrows(
                ResourceConflictException.class,
                () -> service.updateAnalysis(
                        10L,
                        createRequest(
                                new BigDecimal("12000.00"),
                                new BigDecimal("10000.00"),
                                RiskRating.MODERATE,
                                "Analysis"
                        )
                )
        );

        verify(underwritingReviewRepository, never())
                .save(any());
    }

    @Test
    void requestMoreInformation_shouldMoveToNeedsMoreInformation() {

        UnderwritingReview review =
                createReview(
                        UnderwritingReviewStatus.IN_REVIEW
                );

        User user = createUser();

        when(underwritingReviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(user));

        when(underwritingReviewRepository.save(review))
                .thenReturn(review);

        var response =
                service.requestMoreInformation(
                        10L,
                        createRequest(
                                new BigDecimal("12000.00"),
                                new BigDecimal("10000.00"),
                                RiskRating.MODERATE,
                                "Need updated bank statements"
                        ),
                        "admin@lending.local"
                );

        assertEquals(
                UnderwritingReviewStatus.NEEDS_MORE_INFORMATION,
                response.status()
        );

        assertNull(response.completedAt());

        assertEquals(
                "System Administrator",
                response.underwriterUserName()
        );
    }

    @Test
    void resumeReview_shouldMoveBackToInReview() {

        UnderwritingReview review =
                createReview(
                        UnderwritingReviewStatus.NEEDS_MORE_INFORMATION
                );

        User user = createUser();

        when(underwritingReviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(user));

        when(underwritingReviewRepository.save(review))
                .thenReturn(review);

        var response = service.resumeReview(
                10L,
                "admin@lending.local"
        );

        assertEquals(
                UnderwritingReviewStatus.IN_REVIEW,
                response.status()
        );

        assertNull(response.completedAt());
    }

    @Test
    void resumeReview_shouldRejectInvalidStatus() {

        UnderwritingReview review =
                createReview(
                        UnderwritingReviewStatus.IN_REVIEW
                );

        when(underwritingReviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        assertThrows(
                ResourceConflictException.class,
                () -> service.resumeReview(
                        10L,
                        "admin@lending.local"
                )
        );

        verify(userRepository, never())
                .findByEmailIgnoreCase(anyString());
    }

    @Test
    void recommendApproval_shouldRequireRiskRating() {

        UnderwritingReview review =
                createReview(
                        UnderwritingReviewStatus.IN_REVIEW
                );

        when(underwritingReviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        UnderwritingReviewRequest request =
                createRequest(
                        new BigDecimal("12500.00"),
                        new BigDecimal("10000.00"),
                        null,
                        "Recommend approval"
                );

        assertThrows(
                ResourceConflictException.class,
                () -> service.recommendApproval(
                        10L,
                        request,
                        "admin@lending.local"
                )
        );

        verify(userRepository, never())
                .findByEmailIgnoreCase(anyString());
    }

    @Test
    void recommendApproval_shouldRequireRecommendationNotes() {

        UnderwritingReview review =
                createReview(
                        UnderwritingReviewStatus.IN_REVIEW
                );

        when(underwritingReviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        UnderwritingReviewRequest request =
                createRequest(
                        new BigDecimal("12500.00"),
                        new BigDecimal("10000.00"),
                        RiskRating.MODERATE,
                        "   "
                );

        assertThrows(
                ResourceConflictException.class,
                () -> service.recommendApproval(
                        10L,
                        request,
                        "admin@lending.local"
                )
        );

        verify(userRepository, never())
                .findByEmailIgnoreCase(anyString());
    }

    @Test
    void recommendApproval_shouldCompleteReview() {

        UnderwritingReview review =
                createReview(
                        UnderwritingReviewStatus.IN_REVIEW
                );

        User user = createUser();

        when(underwritingReviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(user));

        when(underwritingReviewRepository.save(review))
                .thenReturn(review);

        var response = service.recommendApproval(
                10L,
                createRequest(
                        new BigDecimal("14000.00"),
                        new BigDecimal("10000.00"),
                        RiskRating.LOW,
                        "Recommend approval based on strong cash flow"
                ),
                "admin@lending.local"
        );

        assertEquals(
                UnderwritingReviewStatus.RECOMMEND_APPROVAL,
                response.status()
        );

        assertNotNull(response.completedAt());

        assertEquals(
                new BigDecimal("1.4000"),
                response.debtServiceCoverageRatio()
        );

        assertEquals(
                "System Administrator",
                response.underwriterUserName()
        );
    }

    @Test
    void recommendDecline_shouldCompleteReview() {

        UnderwritingReview review =
                createReview(
                        UnderwritingReviewStatus.IN_REVIEW
                );

        User user = createUser();

        when(underwritingReviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(userRepository.findByEmailIgnoreCase(
                "admin@lending.local"
        )).thenReturn(Optional.of(user));

        when(underwritingReviewRepository.save(review))
                .thenReturn(review);

        var response = service.recommendDecline(
                10L,
                createRequest(
                        new BigDecimal("7000.00"),
                        new BigDecimal("10000.00"),
                        RiskRating.HIGH,
                        "Recommend decline due to weak cash flow"
                ),
                "admin@lending.local"
        );

        assertEquals(
                UnderwritingReviewStatus.RECOMMEND_DECLINE,
                response.status()
        );

        assertNotNull(response.completedAt());

        assertEquals(
                new BigDecimal("0.7000"),
                response.debtServiceCoverageRatio()
        );
    }

    private UnderwritingReviewRequest createRequest(
            BigDecimal monthlyCashFlow,
            BigDecimal monthlyDebtService,
            RiskRating riskRating,
            String recommendationNotes
    ) {

        return new UnderwritingReviewRequest(
                new BigDecimal("100000.00"),
                monthlyDebtService,
                monthlyCashFlow,
                riskRating,
                "Experienced management team",
                "Customer concentration",
                "Industry volatility",
                "Strong collateral and liquidity",
                "Cash flow analysis supports repayment assessment",
                recommendationNotes
        );
    }

    private UnderwritingReview createReview(
            UnderwritingReviewStatus status
    ) {

        UnderwritingReview review =
                new UnderwritingReview();

        ReflectionTestUtils.setField(
                review,
                "id",
                20L
        );

        review.setApplication(
                createApplication()
        );

        review.setStatus(status);
        review.setRequestedAmount(
                new BigDecimal("350000.00")
        );
        review.setAnnualRevenue(
                new BigDecimal("2400000.00")
        );

        review.prePersist();
        review.setStatus(status);

        return review;
    }

    private LoanApplication createApplication() {

        Business business =
                new Business();

        ReflectionTestUtils.setField(
                business,
                "id",
                1L
        );

        business.setLegalName(
                "Atlanta Logistics LLC"
        );

        business.setAnnualRevenue(
                new BigDecimal("2400000.00")
        );

        LoanApplication application =
                new LoanApplication();

        ReflectionTestUtils.setField(
                application,
                "id",
                10L
        );

        application.setBusiness(business);

        application.setApplicationNumber(
                "APP-2026-UW001"
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

        return application;
    }

    private User createUser() {

        User user =
                new User();

        ReflectionTestUtils.setField(
                user,
                "id",
                1L
        );

        user.setFirstName("System");
        user.setLastName("Administrator");
        user.setEmail("admin@lending.local");
        user.setEnabled(true);

        return user;
    }
}
