package com.lending.platform.repository;

import com.lending.platform.entity.ApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationStatusHistoryRepository
        extends JpaRepository<ApplicationStatusHistory, Long> {

    List<ApplicationStatusHistory>
    findAllByApplicationIdOrderByChangedAtAsc(Long applicationId);
}
