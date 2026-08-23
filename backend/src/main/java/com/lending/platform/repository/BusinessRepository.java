package com.lending.platform.repository;

import com.lending.platform.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByEin(String ein);

    boolean existsByEin(String ein);
}
