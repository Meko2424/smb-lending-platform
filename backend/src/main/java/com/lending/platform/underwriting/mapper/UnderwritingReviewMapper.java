package com.lending.platform.underwriting.mapper;

import com.lending.platform.entity.User;
import com.lending.platform.underwriting.dto.response.UnderwritingReviewResponse;
import com.lending.platform.underwriting.entity.UnderwritingReview;
import org.springframework.stereotype.Component;

@Component
public class UnderwritingReviewMapper {

    public UnderwritingReviewResponse toResponse(
            UnderwritingReview review
    ) {

        User underwriter = review.getUnderwriterUser();

        Long underwriterUserId = null;
        String underwriterUserName = null;

        if (underwriter != null) {
            underwriterUserId = underwriter.getId();

            underwriterUserName =
                    underwriter.getFirstName()
                            + " "
                            + underwriter.getLastName();
        }

        return new UnderwritingReviewResponse(
                review.getId(),
                review.getApplication().getId(),
                review.getApplication().getApplicationNumber(),
                review.getStatus(),
                underwriterUserId,
                underwriterUserName,
                review.getRequestedAmount(),
                review.getAnnualRevenue(),
                review.getExistingDebt(),
                review.getMonthlyDebtService(),
                review.getMonthlyCashFlow(),
                review.getDebtServiceCoverageRatio(),
                review.getRiskRating(),
                review.getStrengths(),
                review.getWeaknesses(),
                review.getRiskFactors(),
                review.getMitigants(),
                review.getFinancialAnalysis(),
                review.getRecommendationNotes(),
                review.getStartedAt(),
                review.getCompletedAt(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
