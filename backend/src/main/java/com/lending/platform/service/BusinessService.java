package com.lending.platform.service;

import com.lending.platform.dto.request.BusinessRequest;
import com.lending.platform.dto.response.BusinessResponse;

import java.util.List;

public interface BusinessService {

    BusinessResponse createBusiness(BusinessRequest request);

    BusinessResponse getBusinessById(Long id);

    List<BusinessResponse> getAllBusinesses();

    BusinessResponse updateBusiness(Long id, BusinessRequest request);

    void deleteBusiness(Long id);
}
