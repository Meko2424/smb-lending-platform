package com.lending.platform.underwriting.repository;

import com.lending.platform.underwriting.entity.UnderwritingReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UnderwritingReviewRepository
        extends JpaRepository<UnderwritingReview, Long> {

    Optional<UnderwritingReview> findByApplicationId(Long applicationId);

    boolean existsByApplicationId(Long applicationId);
}
