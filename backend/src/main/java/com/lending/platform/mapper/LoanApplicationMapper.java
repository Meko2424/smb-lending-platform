package com.lending.platform.mapper;

import com.lending.platform.dto.request.LoanApplicationRequest;
import com.lending.platform.dto.response.LoanApplicationResponse;
import com.lending.platform.entity.Business;
import com.lending.platform.entity.LoanApplication;
import com.lending.platform.entity.User;
import org.springframework.stereotype.Component;

@Component
public class LoanApplicationMapper {

    public LoanApplication toEntity(
            LoanApplicationRequest request,
            Business business
    ) {
        LoanApplication application = new LoanApplication();

        application.setBusiness(business);
        application.setLoanProduct(request.loanProduct());
        application.setLoanPurpose(request.loanPurpose());
        application.setRequestedAmount(request.requestedAmount());
        application.setRequestedTermMonths(request.requestedTermMonths());

        return application;
    }

    public void updateEntity(
            LoanApplication application,
            LoanApplicationRequest request,
            Business business
    ) {
        application.setBusiness(business);
        application.setLoanProduct(request.loanProduct());
        application.setLoanPurpose(request.loanPurpose());
        application.setRequestedAmount(request.requestedAmount());
        application.setRequestedTermMonths(request.requestedTermMonths());
    }

    public LoanApplicationResponse toResponse(
            LoanApplication application
    ) {
        User loanOfficer = application.getAssignedLoanOfficer();
        User underwriter = application.getAssignedUnderwriter();

        return new LoanApplicationResponse(
                application.getId(),
                application.getBusiness().getId(),
                application.getBusiness().getLegalName(),
                application.getApplicationNumber(),
                application.getLoanProduct(),
                application.getLoanPurpose(),
                application.getRequestedAmount(),
                application.getRequestedTermMonths(),
                application.getStatus(),

                loanOfficer != null
                        ? loanOfficer.getId()
                        : null,

                loanOfficer != null
                        ? loanOfficer.getFirstName()
                          + " "
                          + loanOfficer.getLastName()
                        : null,

                underwriter != null
                        ? underwriter.getId()
                        : null,

                underwriter != null
                        ? underwriter.getFirstName()
                          + " "
                          + underwriter.getLastName()
                        : null,

                application.getSubmittedAt(),
                application.getDecisionAt(),
                application.getFundedAt(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}