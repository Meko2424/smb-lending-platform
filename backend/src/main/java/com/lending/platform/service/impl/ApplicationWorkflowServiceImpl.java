package com.lending.platform.service.impl;

import com.lending.platform.dto.request.ApplicationStatusTransitionRequest;
import com.lending.platform.dto.response.ApplicationStatusHistoryResponse;
import com.lending.platform.dto.response.LoanApplicationResponse;
import com.lending.platform.entity.ApplicationStatus;
import com.lending.platform.entity.ApplicationStatusHistory;
import com.lending.platform.entity.LoanApplication;
import com.lending.platform.entity.User;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.mapper.ApplicationStatusHistoryMapper;
import com.lending.platform.mapper.LoanApplicationMapper;
import com.lending.platform.repository.ApplicationStatusHistoryRepository;
import com.lending.platform.repository.LoanApplicationRepository;
import com.lending.platform.repository.UserRepository;
import com.lending.platform.service.ApplicationWorkflowRules;
import com.lending.platform.service.ApplicationWorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ApplicationWorkflowServiceImpl
        implements ApplicationWorkflowService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final ApplicationWorkflowRules workflowRules;
    private final LoanApplicationMapper loanApplicationMapper;
    private final ApplicationStatusHistoryMapper historyMapper;

    public ApplicationWorkflowServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            ApplicationStatusHistoryRepository historyRepository,
            UserRepository userRepository,
            ApplicationWorkflowRules workflowRules,
            LoanApplicationMapper loanApplicationMapper,
            ApplicationStatusHistoryMapper historyMapper
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.workflowRules = workflowRules;
        this.loanApplicationMapper = loanApplicationMapper;
        this.historyMapper = historyMapper;
    }

    @Override
    public LoanApplicationResponse transitionStatus(
            Long applicationId,
            ApplicationStatusTransitionRequest request,
            String authenticatedEmail
    ) {

        LoanApplication application =
                loanApplicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan application not found with id: "
                                                + applicationId
                                )
                        );

        User user =
                userRepository.findByEmailIgnoreCase(authenticatedEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Authenticated user not found"
                                )
                        );

        ApplicationStatus currentStatus =
                application.getStatus();

        ApplicationStatus targetStatus =
                request.targetStatus();

        if (!workflowRules.isTransitionAllowed(
                currentStatus,
                targetStatus
        )) {
            throw new ResourceConflictException(
                    "Invalid application status transition from "
                            + currentStatus
                            + " to "
                            + targetStatus
            );
        }

        application.setStatus(targetStatus);

        applyLifecycleTimestamp(
                application,
                targetStatus
        );

        LoanApplication updatedApplication =
                loanApplicationRepository.save(application);

        ApplicationStatusHistory history =
                new ApplicationStatusHistory();

        history.setApplication(updatedApplication);
        history.setFromStatus(currentStatus);
        history.setToStatus(targetStatus);
        history.setChangedByUser(user);
        history.setComment(request.comment());

        historyRepository.save(history);

        return loanApplicationMapper.toResponse(
                updatedApplication
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationStatusHistoryResponse> getStatusHistory(
            Long applicationId
    ) {

        if (!loanApplicationRepository.existsById(applicationId)) {
            throw new ResourceNotFoundException(
                    "Loan application not found with id: "
                            + applicationId
            );
        }

        return historyRepository
                .findAllByApplicationIdOrderByChangedAtAsc(
                        applicationId
                )
                .stream()
                .map(historyMapper::toResponse)
                .toList();
    }

    private void applyLifecycleTimestamp(
            LoanApplication application,
            ApplicationStatus targetStatus
    ) {

        LocalDateTime now = LocalDateTime.now();

        if (targetStatus == ApplicationStatus.SUBMITTED
                && application.getSubmittedAt() == null) {
            application.setSubmittedAt(now);
        }

        if (targetStatus == ApplicationStatus.APPROVED
                || targetStatus == ApplicationStatus.DECLINED) {
            application.setDecisionAt(now);
        }

        if (targetStatus == ApplicationStatus.FUNDED) {
            application.setFundedAt(now);
        }
    }
}
