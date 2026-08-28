package com.lending.platform.mapper;

import com.lending.platform.dto.eligibility.EligibilityCriterionResponse;
import com.lending.platform.dto.eligibility.EligibilityReviewResponse;
import com.lending.platform.entity.EligibilityCriterion;
import com.lending.platform.entity.EligibilityReview;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EligibilityReviewMapper {

    public EligibilityCriterionResponse toCriterionResponse(
            EligibilityCriterion criterion
    ) {
        return new EligibilityCriterionResponse(
                criterion.getId(),
                criterion.getCriterionType(),
                criterion.getStatus(),
                criterion.getNotes(),
                criterion.getCreatedAt(),
                criterion.getUpdatedAt()
        );
    }

    public EligibilityReviewResponse toResponse(
            EligibilityReview review,
            List<EligibilityCriterion> criteria
    ) {

        Long reviewedByUserId = null;
        String reviewedByUserName = null;

        if (review.getReviewedByUser() != null) {
            reviewedByUserId = review.getReviewedByUser().getId();
            reviewedByUserName =
                    review.getReviewedByUser().getFirstName()
                            + " "
                            + review.getReviewedByUser().getLastName();
        }

        List<EligibilityCriterionResponse> criterionResponses =
                criteria.stream()
                        .map(this::toCriterionResponse)
                        .toList();

        return new EligibilityReviewResponse(
                review.getId(),
                review.getApplication().getId(),
                review.getStatus(),
                reviewedByUserId,
                reviewedByUserName,
                review.getStartedAt(),
                review.getCompletedAt(),
                review.getSummary(),
                criterionResponses,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}