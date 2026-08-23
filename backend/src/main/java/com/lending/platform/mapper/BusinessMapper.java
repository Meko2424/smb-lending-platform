package com.lending.platform.mapper;

import com.lending.platform.dto.request.BusinessRequest;
import com.lending.platform.dto.response.BusinessResponse;
import com.lending.platform.entity.Business;
import org.springframework.stereotype.Component;

@Component
public class BusinessMapper {

    public Business toEntity(BusinessRequest request) {
        Business business = new Business();
        updateEntity(business, request);
        return business;
    }

    public void updateEntity(Business business, BusinessRequest request) {
        business.setLegalName(request.legalName());
        business.setDbaName(request.dbaName());
        business.setEin(normalizeEin(request.ein()));
        business.setBusinessType(request.businessType());
        business.setIndustry(request.industry());
        business.setNaicsCode(request.naicsCode());
        business.setEstablishedDate(request.establishedDate());
        business.setPhone(request.phone());
        business.setEmail(request.email());
        business.setWebsite(request.website());
        business.setAddressLine1(request.addressLine1());
        business.setAddressLine2(request.addressLine2());
        business.setCity(request.city());
        business.setState(request.state());
        business.setPostalCode(request.postalCode());
        business.setAnnualRevenue(request.annualRevenue());
        business.setEmployeeCount(request.employeeCount());
    }

    public BusinessResponse toResponse(Business business) {
        return new BusinessResponse(
                business.getId(),
                business.getLegalName(),
                business.getDbaName(),
                business.getEin(),
                business.getBusinessType(),
                business.getIndustry(),
                business.getNaicsCode(),
                business.getEstablishedDate(),
                business.getPhone(),
                business.getEmail(),
                business.getWebsite(),
                business.getAddressLine1(),
                business.getAddressLine2(),
                business.getCity(),
                business.getState(),
                business.getPostalCode(),
                business.getAnnualRevenue(),
                business.getEmployeeCount(),
                business.getCreatedAt(),
                business.getUpdatedAt()
        );
    }

    public String normalizeEin(String ein) {
        return ein == null ? null : ein.replace("-", "");
    }
}
