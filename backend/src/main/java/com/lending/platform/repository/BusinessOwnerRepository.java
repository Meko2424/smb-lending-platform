package com.lending.platform.repository;

import com.lending.platform.entity.BusinessOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessOwnerRepository
        extends JpaRepository<BusinessOwner, Long> {

    List<BusinessOwner> findAllByBusinessId(Long businessId);
}
