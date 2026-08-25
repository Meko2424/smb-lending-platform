
package com.lending.platform.service.impl;

import com.lending.platform.dto.request.ApplicationAssignmentRequest;
import com.lending.platform.dto.response.LoanApplicationResponse;
import com.lending.platform.entity.LoanApplication;
import com.lending.platform.entity.User;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.mapper.LoanApplicationMapper;
import com.lending.platform.repository.LoanApplicationRepository;
import com.lending.platform.repository.UserRepository;
import com.lending.platform.service.ApplicationAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ApplicationAssignmentServiceImpl
        implements ApplicationAssignmentService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final LoanApplicationMapper loanApplicationMapper;

    public ApplicationAssignmentServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            UserRepository userRepository,
            LoanApplicationMapper loanApplicationMapper
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.userRepository = userRepository;
        this.loanApplicationMapper = loanApplicationMapper;
    }

    @Override
    public LoanApplicationResponse assignLoanOfficer(
            Long applicationId,
            ApplicationAssignmentRequest request
    ) {

        LoanApplication application =
                findApplication(applicationId);

        User user =
                findUser(request.userId());

        validateUserRole(
                user,
                "LOAN_OFFICER"
        );

        application.setAssignedLoanOfficer(user);

        LoanApplication updatedApplication =
                loanApplicationRepository.save(application);

        return loanApplicationMapper.toResponse(
                updatedApplication
        );
    }

    @Override
    public LoanApplicationResponse assignUnderwriter(
            Long applicationId,
            ApplicationAssignmentRequest request
    ) {

        LoanApplication application =
                findApplication(applicationId);

        User user =
                findUser(request.userId());

        validateUserRole(
                user,
                "UNDERWRITER"
        );

        application.setAssignedUnderwriter(user);

        LoanApplication updatedApplication =
                loanApplicationRepository.save(application);

        return loanApplicationMapper.toResponse(
                updatedApplication
        );
    }

    @Override
    public LoanApplicationResponse unassignLoanOfficer(
            Long applicationId
    ) {

        LoanApplication application =
                findApplication(applicationId);

        application.setAssignedLoanOfficer(null);

        LoanApplication updatedApplication =
                loanApplicationRepository.save(application);

        return loanApplicationMapper.toResponse(
                updatedApplication
        );
    }

    @Override
    public LoanApplicationResponse unassignUnderwriter(
            Long applicationId
    ) {

        LoanApplication application =
                findApplication(applicationId);

        application.setAssignedUnderwriter(null);

        LoanApplication updatedApplication =
                loanApplicationRepository.save(application);

        return loanApplicationMapper.toResponse(
                updatedApplication
        );
    }

    private LoanApplication findApplication(
            Long applicationId
    ) {

        return loanApplicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan application not found with id: "
                                        + applicationId
                        )
                );
    }

    private User findUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: "
                                        + userId
                        )
                );
    }

    private void validateUserRole(
            User user,
            String requiredRole
    ) {

        boolean hasRequiredRole =
                user.getRoles()
                        .stream()
                        .anyMatch(role ->
                                role.getName()
                                        .equals(requiredRole)
                                        || role.getName()
                                        .equals("ADMIN")
                        );

        if (!hasRequiredRole) {
            throw new ResourceConflictException(
                    "User does not have required role: "
                            + requiredRole
            );
        }

        if (!user.isEnabled()) {
            throw new ResourceConflictException(
                    "Disabled users cannot be assigned to applications"
            );
        }
    }
}