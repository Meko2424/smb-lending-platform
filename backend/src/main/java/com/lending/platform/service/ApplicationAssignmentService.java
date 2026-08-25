package com.lending.platform.service;

import com.lending.platform.dto.request.ApplicationAssignmentRequest;
import com.lending.platform.dto.response.LoanApplicationResponse;

public interface ApplicationAssignmentService {

    LoanApplicationResponse assignLoanOfficer(
            Long applicationId,
            ApplicationAssignmentRequest request
    );

    LoanApplicationResponse assignUnderwriter(
            Long applicationId,
            ApplicationAssignmentRequest request
    );

    LoanApplicationResponse unassignLoanOfficer(
            Long applicationId
    );

    LoanApplicationResponse unassignUnderwriter(
            Long applicationId
    );
}
