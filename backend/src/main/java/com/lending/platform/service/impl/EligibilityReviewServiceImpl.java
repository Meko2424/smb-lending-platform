package com.lending.platform.service.impl;

import com.lending.platform.dto.eligibility.EligibilityCriterionRequest;
import com.lending.platform.dto.eligibility.EligibilityReviewRequest;
import com.lending.platform.dto.eligibility.EligibilityReviewResponse;
import com.lending.platform.entity.*;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.mapper.EligibilityReviewMapper;
import com.lending.platform.repository.EligibilityCriterionRepository;
import com.lending.platform.repository.EligibilityReviewRepository;
import com.lending.platform.repository.LoanApplicationRepository;
import com.lending.platform.repository.UserRepository;
import com.lending.platform.service.EligibilityReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class EligibilityReviewServiceImpl
        implements EligibilityReviewService {

    private final EligibilityReviewRepository reviewRepository;
    private final EligibilityCriterionRepository criterionRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final EligibilityReviewMapper mapper;

    public EligibilityReviewServiceImpl(
            EligibilityReviewRepository reviewRepository,
            EligibilityCriterionRepository criterionRepository,
            LoanApplicationRepository loanApplicationRepository,
            UserRepository userRepository,
            EligibilityReviewMapper mapper
    ) {
        this.reviewRepository = reviewRepository;
        this.criterionRepository = criterionRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public EligibilityReviewResponse createReview(
            Long applicationId,
            EligibilityReviewRequest request
    ) {

        LoanApplication application = findApplication(applicationId);

        if (reviewRepository.existsByApplicationId(applicationId)) {
            throw new ResourceConflictException(
                    "Eligibility review already exists for this application"
            );
        }

        EligibilityReview review = new EligibilityReview();
        review.setApplication(application);
        review.setStatus(EligibilityReviewStatus.PENDING);

        if (request != null) {
            review.setSummary(request.summary());
        }

        EligibilityReview savedReview =
                reviewRepository.save(review);

        List<EligibilityCriterion> criteria =
                Arrays.stream(EligibilityCriterionType.values())
                        .map(type -> createCriterion(savedReview, type))
                        .toList();

        criterionRepository.saveAll(criteria);

        return mapper.toResponse(savedReview, criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public EligibilityReviewResponse getReview(
            Long applicationId
    ) {

        EligibilityReview review = findReview(applicationId);

        return mapper.toResponse(
                review,
                getCriteria(review.getId())
        );
    }

    @Override
    public EligibilityReviewResponse startReview(
            Long applicationId,
            String authenticatedEmail
    ) {

        EligibilityReview review = findReview(applicationId);

        if (review.getStatus() != EligibilityReviewStatus.PENDING) {
            throw new ResourceConflictException(
                    "Only pending eligibility reviews can be started"
            );
        }

        User reviewer = findUser(authenticatedEmail);

        review.setStatus(EligibilityReviewStatus.IN_REVIEW);
        review.setReviewedByUser(reviewer);
        review.setStartedAt(LocalDateTime.now());
        review.touch();

        EligibilityReview saved =
                reviewRepository.save(review);

        return mapper.toResponse(
                saved,
                getCriteria(saved.getId())
        );
    }

    @Override
    public EligibilityReviewResponse updateCriterion(
            Long applicationId,
            EligibilityCriterionType criterionType,
            EligibilityCriterionRequest request
    ) {

        EligibilityReview review = findReview(applicationId);

        if (review.getStatus()
                != EligibilityReviewStatus.IN_REVIEW) {

            throw new ResourceConflictException(
                    "Eligibility criteria can only be updated while the review is in progress"
            );
        }

        EligibilityCriterion criterion =
                criterionRepository
                        .findByEligibilityReviewIdAndCriterionType(
                                review.getId(),
                                criterionType
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Eligibility criterion not found: "
                                                + criterionType
                                )
                        );

        if (request.status()
                == EligibilityCriterionStatus.PENDING) {

            criterion.setStatus(
                    EligibilityCriterionStatus.PENDING
            );
        } else {
            criterion.setStatus(request.status());
        }

        criterion.setNotes(request.notes());
        criterion.touch();

        criterionRepository.save(criterion);

        return mapper.toResponse(
                review,
                getCriteria(review.getId())
        );
    }

    @Override
    public EligibilityReviewResponse completeAsEligible(
            Long applicationId,
            String authenticatedEmail
    ) {

        EligibilityReview review = findReview(applicationId);

        requireInReview(review);

        List<EligibilityCriterion> criteria =
                getCriteria(review.getId());

        boolean allSatisfied =
                criteria.stream()
                        .allMatch(criterion ->
                                criterion.getStatus()
                                        == EligibilityCriterionStatus.PASSED
                                        ||
                                        criterion.getStatus()
                                                == EligibilityCriterionStatus.NOT_APPLICABLE
                        );

        if (!allSatisfied) {
            throw new ResourceConflictException(
                    "All eligibility criteria must be passed or not applicable before the review can be completed as eligible"
            );
        }

        User reviewer = findUser(authenticatedEmail);

        review.setStatus(
                EligibilityReviewStatus.ELIGIBLE
        );
        review.setReviewedByUser(reviewer);
        review.setCompletedAt(LocalDateTime.now());
        review.touch();

        EligibilityReview saved =
                reviewRepository.save(review);

        return mapper.toResponse(saved, criteria);
    }

    @Override
    public EligibilityReviewResponse completeAsIneligible(
            Long applicationId,
            EligibilityReviewRequest request,
            String authenticatedEmail
    ) {

        EligibilityReview review = findReview(applicationId);

        requireInReview(review);

        List<EligibilityCriterion> criteria =
                getCriteria(review.getId());

        boolean hasFailedCriterion =
                criteria.stream()
                        .anyMatch(criterion ->
                                criterion.getStatus()
                                        == EligibilityCriterionStatus.FAILED
                        );

        if (!hasFailedCriterion) {
            throw new ResourceConflictException(
                    "At least one eligibility criterion must be failed before the review can be completed as ineligible"
            );
        }

        User reviewer = findUser(authenticatedEmail);

        review.setStatus(
                EligibilityReviewStatus.INELIGIBLE
        );
        review.setReviewedByUser(reviewer);
        review.setCompletedAt(LocalDateTime.now());

        if (request != null) {
            review.setSummary(request.summary());
        }

        review.touch();

        EligibilityReview saved =
                reviewRepository.save(review);

        return mapper.toResponse(saved, criteria);
    }

    private EligibilityCriterion createCriterion(
            EligibilityReview review,
            EligibilityCriterionType type
    ) {

        EligibilityCriterion criterion =
                new EligibilityCriterion();

        criterion.setEligibilityReview(review);
        criterion.setCriterionType(type);
        criterion.setStatus(
                EligibilityCriterionStatus.PENDING
        );

        return criterion;
    }

    private LoanApplication findApplication(
            Long applicationId
    ) {
        return loanApplicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan application not found with id: "
                                        + applicationId
                        )
                );
    }

    private EligibilityReview findReview(
            Long applicationId
    ) {
        return reviewRepository
                .findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Eligibility review not found for application id: "
                                        + applicationId
                        )
                );
    }

    private User findUser(String email) {
        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    private List<EligibilityCriterion> getCriteria(
            Long reviewId
    ) {
        return criterionRepository
                .findAllByEligibilityReviewIdOrderByIdAsc(
                        reviewId
                );
    }

    private void requireInReview(
            EligibilityReview review
    ) {
        if (review.getStatus()
                != EligibilityReviewStatus.IN_REVIEW) {

            throw new ResourceConflictException(
                    "Only eligibility reviews in progress can be completed"
            );
        }
    }
}
