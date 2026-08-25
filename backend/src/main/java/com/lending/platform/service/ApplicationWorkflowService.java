package com.lending.platform.service;

import com.lending.platform.dto.request.ApplicationStatusTransitionRequest;
import com.lending.platform.dto.response.ApplicationStatusHistoryResponse;
import com.lending.platform.dto.response.LoanApplicationResponse;

import java.util.List;

public interface ApplicationWorkflowService {

    LoanApplicationResponse transitionStatus(
            Long applicationId,
            ApplicationStatusTransitionRequest request,
            String authenticatedEmail
    );

    List<ApplicationStatusHistoryResponse> getStatusHistory(
            Long applicationId
    );
}