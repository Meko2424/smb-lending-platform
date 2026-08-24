package com.lending.platform.service;

import com.lending.platform.dto.request.BusinessOwnerRequest;
import com.lending.platform.dto.response.BusinessOwnerResponse;

import java.util.List;

public interface BusinessOwnerService {

    BusinessOwnerResponse createOwner(
            Long businessId,
            BusinessOwnerRequest request
    );

    List<BusinessOwnerResponse> getOwnersByBusiness(Long businessId);

    BusinessOwnerResponse getOwnerById(
            Long businessId,
            Long ownerId
    );

    BusinessOwnerResponse updateOwner(
            Long businessId,
            Long ownerId,
            BusinessOwnerRequest request
    );

    void deleteOwner(
            Long businessId,
            Long ownerId
    );
}
