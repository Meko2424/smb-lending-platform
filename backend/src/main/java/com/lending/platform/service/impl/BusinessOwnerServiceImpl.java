package com.lending.platform.service.impl;

import com.lending.platform.dto.request.BusinessOwnerRequest;
import com.lending.platform.dto.response.BusinessOwnerResponse;
import com.lending.platform.entity.Business;
import com.lending.platform.entity.BusinessOwner;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.mapper.BusinessOwnerMapper;
import com.lending.platform.repository.BusinessOwnerRepository;
import com.lending.platform.repository.BusinessRepository;
import com.lending.platform.service.BusinessOwnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class BusinessOwnerServiceImpl implements BusinessOwnerService {

    private final BusinessRepository businessRepository;
    private final BusinessOwnerRepository businessOwnerRepository;
    private final BusinessOwnerMapper businessOwnerMapper;

    public BusinessOwnerServiceImpl(
            BusinessRepository businessRepository,
            BusinessOwnerRepository businessOwnerRepository,
            BusinessOwnerMapper businessOwnerMapper
    ) {
        this.businessRepository = businessRepository;
        this.businessOwnerRepository = businessOwnerRepository;
        this.businessOwnerMapper = businessOwnerMapper;
    }

    @Override
    public BusinessOwnerResponse createOwner(
            Long businessId,
            BusinessOwnerRequest request
    ) {

        Business business = findBusiness(businessId);

        validateOwnershipTotal(
                businessId,
                null,
                request.ownershipPercentage()
        );

        BusinessOwner owner =
                businessOwnerMapper.toEntity(request, business);

        BusinessOwner savedOwner =
                businessOwnerRepository.save(owner);

        return businessOwnerMapper.toResponse(savedOwner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessOwnerResponse> getOwnersByBusiness(
            Long businessId
    ) {

        findBusiness(businessId);

        return businessOwnerRepository
                .findAllByBusinessId(businessId)
                .stream()
                .map(businessOwnerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessOwnerResponse getOwnerById(
            Long businessId,
            Long ownerId
    ) {

        BusinessOwner owner =
                findOwnerForBusiness(businessId, ownerId);

        return businessOwnerMapper.toResponse(owner);
    }

    @Override
    public BusinessOwnerResponse updateOwner(
            Long businessId,
            Long ownerId,
            BusinessOwnerRequest request
    ) {

        findBusiness(businessId);

        BusinessOwner owner =
                findOwnerForBusiness(businessId, ownerId);

        validateOwnershipTotal(
                businessId,
                ownerId,
                request.ownershipPercentage()
        );

        businessOwnerMapper.updateEntity(owner, request);

        BusinessOwner updatedOwner =
                businessOwnerRepository.save(owner);

        return businessOwnerMapper.toResponse(updatedOwner);
    }

    @Override
    public void deleteOwner(
            Long businessId,
            Long ownerId
    ) {

        BusinessOwner owner =
                findOwnerForBusiness(businessId, ownerId);

        businessOwnerRepository.delete(owner);
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

    private BusinessOwner findOwnerForBusiness(
            Long businessId,
            Long ownerId
    ) {

        BusinessOwner owner =
                businessOwnerRepository.findById(ownerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Business owner not found with id: "
                                                + ownerId
                                )
                        );

        if (!owner.getBusiness().getId().equals(businessId)) {
            throw new ResourceNotFoundException(
                    "Business owner not found with id: "
                            + ownerId
                            + " for business id: "
                            + businessId
            );
        }

        return owner;
    }

    private void validateOwnershipTotal(
            Long businessId,
            Long ownerIdToExclude,
            BigDecimal requestedPercentage
    ) {

        BigDecimal existingTotal =
                businessOwnerRepository
                        .findAllByBusinessId(businessId)
                        .stream()
                        .filter(owner ->
                                ownerIdToExclude == null
                                        || !owner.getId()
                                        .equals(ownerIdToExclude)
                        )
                        .map(BusinessOwner::getOwnershipPercentage)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal newTotal =
                existingTotal.add(requestedPercentage);

        if (newTotal.compareTo(
                new BigDecimal("100.00")
        ) > 0) {

            throw new ResourceConflictException(
                    "Combined ownership percentage cannot exceed 100%"
            );
        }
    }
}
