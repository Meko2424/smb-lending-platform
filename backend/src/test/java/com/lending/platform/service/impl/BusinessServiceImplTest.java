package com.lending.platform.service.impl;

import com.lending.platform.dto.request.BusinessRequest;
import com.lending.platform.dto.response.BusinessResponse;
import com.lending.platform.entity.Business;
import com.lending.platform.entity.BusinessType;
import com.lending.platform.exception.ResourceConflictException;
import com.lending.platform.exception.ResourceNotFoundException;
import com.lending.platform.mapper.BusinessMapper;
import com.lending.platform.repository.BusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessServiceImplTest {

    @Mock
    private BusinessRepository businessRepository;

    private BusinessMapper businessMapper;
    private BusinessServiceImpl businessService;

    @BeforeEach
    void setUp() {
        businessMapper = new BusinessMapper();

        businessService = new BusinessServiceImpl(
                businessRepository,
                businessMapper
        );
    }

    @Test
    void createBusiness_shouldCreateBusiness() {

        BusinessRequest request = createRequest();

        when(businessRepository.existsByEin("123456789"))
                .thenReturn(false);

        when(businessRepository.save(any(Business.class)))
                .thenAnswer(invocation -> {
                    Business business = invocation.getArgument(0);
                    ReflectionTestUtils.setField(business, "id", 1L);
                    business.onCreate();
                    return business;
                });

        BusinessResponse response =
                businessService.createBusiness(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Atlanta Logistics LLC", response.legalName());
        assertEquals("123456789", response.ein());
        assertEquals(BusinessType.LLC, response.businessType());

        verify(businessRepository)
                .existsByEin("123456789");

        verify(businessRepository)
                .save(any(Business.class));
    }

    @Test
    void createBusiness_shouldThrowConflictWhenEinExists() {

        BusinessRequest request = createRequest();

        when(businessRepository.existsByEin("123456789"))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> businessService.createBusiness(request)
        );

        verify(businessRepository, never())
                .save(any(Business.class));
    }

    @Test
    void getBusinessById_shouldReturnBusiness() {

        Business business = createBusiness();

        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));

        BusinessResponse response =
                businessService.getBusinessById(1L);

        assertEquals(1L, response.id());
        assertEquals("Atlanta Logistics LLC", response.legalName());
        assertEquals("123456789", response.ein());

        verify(businessRepository).findById(1L);
    }

    @Test
    void getBusinessById_shouldThrowWhenBusinessDoesNotExist() {

        when(businessRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> businessService.getBusinessById(999L)
        );
    }

    @Test
    void deleteBusiness_shouldDeleteExistingBusiness() {

        Business business = createBusiness();

        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(business));

        businessService.deleteBusiness(1L);

        verify(businessRepository).delete(business);
    }

    @Test
    void getAllBusinesses_shouldReturnAllBusinesses() {

        Business firstBusiness = createBusiness();

        Business secondBusiness =
                businessMapper.toEntity(createRequest());

        ReflectionTestUtils.setField(
                secondBusiness,
                "id",
                2L
        );

        secondBusiness.setLegalName("Peachtree Construction LLC");
        secondBusiness.setEin("987654321");
        secondBusiness.onCreate();

        when(businessRepository.findAll())
                .thenReturn(java.util.List.of(
                        firstBusiness,
                        secondBusiness
                ));

        var responses = businessService.getAllBusinesses();

        assertEquals(2, responses.size());
        assertEquals(
                "Atlanta Logistics LLC",
                responses.get(0).legalName()
        );
        assertEquals(
                "Peachtree Construction LLC",
                responses.get(1).legalName()
        );

        verify(businessRepository).findAll();
    }

    @Test
    void updateBusiness_shouldUpdateExistingBusiness() {

        Business existingBusiness = createBusiness();

        BusinessRequest updatedRequest = new BusinessRequest(
                "Atlanta Logistics Group LLC",
                "Atlanta Logistics Group",
                "12-3456789",
                BusinessType.LLC,
                "Transportation and Warehousing",
                "484121",
                LocalDate.of(2019, 4, 15),
                "404-555-9999",
                "contact@atlantalogistics.com",
                "https://atlantalogistics.com",
                "1500 Peachtree Industrial Blvd",
                null,
                "Atlanta",
                "GA",
                "30309",
                new BigDecimal("3000000.00"),
                25
        );

        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(existingBusiness));

        when(businessRepository.findByEin("123456789"))
                .thenReturn(Optional.of(existingBusiness));

        when(businessRepository.save(existingBusiness))
                .thenReturn(existingBusiness);

        BusinessResponse response =
                businessService.updateBusiness(
                        1L,
                        updatedRequest
                );

        assertEquals(
                "Atlanta Logistics Group LLC",
                response.legalName()
        );

        assertEquals(
                new BigDecimal("3000000.00"),
                response.annualRevenue()
        );

        assertEquals(25, response.employeeCount());

        verify(businessRepository)
                .save(existingBusiness);
    }

    @Test
    void updateBusiness_shouldThrowConflictWhenEinBelongsToAnotherBusiness() {

        Business existingBusiness = createBusiness();

        Business otherBusiness = createBusiness();

        ReflectionTestUtils.setField(
                otherBusiness,
                "id",
                2L
        );

        otherBusiness.setEin("987654321");

        BusinessRequest request = new BusinessRequest(
                "Atlanta Logistics LLC",
                "Atlanta Logistics",
                "98-7654321",
                BusinessType.LLC,
                "Transportation",
                "484121",
                LocalDate.of(2019, 4, 15),
                "404-555-0188",
                "operations@atlantalogistics.com",
                "https://atlantalogistics.com",
                "1200 Peachtree Industrial Blvd",
                null,
                "Atlanta",
                "GA",
                "30309",
                new BigDecimal("2400000.00"),
                18
        );

        when(businessRepository.findById(1L))
                .thenReturn(Optional.of(existingBusiness));

        when(businessRepository.findByEin("987654321"))
                .thenReturn(Optional.of(otherBusiness));

        assertThrows(
                ResourceConflictException.class,
                () -> businessService.updateBusiness(
                        1L,
                        request
                )
        );

        verify(businessRepository, never())
                .save(any(Business.class));
    }

    private BusinessRequest createRequest() {
        return new BusinessRequest(
                "Atlanta Logistics LLC",
                "Atlanta Logistics",
                "12-3456789",
                BusinessType.LLC,
                "Transportation",
                "484121",
                LocalDate.of(2019, 4, 15),
                "404-555-0188",
                "operations@atlantalogistics.com",
                "https://atlantalogistics.com",
                "1200 Peachtree Industrial Blvd",
                null,
                "Atlanta",
                "GA",
                "30309",
                new BigDecimal("2400000.00"),
                18
        );
    }

    private Business createBusiness() {

        Business business =
                businessMapper.toEntity(createRequest());

        ReflectionTestUtils.setField(
                business,
                "id",
                1L
        );

        business.onCreate();

        return business;
    }
}
