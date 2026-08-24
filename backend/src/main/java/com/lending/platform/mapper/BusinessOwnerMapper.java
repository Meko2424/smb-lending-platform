package com.lending.platform.mapper;

import com.lending.platform.dto.request.BusinessOwnerRequest;
import com.lending.platform.dto.response.BusinessOwnerResponse;
import com.lending.platform.entity.Business;
import com.lending.platform.entity.BusinessOwner;
import org.springframework.stereotype.Component;

@Component
public class BusinessOwnerMapper {

    public BusinessOwner toEntity(
            BusinessOwnerRequest request,
            Business business
    ) {
        BusinessOwner owner = new BusinessOwner();
        owner.setBusiness(business);
        updateEntity(owner, request);
        return owner;
    }

    public void updateEntity(
            BusinessOwner owner,
            BusinessOwnerRequest request
    ) {
        owner.setFirstName(request.firstName());
        owner.setLastName(request.lastName());
        owner.setTitle(request.title());
        owner.setOwnershipPercentage(request.ownershipPercentage());
        owner.setEmail(request.email());
        owner.setPhone(request.phone());
        owner.setGuarantor(request.guarantor());
    }

    public BusinessOwnerResponse toResponse(BusinessOwner owner) {
        return new BusinessOwnerResponse(
                owner.getId(),
                owner.getBusiness().getId(),
                owner.getFirstName(),
                owner.getLastName(),
                owner.getTitle(),
                owner.getOwnershipPercentage(),
                owner.getEmail(),
                owner.getPhone(),
                owner.isGuarantor(),
                owner.getCreatedAt(),
                owner.getUpdatedAt()
        );
    }
}
