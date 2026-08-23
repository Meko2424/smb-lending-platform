package com.lending.platform.service.impl;

import com.lending.platform.dto.request.BusinessRequest;
import com.lending.platform.dto.response.BusinessResponse;
import com.lending.platform.entity.Business;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.mapper.BusinessMapper;
import com.lending.platform.repository.BusinessRepository;
import com.lending.platform.service.BusinessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;

    public BusinessServiceImpl(
            BusinessRepository businessRepository,
            BusinessMapper businessMapper
    ) {
        this.businessRepository = businessRepository;
        this.businessMapper = businessMapper;
    }

    @Override
    public BusinessResponse createBusiness(BusinessRequest request) {

        String normalizedEin = businessMapper.normalizeEin(request.ein());

        if (businessRepository.existsByEin(normalizedEin)) {
            throw new ResourceConflictException(
                    "A business with this EIN already exists"
            );
        }

        Business business = businessMapper.toEntity(request);

        Business savedBusiness = businessRepository.save(business);

        return businessMapper.toResponse(savedBusiness);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessResponse getBusinessById(Long id) {

        Business business = findBusiness(id);

        return businessMapper.toResponse(business);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessResponse> getAllBusinesses() {

        return businessRepository.findAll()
                .stream()
                .map(businessMapper::toResponse)
                .toList();
    }

    @Override
    public BusinessResponse updateBusiness(
            Long id,
            BusinessRequest request
    ) {

        Business business = findBusiness(id);

        String normalizedEin = businessMapper.normalizeEin(request.ein());

        businessRepository.findByEin(normalizedEin)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResourceConflictException(
                            "A business with this EIN already exists"
                    );
                });

        businessMapper.updateEntity(business, request);

        Business updatedBusiness = businessRepository.save(business);

        return businessMapper.toResponse(updatedBusiness);
    }

    @Override
    public void deleteBusiness(Long id) {

        Business business = findBusiness(id);

        businessRepository.delete(business);
    }

    private Business findBusiness(Long id) {

        return businessRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Business not found with id: " + id
                        )
                );
    }
}