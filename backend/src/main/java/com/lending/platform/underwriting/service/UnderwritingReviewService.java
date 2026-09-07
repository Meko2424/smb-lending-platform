package com.lending.platform.underwriting.service;

import com.lending.platform.underwriting.dto.request.UnderwritingReviewRequest;
import com.lending.platform.underwriting.dto.response.UnderwritingReviewResponse;

public interface UnderwritingReviewService {

    UnderwritingReviewResponse createReview(
            Long applicationId
    );

    UnderwritingReviewResponse getReview(
            Long applicationId
    );

    UnderwritingReviewResponse startReview(
            Long applicationId,
            String authenticatedEmail
    );

    UnderwritingReviewResponse resumeReview(
            Long applicationId,
            String authenticatedEmail
    );

    UnderwritingReviewResponse updateAnalysis(
            Long applicationId,
            UnderwritingReviewRequest request
    );

    UnderwritingReviewResponse recommendApproval(
            Long applicationId,
            UnderwritingReviewRequest request,
            String authenticatedEmail
    );

    UnderwritingReviewResponse recommendDecline(
            Long applicationId,
            UnderwritingReviewRequest request,
            String authenticatedEmail
    );

    UnderwritingReviewResponse requestMoreInformation(
            Long applicationId,
            UnderwritingReviewRequest request,
            String authenticatedEmail
    );
}
