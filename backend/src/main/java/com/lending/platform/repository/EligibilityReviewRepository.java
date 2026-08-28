package com.lending.platform.repository;

import com.lending.platform.entity.EligibilityReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EligibilityReviewRepository
        extends JpaRepository<EligibilityReview, Long> {

    Optional<EligibilityReview> findByApplicationId(
            Long applicationId
    );

    boolean existsByApplicationId(
            Long applicationId
    );
}
