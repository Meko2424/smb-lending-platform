package com.lending.platform.credit.service.impl;

import com.lending.platform.credit.dto.request.CreditApprovalRequest;
import com.lending.platform.credit.dto.request.CreditDeclineRequest;
import com.lending.platform.credit.dto.response.CreditDecisionResponse;
import com.lending.platform.credit.entity.CreditDecision;
import com.lending.platform.credit.entity.CreditDecisionStatus;
import com.lending.platform.credit.entity.CreditDecisionType;
import com.lending.platform.credit.mapper.CreditDecisionMapper;
import com.lending.platform.credit.repository.CreditDecisionRepository;
import com.lending.platform.credit.service.CreditDecisionService;
import com.lending.platform.entity.LoanApplication;
import com.lending.platform.entity.User;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.repository.LoanApplicationRepository;
import com.lending.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class CreditDecisionServiceImpl
        implements CreditDecisionService {

    private final CreditDecisionRepository creditDecisionRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final CreditDecisionMapper mapper;

    public CreditDecisionServiceImpl(
            CreditDecisionRepository creditDecisionRepository,
            LoanApplicationRepository loanApplicationRepository,
            UserRepository userRepository,
            CreditDecisionMapper mapper
    ) {
        this.creditDecisionRepository = creditDecisionRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public CreditDecisionResponse createDecision(
            Long applicationId
    ) {

        LoanApplication application =
                findApplication(applicationId);

        if (creditDecisionRepository
                .existsByApplicationId(applicationId)) {

            throw new ResourceConflictException(
                    "Credit decision already exists for this application"
            );
        }

        CreditDecision decision =
                new CreditDecision();

        decision.setApplication(application);
        decision.setStatus(
                CreditDecisionStatus.PENDING
        );

        decision.setRequestedAmount(
                application.getRequestedAmount()
        );

        decision.setRequestedTermMonths(
                application.getRequestedTermMonths()
        );

        CreditDecision savedDecision =
                creditDecisionRepository.save(decision);

        return mapper.toResponse(savedDecision);
    }

    @Override
    @Transactional(readOnly = true)
    public CreditDecisionResponse getDecision(
            Long applicationId
    ) {

        return mapper.toResponse(
                findDecision(applicationId)
        );
    }

    @Override
    public CreditDecisionResponse startReview(
            Long applicationId,
            String authenticatedEmail
    ) {

        CreditDecision decision =
                findDecision(applicationId);

        if (decision.getStatus()
                != CreditDecisionStatus.PENDING) {

            throw new ResourceConflictException(
                    "Only pending credit decisions can be started"
            );
        }

        User creditManager =
                findUser(authenticatedEmail);

        decision.setStatus(
                CreditDecisionStatus.IN_REVIEW
        );

        decision.setCreditManagerUser(
                creditManager
        );

        decision.setStartedAt(
                LocalDateTime.now()
        );

        decision.touch();

        CreditDecision savedDecision =
                creditDecisionRepository.save(decision);

        return mapper.toResponse(savedDecision);
    }

    @Override
    public CreditDecisionResponse approve(
            Long applicationId,
            CreditApprovalRequest request,
            String authenticatedEmail
    ) {

        CreditDecision decision =
                findDecision(applicationId);

        requireInReview(decision);

        User creditManager =
                findUser(authenticatedEmail);

        decision.setApprovedAmount(
                request.approvedAmount()
        );

        decision.setApprovedTermMonths(
                request.approvedTermMonths()
        );

        decision.setInterestRate(
                request.interestRate()
        );

        decision.setConditions(
                request.conditions()
        );

        decision.setDecisionNotes(
                request.decisionNotes()
        );

        decision.setDeclineReason(null);

        CreditDecisionType decisionType =
                isApprovalAsRequested(
                        decision,
                        request
                )
                        ? CreditDecisionType.APPROVE_AS_REQUESTED
                        : CreditDecisionType.APPROVE_WITH_CHANGES;

        decision.setDecisionType(
                decisionType
        );

        decision.setStatus(
                CreditDecisionStatus.APPROVED
        );

        decision.setCreditManagerUser(
                creditManager
        );

        decision.setDecidedAt(
                LocalDateTime.now()
        );

        decision.touch();

        CreditDecision savedDecision =
                creditDecisionRepository.save(decision);

        return mapper.toResponse(savedDecision);
    }

    @Override
    public CreditDecisionResponse decline(
            Long applicationId,
            CreditDeclineRequest request,
            String authenticatedEmail
    ) {

        CreditDecision decision =
                findDecision(applicationId);

        requireInReview(decision);

        User creditManager =
                findUser(authenticatedEmail);

        decision.setStatus(
                CreditDecisionStatus.DECLINED
        );

        decision.setDecisionType(
                CreditDecisionType.DECLINE
        );

        decision.setCreditManagerUser(
                creditManager
        );

        decision.setDeclineReason(
                request.declineReason()
        );

        decision.setDecisionNotes(
                request.decisionNotes()
        );

        decision.setApprovedAmount(null);
        decision.setApprovedTermMonths(null);
        decision.setInterestRate(null);
        decision.setConditions(null);

        decision.setDecidedAt(
                LocalDateTime.now()
        );

        decision.touch();

        CreditDecision savedDecision =
                creditDecisionRepository.save(decision);

        return mapper.toResponse(savedDecision);
    }

    private boolean isApprovalAsRequested(
            CreditDecision decision,
            CreditApprovalRequest request
    ) {

        boolean sameAmount =
                decision.getRequestedAmount() != null
                        && decision.getRequestedAmount()
                        .compareTo(
                                request.approvedAmount()
                        ) == 0;

        boolean sameTerm =
                decision.getRequestedTermMonths() != null
                        && decision.getRequestedTermMonths()
                        .equals(
                                request.approvedTermMonths()
                        );

        return sameAmount && sameTerm;
    }

    private LoanApplication findApplication(
            Long applicationId
    ) {

        return loanApplicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan application not found with id: "
                                        + applicationId
                        )
                );
    }

    private CreditDecision findDecision(
            Long applicationId
    ) {

        return creditDecisionRepository
                .findByApplicationId(applicationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Credit decision not found for application id: "
                                        + applicationId
                        )
                );
    }

    private User findUser(
            String authenticatedEmail
    ) {

        return userRepository
                .findByEmailIgnoreCase(
                        authenticatedEmail
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    private void requireInReview(
            CreditDecision decision
    ) {

        if (decision.getStatus()
                != CreditDecisionStatus.IN_REVIEW) {

            throw new ResourceConflictException(
                    "Credit decision can only be completed while the review is in progress"
            );
        }
    }
}
