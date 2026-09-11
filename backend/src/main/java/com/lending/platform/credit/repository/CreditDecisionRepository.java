package com.lending.platform.credit.repository;

import com.lending.platform.credit.entity.CreditDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditDecisionRepository
        extends JpaRepository<CreditDecision, Long> {

    Optional<CreditDecision> findByApplicationId(
            Long applicationId
    );

    boolean existsByApplicationId(
            Long applicationId
    );
}
