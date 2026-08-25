package com.lending.platform.service;

import com.lending.platform.entity.ApplicationStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ApplicationWorkflowRules {

    private static final Map<ApplicationStatus, Set<ApplicationStatus>>
            ALLOWED_TRANSITIONS = Map.of(
            ApplicationStatus.DRAFT,
            Set.of(ApplicationStatus.SUBMITTED),

            ApplicationStatus.SUBMITTED,
            Set.of(ApplicationStatus.DOCUMENT_COLLECTION),

            ApplicationStatus.DOCUMENT_COLLECTION,
            Set.of(ApplicationStatus.ELIGIBILITY_REVIEW),

            ApplicationStatus.ELIGIBILITY_REVIEW,
            Set.of(ApplicationStatus.UNDERWRITING),

            ApplicationStatus.UNDERWRITING,
            Set.of(ApplicationStatus.CREDIT_REVIEW),

            ApplicationStatus.CREDIT_REVIEW,
            Set.of(
                    ApplicationStatus.APPROVED,
                    ApplicationStatus.DECLINED
            ),

            ApplicationStatus.APPROVED,
            Set.of(ApplicationStatus.CLOSING),

            ApplicationStatus.CLOSING,
            Set.of(ApplicationStatus.FUNDED)
    );

    public boolean isTransitionAllowed(
            ApplicationStatus currentStatus,
            ApplicationStatus targetStatus
    ) {

        return ALLOWED_TRANSITIONS
                .getOrDefault(
                        currentStatus,
                        Set.of()
                )
                .contains(targetStatus);
    }
}