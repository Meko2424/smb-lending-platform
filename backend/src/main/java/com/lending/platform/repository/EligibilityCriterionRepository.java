package com.lending.platform.repository;

import com.lending.platform.entity.EligibilityCriterion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EligibilityCriterionRepository
        extends JpaRepository<EligibilityCriterion, Long> {

    List<EligibilityCriterion>
    findAllByEligibilityReviewIdOrderByIdAsc(
            Long eligibilityReviewId
    );

    Optional<EligibilityCriterion>
    findByEligibilityReviewIdAndCriterionType(
            Long eligibilityReviewId,
            com.lending.platform.entity.EligibilityCriterionType criterionType
    );
}