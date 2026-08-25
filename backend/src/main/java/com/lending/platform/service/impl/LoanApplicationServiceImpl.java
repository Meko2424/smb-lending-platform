package com.lending.platform.service.impl;

import com.lending.platform.dto.request.LoanApplicationRequest;
import com.lending.platform.dto.response.LoanApplicationResponse;
import com.lending.platform.entity.ApplicationStatus;
import com.lending.platform.entity.Business;
import com.lending.platform.entity.LoanApplication;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.mapper.LoanApplicationMapper;
import com.lending.platform.repository.BusinessRepository;
import com.lending.platform.repository.LoanApplicationRepository;
import com.lending.platform.service.LoanApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LoanApplicationServiceImpl
        implements LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final BusinessRepository businessRepository;
    private final LoanApplicationMapper loanApplicationMapper;

    public LoanApplicationServiceImpl(
            LoanApplicationRepository loanApplicationRepository,
            BusinessRepository businessRepository,
            LoanApplicationMapper loanApplicationMapper
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.businessRepository = businessRepository;
        this.loanApplicationMapper = loanApplicationMapper;
    }

    @Override
    public LoanApplicationResponse createApplication(
            LoanApplicationRequest request
    ) {

        Business business = findBusiness(request.businessId());

        LoanApplication application =
                loanApplicationMapper.toEntity(
                        request,
                        business
                );

        application.setApplicationNumber(
                generateApplicationNumber()
        );

        application.setStatus(
                ApplicationStatus.DRAFT
        );

        LoanApplication savedApplication =
                loanApplicationRepository.save(application);

        return loanApplicationMapper.toResponse(
                savedApplication
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationResponse getApplicationById(
            Long id
    ) {

        LoanApplication application =
                findApplication(id);

        return loanApplicationMapper.toResponse(
                application
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getAllApplications() {

        return loanApplicationRepository.findAll()
                .stream()
                .map(loanApplicationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getApplicationsByBusiness(
            Long businessId
    ) {

        findBusiness(businessId);

        return loanApplicationRepository
                .findAllByBusinessId(businessId)
                .stream()
                .map(loanApplicationMapper::toResponse)
                .toList();
    }

    @Override
    public LoanApplicationResponse updateApplication(
            Long id,
            LoanApplicationRequest request
    ) {

        LoanApplication application =
                findApplication(id);

        if (application.getStatus()
                != ApplicationStatus.DRAFT) {

            throw new ResourceConflictException(
                    "Only draft applications can be edited"
            );
        }

        Business business =
                findBusiness(request.businessId());

        loanApplicationMapper.updateEntity(
                application,
                request,
                business
        );

        LoanApplication updatedApplication =
                loanApplicationRepository.save(application);

        return loanApplicationMapper.toResponse(
                updatedApplication
        );
    }


    @Override
    public void deleteApplication(Long id) {

        LoanApplication application =
                findApplication(id);

        if (application.getStatus()
                != ApplicationStatus.DRAFT) {

            throw new ResourceConflictException(
                    "Only draft applications can be deleted"
            );
        }

        loanApplicationRepository.delete(application);
    }

    private LoanApplication findApplication(Long id) {

        return loanApplicationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan application not found with id: "
                                        + id
                        )
                );
    }

    private Business findBusiness(Long businessId) {

        return businessRepository.findById(businessId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Business not found with id: "
                                        + businessId
                        )
                );
    }

    private String generateApplicationNumber() {

        String year =
                String.valueOf(Year.now().getValue());

        String randomPart =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase();

        return "APP-" + year + "-" + randomPart;
    }
}