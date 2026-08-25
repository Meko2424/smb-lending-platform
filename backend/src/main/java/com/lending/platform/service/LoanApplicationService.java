package com.lending.platform.service;

import com.lending.platform.dto.request.LoanApplicationRequest;
import com.lending.platform.dto.response.LoanApplicationResponse;

import java.util.List;

public interface LoanApplicationService {

    LoanApplicationResponse createApplication(
            LoanApplicationRequest request
    );

    LoanApplicationResponse getApplicationById(
            Long id
    );

    List<LoanApplicationResponse> getAllApplications();

    List<LoanApplicationResponse> getApplicationsByBusiness(
            Long businessId
    );

    LoanApplicationResponse updateApplication(
            Long id,
            LoanApplicationRequest request
    );


    void deleteApplication(Long id);
}
