package com.lending.platform.repository;

import com.lending.platform.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanApplicationRepository
        extends JpaRepository<LoanApplication, Long> {

    Optional<LoanApplication> findByApplicationNumber(
            String applicationNumber
    );

    boolean existsByApplicationNumber(
            String applicationNumber
    );

    List<LoanApplication> findAllByBusinessId(
            Long businessId
    );
}