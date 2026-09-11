package com.lending.platform.credit.service;


import com.lending.platform.credit.dto.request.CreditApprovalRequest;
import com.lending.platform.credit.dto.request.CreditDeclineRequest;
import com.lending.platform.credit.dto.response.CreditDecisionResponse;

public interface CreditDecisionService {

    CreditDecisionResponse createDecision(
            Long applicationId
    );

    CreditDecisionResponse getDecision(
            Long applicationId
    );

    CreditDecisionResponse startReview(
            Long applicationId,
            String authenticatedEmail
    );

    CreditDecisionResponse approve(
            Long applicationId,
            CreditApprovalRequest request,
            String authenticatedEmail
    );

    CreditDecisionResponse decline(
            Long applicationId,
            CreditDeclineRequest request,
            String authenticatedEmail
    );
}
