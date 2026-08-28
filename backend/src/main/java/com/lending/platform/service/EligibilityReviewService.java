package com.lending.platform.service;

import com.lending.platform.dto.eligibility.EligibilityCriterionRequest;
import com.lending.platform.dto.eligibility.EligibilityReviewRequest;
import com.lending.platform.dto.eligibility.EligibilityReviewResponse;
import com.lending.platform.entity.EligibilityCriterionType;

public interface EligibilityReviewService {

    EligibilityReviewResponse createReview(
            Long applicationId,
            EligibilityReviewRequest request
    );

    EligibilityReviewResponse getReview(
            Long applicationId
    );

    EligibilityReviewResponse startReview(
            Long applicationId,
            String authenticatedEmail
    );

    EligibilityReviewResponse updateCriterion(
            Long applicationId,
            EligibilityCriterionType criterionType,
            EligibilityCriterionRequest request
    );

    EligibilityReviewResponse completeAsEligible(
            Long applicationId,
            String authenticatedEmail
    );

    EligibilityReviewResponse completeAsIneligible(
            Long applicationId,
            EligibilityReviewRequest request,
            String authenticatedEmail
    );
}

