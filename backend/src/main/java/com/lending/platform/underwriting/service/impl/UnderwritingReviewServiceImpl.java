package com.lending.platform.underwriting.service.impl;

import com.lending.platform.entity.Business;
import com.lending.platform.entity.LoanApplication;
import com.lending.platform.entity.User;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.repository.LoanApplicationRepository;
import com.lending.platform.repository.UserRepository;
import com.lending.platform.underwriting.dto.request.UnderwritingReviewRequest;
import com.lending.platform.underwriting.dto.response.UnderwritingReviewResponse;
import com.lending.platform.underwriting.entity.UnderwritingReview;
import com.lending.platform.underwriting.entity.UnderwritingReviewStatus;
import com.lending.platform.underwriting.mapper.UnderwritingReviewMapper;
import com.lending.platform.underwriting.repository.UnderwritingReviewRepository;
import com.lending.platform.underwriting.service.UnderwritingReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Transactional
public class UnderwritingReviewServiceImpl
        implements UnderwritingReviewService {

    private final UnderwritingReviewRepository underwritingReviewRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final UnderwritingReviewMapper mapper;

    public UnderwritingReviewServiceImpl(
            UnderwritingReviewRepository underwritingReviewRepository,
            LoanApplicationRepository loanApplicationRepository,
            UserRepository userRepository,
            UnderwritingReviewMapper mapper
    ) {
        this.underwritingReviewRepository = underwritingReviewRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public UnderwritingReviewResponse createReview(
            Long applicationId
    ) {

        LoanApplication application =
                findApplication(applicationId);

        if (underwritingReviewRepository
                .existsByApplicationId(applicationId)) {

            throw new ResourceConflictException(
                    "Underwriting review already exists for this application"
            );
        }

        Business business = application.getBusiness();

        UnderwritingReview review =
                new UnderwritingReview();

        review.setApplication(application);

        review.setStatus(
                UnderwritingReviewStatus.PENDING
        );

        review.setRequestedAmount(
                application.getRequestedAmount()
        );

        review.setAnnualRevenue(
                business.getAnnualRevenue()
        );

        UnderwritingReview savedReview =
                underwritingReviewRepository.save(review);

        return mapper.toResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public UnderwritingReviewResponse getReview(
            Long applicationId
    ) {

        return mapper.toResponse(
                findReview(applicationId)
        );
    }

    @Override
    public UnderwritingReviewResponse startReview(
            Long applicationId,
            String authenticatedEmail
    ) {

        UnderwritingReview review =
                findReview(applicationId);

        if (review.getStatus()
                != UnderwritingReviewStatus.PENDING) {

            throw new ResourceConflictException(
                    "Only pending underwriting reviews can be started"
            );
        }

        User underwriter =
                findUser(authenticatedEmail);

        review.setStatus(
                UnderwritingReviewStatus.IN_REVIEW
        );

        review.setUnderwriterUser(underwriter);

        review.setStartedAt(
                LocalDateTime.now()
        );

        review.touch();

        UnderwritingReview savedReview =
                underwritingReviewRepository.save(review);

        return mapper.toResponse(savedReview);
    }

    @Override
    public UnderwritingReviewResponse resumeReview(
            Long applicationId,
            String authenticatedEmail
    ) {

        UnderwritingReview review =
                findReview(applicationId);

        if (review.getStatus()
                != UnderwritingReviewStatus.NEEDS_MORE_INFORMATION) {

            throw new ResourceConflictException(
                    "Only underwriting reviews waiting for more information can be resumed"
            );
        }

        User underwriter =
                findUser(authenticatedEmail);

        review.setStatus(
                UnderwritingReviewStatus.IN_REVIEW
        );

        review.setUnderwriterUser(underwriter);
        review.setCompletedAt(null);
        review.touch();

        UnderwritingReview savedReview =
                underwritingReviewRepository.save(review);

        return mapper.toResponse(savedReview);
    }

    @Override
    public UnderwritingReviewResponse updateAnalysis(
            Long applicationId,
            UnderwritingReviewRequest request
    ) {

        UnderwritingReview review =
                findReview(applicationId);

        requireInReview(review);

        applyAnalysis(review, request);

        review.touch();

        UnderwritingReview savedReview =
                underwritingReviewRepository.save(review);

        return mapper.toResponse(savedReview);
    }

    @Override
    public UnderwritingReviewResponse recommendApproval(
            Long applicationId,
            UnderwritingReviewRequest request,
            String authenticatedEmail
    ) {

        UnderwritingReview review =
                findReview(applicationId);

        requireInReview(review);

        applyAnalysis(review, request);

        validateRecommendationData(review);

        User underwriter =
                findUser(authenticatedEmail);

        review.setUnderwriterUser(underwriter);

        review.setStatus(
                UnderwritingReviewStatus.RECOMMEND_APPROVAL
        );

        review.setCompletedAt(
                LocalDateTime.now()
        );

        review.touch();

        UnderwritingReview savedReview =
                underwritingReviewRepository.save(review);

        return mapper.toResponse(savedReview);
    }

    @Override
    public UnderwritingReviewResponse recommendDecline(
            Long applicationId,
            UnderwritingReviewRequest request,
            String authenticatedEmail
    ) {

        UnderwritingReview review =
                findReview(applicationId);

        requireInReview(review);

        applyAnalysis(review, request);

        validateRecommendationData(review);

        User underwriter =
                findUser(authenticatedEmail);

        review.setUnderwriterUser(underwriter);

        review.setStatus(
                UnderwritingReviewStatus.RECOMMEND_DECLINE
        );

        review.setCompletedAt(
                LocalDateTime.now()
        );

        review.touch();

        UnderwritingReview savedReview =
                underwritingReviewRepository.save(review);

        return mapper.toResponse(savedReview);
    }

    @Override
    public UnderwritingReviewResponse requestMoreInformation(
            Long applicationId,
            UnderwritingReviewRequest request,
            String authenticatedEmail
    ) {

        UnderwritingReview review =
                findReview(applicationId);

        requireInReview(review);

        applyAnalysis(review, request);

        User underwriter =
                findUser(authenticatedEmail);

        review.setUnderwriterUser(underwriter);

        review.setStatus(
                UnderwritingReviewStatus.NEEDS_MORE_INFORMATION
        );

        review.setCompletedAt(null);

        review.touch();

        UnderwritingReview savedReview =
                underwritingReviewRepository.save(review);

        return mapper.toResponse(savedReview);
    }

    private void applyAnalysis(
            UnderwritingReview review,
            UnderwritingReviewRequest request
    ) {

        review.setExistingDebt(
                request.existingDebt()
        );

        review.setMonthlyDebtService(
                request.monthlyDebtService()
        );

        review.setMonthlyCashFlow(
                request.monthlyCashFlow()
        );

        review.setDebtServiceCoverageRatio(
                calculateDscr(
                        request.monthlyCashFlow(),
                        request.monthlyDebtService()
                )
        );

        review.setRiskRating(
                request.riskRating()
        );

        review.setStrengths(
                request.strengths()
        );

        review.setWeaknesses(
                request.weaknesses()
        );

        review.setRiskFactors(
                request.riskFactors()
        );

        review.setMitigants(
                request.mitigants()
        );

        review.setFinancialAnalysis(
                request.financialAnalysis()
        );

        review.setRecommendationNotes(
                request.recommendationNotes()
        );
    }

    private BigDecimal calculateDscr(
            BigDecimal monthlyCashFlow,
            BigDecimal monthlyDebtService
    ) {

        if (monthlyCashFlow == null
                || monthlyDebtService == null) {

            return null;
        }

        if (monthlyDebtService.compareTo(
                BigDecimal.ZERO
        ) == 0) {

            return null;
        }

        return monthlyCashFlow.divide(
                monthlyDebtService,
                4,
                RoundingMode.HALF_UP
        );
    }

    private void validateRecommendationData(
            UnderwritingReview review
    ) {

        if (review.getRiskRating() == null) {
            throw new ResourceConflictException(
                    "Risk rating is required before completing an underwriting recommendation"
            );
        }

        if (review.getRecommendationNotes() == null
                || review.getRecommendationNotes().isBlank()) {

            throw new ResourceConflictException(
                    "Recommendation notes are required before completing an underwriting recommendation"
            );
        }
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

    private UnderwritingReview findReview(
            Long applicationId
    ) {

        return underwritingReviewRepository
                .findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Underwriting review not found for application id: "
                                        + applicationId
                        )
                );
    }

    private User findUser(
            String authenticatedEmail
    ) {

        return userRepository
                .findByEmailIgnoreCase(
                        authenticatedEmail
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    private void requireInReview(
            UnderwritingReview review
    ) {

        if (review.getStatus()
                != UnderwritingReviewStatus.IN_REVIEW) {

            throw new ResourceConflictException(
                    "Underwriting analysis can only be changed while the review is in progress"
            );
        }
    }
}
