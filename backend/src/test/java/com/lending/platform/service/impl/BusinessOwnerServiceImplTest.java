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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessOwnerServiceImplTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private BusinessOwnerRepository businessOwnerRepository;

    private BusinessOwnerMapper businessOwnerMapper;
    private BusinessOwnerServiceImpl businessOwnerService;

    @BeforeEach
    void setUp() {
        businessOwnerMapper = new BusinessOwnerMapper();

        businessOwnerService = new BusinessOwnerServiceImpl(
                businessRepository,
                businessOwnerRepository,
                businessOwnerMapper
        );
    }

    @Test
    void createOwner_shouldCreateOwner() {

        Business business = createBusiness(1L);

        BusinessOwnerRequest request =
                createRequest("60.00");

        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));

        when(businessOwnerRepository.findAllByBusinessId(1L))
                .thenReturn(List.of());

        when(businessOwnerRepository.save(any(BusinessOwner.class)))
                .thenAnswer(invocation -> {
                    BusinessOwner owner =
                            invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            owner,
                            "id",
                            10L
                    );

                    owner.onCreate();

                    return owner;
                });

        BusinessOwnerResponse response =
                businessOwnerService.createOwner(
                        1L,
                        request
                );

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals(1L, response.businessId());
        assertEquals("Daniel", response.firstName());
        assertEquals("Carter", response.lastName());
        assertEquals(
                new BigDecimal("60.00"),
                response.ownershipPercentage()
        );
        assertTrue(response.guarantor());

        verify(businessOwnerRepository)
                .save(any(BusinessOwner.class));
    }

    @Test
    void createOwner_shouldThrowWhenBusinessDoesNotExist() {

        when(businessRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> businessOwnerService.createOwner(
                        999L,
                        createRequest("60.00")
                )
        );

        verify(businessOwnerRepository, never())
                .save(any());
    }

    @Test
    void createOwner_shouldThrowWhenOwnershipExceeds100() {

        Business business = createBusiness(1L);

        BusinessOwner existingOwner =
                createOwner(
                        10L,
                        business,
                        "70.00"
                );

        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));

        when(businessOwnerRepository.findAllByBusinessId(1L))
                .thenReturn(List.of(existingOwner));

        assertThrows(
                ResourceConflictException.class,
                () -> businessOwnerService.createOwner(
                        1L,
                        createRequest("40.00")
                )
        );

        verify(businessOwnerRepository, never())
                .save(any());
    }

    @Test
    void getOwnerById_shouldReturnOwner() {

        Business business = createBusiness(1L);

        BusinessOwner owner =
                createOwner(
                        10L,
                        business,
                        "60.00"
                );

        when(businessOwnerRepository.findById(10L))
                .thenReturn(Optional.of(owner));

        BusinessOwnerResponse response =
                businessOwnerService.getOwnerById(
                        1L,
                        10L
                );

        assertEquals(10L, response.id());
        assertEquals(1L, response.businessId());
        assertEquals("Daniel", response.firstName());
    }

    @Test
    void getOwnerById_shouldThrowWhenOwnerBelongsToDifferentBusiness() {

        Business differentBusiness =
                createBusiness(2L);

        BusinessOwner owner =
                createOwner(
                        10L,
                        differentBusiness,
                        "60.00"
                );

        when(businessOwnerRepository.findById(10L))
                .thenReturn(Optional.of(owner));

        assertThrows(
                ResourceNotFoundException.class,
                () -> businessOwnerService.getOwnerById(
                        1L,
                        10L
                )
        );
    }

    @Test
    void updateOwner_shouldRecalculateOwnershipExcludingCurrentOwner() {

        Business business = createBusiness(1L);

        BusinessOwner ownerOne =
                createOwner(
                        10L,
                        business,
                        "60.00"
                );

        BusinessOwner ownerTwo =
                createOwner(
                        11L,
                        business,
                        "40.00"
                );

        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));

        when(businessOwnerRepository.findById(10L))
                .thenReturn(Optional.of(ownerOne));

        when(businessOwnerRepository.findAllByBusinessId(1L))
                .thenReturn(List.of(
                        ownerOne,
                        ownerTwo
                ));

        when(businessOwnerRepository.save(ownerOne))
                .thenReturn(ownerOne);

        BusinessOwnerResponse response =
                businessOwnerService.updateOwner(
                        1L,
                        10L,
                        createRequest("55.00")
                );

        assertEquals(
                new BigDecimal("55.00"),
                response.ownershipPercentage()
        );

        verify(businessOwnerRepository)
                .save(ownerOne);
    }

    @Test
    void updateOwner_shouldThrowWhenUpdatedTotalExceeds100() {

        Business business = createBusiness(1L);

        BusinessOwner ownerOne =
                createOwner(
                        10L,
                        business,
                        "60.00"
                );

        BusinessOwner ownerTwo =
                createOwner(
                        11L,
                        business,
                        "40.00"
                );

        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));

        when(businessOwnerRepository.findById(10L))
                .thenReturn(Optional.of(ownerOne));

        when(businessOwnerRepository.findAllByBusinessId(1L))
                .thenReturn(List.of(
                        ownerOne,
                        ownerTwo
                ));

        assertThrows(
                ResourceConflictException.class,
                () -> businessOwnerService.updateOwner(
                        1L,
                        10L,
                        createRequest("70.00")
                )
        );

        verify(businessOwnerRepository, never())
                .save(any());
    }

    @Test
    void deleteOwner_shouldDeleteExistingOwner() {

        Business business = createBusiness(1L);

        BusinessOwner owner =
                createOwner(
                        10L,
                        business,
                        "60.00"
                );

        when(businessOwnerRepository.findById(10L))
                .thenReturn(Optional.of(owner));

        businessOwnerService.deleteOwner(
                1L,
                10L
        );

        verify(businessOwnerRepository)
                .delete(owner);
    }

    private Business createBusiness(Long id) {

        Business business = new Business();

        ReflectionTestUtils.setField(
                business,
                "id",
                id
        );

        business.setLegalName(
                "Atlanta Logistics LLC"
        );

        return business;
    }

    private BusinessOwner createOwner(
            Long ownerId,
            Business business,
            String percentage
    ) {

        BusinessOwner owner = new BusinessOwner();

        ReflectionTestUtils.setField(
                owner,
                "id",
                ownerId
        );

        owner.setBusiness(business);
        owner.setFirstName("Daniel");
        owner.setLastName("Carter");
        owner.setTitle("President");
        owner.setOwnershipPercentage(
                new BigDecimal(percentage)
        );
        owner.setEmail(
                "daniel@atlantalogistics.com"
        );
        owner.setPhone(
                "404-555-0101"
        );
        owner.setGuarantor(true);

        owner.onCreate();

        return owner;
    }

    private BusinessOwnerRequest createRequest(
            String percentage
    ) {

        return new BusinessOwnerRequest(
                "Daniel",
                "Carter",
                "President",
                new BigDecimal(percentage),
                "daniel@atlantalogistics.com",
                "404-555-0101",
                true
        );
    }
}
