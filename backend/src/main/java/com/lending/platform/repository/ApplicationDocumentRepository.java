package com.lending.platform.repository;

import com.lending.platform.entity.ApplicationDocument;
import com.lending.platform.entity.DocumentStatus;
import com.lending.platform.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationDocumentRepository
        extends JpaRepository<ApplicationDocument, Long> {

    List<ApplicationDocument> findAllByApplicationIdOrderByCreatedAtAsc(
            Long applicationId
    );

    List<ApplicationDocument> findAllByApplicationIdAndStatus(
            Long applicationId,
            DocumentStatus status
    );

    boolean existsByApplicationIdAndDocumentType(
            Long applicationId,
            DocumentType documentType
    );
}
