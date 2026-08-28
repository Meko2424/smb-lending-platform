package com.lending.platform.service.impl;

import com.lending.platform.dto.eligibility.EligibilityCriterionRequest;
import com.lending.platform.dto.eligibility.EligibilityReviewRequest;
import com.lending.platform.entity.*;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.mapper.EligibilityReviewMapper;
import com.lending.platform.repository.EligibilityCriterionRepository;
import com.lending.platform.repository.EligibilityReviewRepository;
import com.lending.platform.repository.LoanApplicationRepository;
import com.lending.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EligibilityReviewServiceImplTest {

    @Mock
    private EligibilityReviewRepository reviewRepository;

    @Mock
    private EligibilityCriterionRepository criterionRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private UserRepository userRepository;

    private EligibilityReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EligibilityReviewServiceImpl(
                reviewRepository,
                criterionRepository,
                loanApplicationRepository,
                userRepository,
                new EligibilityReviewMapper()
        );
    }

    @Test
    void createReview_shouldCreatePendingReviewAndCriteria() {
        LoanApplication application = application(10L);

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(reviewRepository.existsByApplicationId(10L))
                .thenReturn(false);

        when(reviewRepository.save(any(EligibilityReview.class)))
                .thenAnswer(invocation -> {
                    EligibilityReview review = invocation.getArgument(0);
                    ReflectionTestUtils.setField(review, "id", 20L);
                    review.onCreate();
                    return review;
                });

        when(criterionRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createReview(
                10L,
                new EligibilityReviewRequest("Initial eligibility screening")
        );

        assertEquals(EligibilityReviewStatus.PENDING, response.status());
        assertEquals(10L, response.applicationId());
        assertEquals(
                EligibilityCriterionType.values().length,
                response.criteria().size()
        );

        assertTrue(
                response.criteria().stream()
                        .allMatch(c ->
                                c.status() == EligibilityCriterionStatus.PENDING
                        )
        );

        verify(criterionRepository).saveAll(anyList());
    }

    @Test
    void createReview_shouldRejectDuplicateReview() {
        LoanApplication application = application(10L);

        when(loanApplicationRepository.findById(10L))
                .thenReturn(Optional.of(application));

        when(reviewRepository.existsByApplicationId(10L))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> service.createReview(
                        10L,
                        new EligibilityReviewRequest(null)
                )
        );
    }

    @Test
    void createReview_shouldThrowWhenApplicationMissing() {
        when(loanApplicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.createReview(
                        999L,
                        new EligibilityReviewRequest(null)
                )
        );
    }

    @Test
    void startReview_shouldMovePendingToInReview() {
        EligibilityReview review =
                review(20L, 10L, EligibilityReviewStatus.PENDING);

        User reviewer = user(5L);

        when(reviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(userRepository.findByEmailIgnoreCase("admin@lending.local"))
                .thenReturn(Optional.of(reviewer));

        when(reviewRepository.save(review))
                .thenReturn(review);

        when(criterionRepository.findAllByEligibilityReviewIdOrderByIdAsc(20L))
                .thenReturn(new ArrayList<>());

        var response =
                service.startReview(
                        10L,
                        "admin@lending.local"
                );

        assertEquals(
                EligibilityReviewStatus.IN_REVIEW,
                response.status()
        );

        assertNotNull(response.startedAt());
        assertEquals(5L, response.reviewedByUserId());
        assertEquals(
                "System Administrator",
                response.reviewedByUserName()
        );
    }

    @Test
    void startReview_shouldRejectNonPendingReview() {
        EligibilityReview review =
                review(
                        20L,
                        10L,
                        EligibilityReviewStatus.IN_REVIEW
                );

        when(reviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        assertThrows(
                ResourceConflictException.class,
                () -> service.startReview(
                        10L,
                        "admin@lending.local"
                )
        );
    }

    @Test
    void updateCriterion_shouldUpdateCriterionWhileInReview() {
        EligibilityReview review =
                review(
                        20L,
                        10L,
                        EligibilityReviewStatus.IN_REVIEW
                );

        EligibilityCriterion criterion =
                criterion(
                        30L,
                        review,
                        EligibilityCriterionType.BUSINESS_OPERATING_HISTORY,
                        EligibilityCriterionStatus.PENDING
                );

        when(reviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(criterionRepository
                .findByEligibilityReviewIdAndCriterionType(
                        20L,
                        EligibilityCriterionType.BUSINESS_OPERATING_HISTORY
                ))
                .thenReturn(Optional.of(criterion));

        when(criterionRepository.save(criterion))
                .thenReturn(criterion);

        when(criterionRepository.findAllByEligibilityReviewIdOrderByIdAsc(20L))
                .thenReturn(List.of(criterion));

        var response =
                service.updateCriterion(
                        10L,
                        EligibilityCriterionType.BUSINESS_OPERATING_HISTORY,
                        new EligibilityCriterionRequest(
                                EligibilityCriterionStatus.PASSED,
                                "Business has sufficient operating history"
                        )
                );

        assertEquals(
                EligibilityCriterionStatus.PASSED,
                response.criteria().getFirst().status()
        );

        assertEquals(
                "Business has sufficient operating history",
                response.criteria().getFirst().notes()
        );
    }

    @Test
    void updateCriterion_shouldRejectWhenReviewNotInProgress() {
        EligibilityReview review =
                review(
                        20L,
                        10L,
                        EligibilityReviewStatus.PENDING
                );

        when(reviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        assertThrows(
                ResourceConflictException.class,
                () -> service.updateCriterion(
                        10L,
                        EligibilityCriterionType.REVENUE_REQUIREMENT,
                        new EligibilityCriterionRequest(
                                EligibilityCriterionStatus.PASSED,
                                null
                        )
                )
        );
    }

    @Test
    void completeAsEligible_shouldSucceedWhenAllCriteriaSatisfied() {
        EligibilityReview review =
                review(
                        20L,
                        10L,
                        EligibilityReviewStatus.IN_REVIEW
                );

        List<EligibilityCriterion> criteria =
                satisfiedCriteria(review);

        User reviewer = user(5L);

        when(reviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(criterionRepository.findAllByEligibilityReviewIdOrderByIdAsc(20L))
                .thenReturn(criteria);

        when(userRepository.findByEmailIgnoreCase("admin@lending.local"))
                .thenReturn(Optional.of(reviewer));

        when(reviewRepository.save(review))
                .thenReturn(review);

        var response =
                service.completeAsEligible(
                        10L,
                        "admin@lending.local"
                );

        assertEquals(
                EligibilityReviewStatus.ELIGIBLE,
                response.status()
        );

        assertNotNull(response.completedAt());
        assertEquals(5L, response.reviewedByUserId());
    }

    @Test
    void completeAsEligible_shouldRejectPendingCriterion() {
        EligibilityReview review =
                review(
                        20L,
                        10L,
                        EligibilityReviewStatus.IN_REVIEW
                );

        List<EligibilityCriterion> criteria =
                satisfiedCriteria(review);

        criteria.getFirst().setStatus(
                EligibilityCriterionStatus.PENDING
        );

        when(reviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(criterionRepository.findAllByEligibilityReviewIdOrderByIdAsc(20L))
                .thenReturn(criteria);

        assertThrows(
                ResourceConflictException.class,
                () -> service.completeAsEligible(
                        10L,
                        "admin@lending.local"
                )
        );
    }

    @Test
    void completeAsEligible_shouldRejectFailedCriterion() {
        EligibilityReview review =
                review(
                        20L,
                        10L,
                        EligibilityReviewStatus.IN_REVIEW
                );

        List<EligibilityCriterion> criteria =
                satisfiedCriteria(review);

        criteria.getFirst().setStatus(
                EligibilityCriterionStatus.FAILED
        );

        when(reviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(criterionRepository.findAllByEligibilityReviewIdOrderByIdAsc(20L))
                .thenReturn(criteria);

        assertThrows(
                ResourceConflictException.class,
                () -> service.completeAsEligible(
                        10L,
                        "admin@lending.local"
                )
        );
    }

    @Test
    void completeAsIneligible_shouldSucceedWithFailedCriterion() {
        EligibilityReview review =
                review(
                        20L,
                        10L,
                        EligibilityReviewStatus.IN_REVIEW
                );

        List<EligibilityCriterion> criteria =
                satisfiedCriteria(review);

        criteria.getFirst().setStatus(
                EligibilityCriterionStatus.FAILED
        );

        User reviewer = user(5L);

        when(reviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(criterionRepository.findAllByEligibilityReviewIdOrderByIdAsc(20L))
                .thenReturn(criteria);

        when(userRepository.findByEmailIgnoreCase("admin@lending.local"))
                .thenReturn(Optional.of(reviewer));

        when(reviewRepository.save(review))
                .thenReturn(review);

        var response =
                service.completeAsIneligible(
                        10L,
                        new EligibilityReviewRequest(
                                "Revenue requirement was not satisfied"
                        ),
                        "admin@lending.local"
                );

        assertEquals(
                EligibilityReviewStatus.INELIGIBLE,
                response.status()
        );

        assertEquals(
                "Revenue requirement was not satisfied",
                response.summary()
        );

        assertNotNull(response.completedAt());
    }

    @Test
    void completeAsIneligible_shouldRejectWithoutFailedCriterion() {
        EligibilityReview review =
                review(
                        20L,
                        10L,
                        EligibilityReviewStatus.IN_REVIEW
                );

        List<EligibilityCriterion> criteria =
                satisfiedCriteria(review);

        when(reviewRepository.findByApplicationId(10L))
                .thenReturn(Optional.of(review));

        when(criterionRepository.findAllByEligibilityReviewIdOrderByIdAsc(20L))
                .thenReturn(criteria);

        assertThrows(
                ResourceConflictException.class,
                () -> service.completeAsIneligible(
                        10L,
                        new EligibilityReviewRequest(
                                "Ineligible"
                        ),
                        "admin@lending.local"
                )
        );
    }

    private LoanApplication application(Long id) {
        LoanApplication application = new LoanApplication();
        ReflectionTestUtils.setField(application, "id", id);
        return application;
    }

    private EligibilityReview review(
            Long reviewId,
            Long applicationId,
            EligibilityReviewStatus status
    ) {
        EligibilityReview review = new EligibilityReview();
        ReflectionTestUtils.setField(review, "id", reviewId);

        review.setApplication(application(applicationId));
        review.setStatus(status);
        review.onCreate();
        review.setStatus(status);

        return review;
    }

    private EligibilityCriterion criterion(
            Long id,
            EligibilityReview review,
            EligibilityCriterionType type,
            EligibilityCriterionStatus status
    ) {
        EligibilityCriterion criterion =
                new EligibilityCriterion();

        ReflectionTestUtils.setField(criterion, "id", id);

        criterion.setEligibilityReview(review);
        criterion.setCriterionType(type);
        criterion.setStatus(status);
        criterion.onCreate();
        criterion.setStatus(status);

        return criterion;
    }

    private List<EligibilityCriterion> satisfiedCriteria(
            EligibilityReview review
    ) {
        List<EligibilityCriterion> criteria =
                new ArrayList<>();

        long id = 1L;

        for (EligibilityCriterionType type :
                EligibilityCriterionType.values()) {

            EligibilityCriterionStatus status =
                    type == EligibilityCriterionType.OTHER
                            ? EligibilityCriterionStatus.NOT_APPLICABLE
                            : EligibilityCriterionStatus.PASSED;

            criteria.add(
                    criterion(
                            id++,
                            review,
                            type,
                            status
                    )
            );
        }

        return criteria;
    }

    private User user(Long id) {
        User user = new User();

        ReflectionTestUtils.setField(user, "id", id);

        user.setFirstName("System");
        user.setLastName("Administrator");
        user.setEmail("admin@lending.local");

        return user;
    }
}
