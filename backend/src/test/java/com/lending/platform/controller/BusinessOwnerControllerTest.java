package com.lending.platform.controller;

import com.lending.platform.dto.request.BusinessOwnerRequest;
import com.lending.platform.dto.request.BusinessRequest;
import com.lending.platform.entity.Business;
import com.lending.platform.entity.BusinessOwner;
import com.lending.platform.entity.BusinessType;
import com.lending.platform.mapper.BusinessMapper;
import com.lending.platform.mapper.BusinessOwnerMapper;
import com.lending.platform.repository.BusinessOwnerRepository;
import com.lending.platform.repository.BusinessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BusinessOwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private BusinessOwnerRepository businessOwnerRepository;

    @Autowired
    private BusinessMapper businessMapper;

    @Autowired
    private BusinessOwnerMapper businessOwnerMapper;

    private Business business;

    @BeforeEach
    void setUp() {
        businessOwnerRepository.deleteAll();
        businessRepository.deleteAll();

        business = businessRepository.save(
                businessMapper.toEntity(createBusinessRequest())
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createOwner_shouldReturn201ForAdmin() throws Exception {

        BusinessOwnerRequest request =
                createOwnerRequest("60.00");

        mockMvc.perform(
                        post(
                                "/api/v1/businesses/{businessId}/owners",
                                business.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.businessId")
                        .value(business.getId()))
                .andExpect(jsonPath("$.firstName")
                        .value("Daniel"))
                .andExpect(jsonPath("$.lastName")
                        .value("Carter"))
                .andExpect(jsonPath("$.ownershipPercentage")
                        .value(60.00))
                .andExpect(jsonPath("$.guarantor")
                        .value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createOwner_shouldReturn400ForInvalidRequest() throws Exception {

        BusinessOwnerRequest request =
                new BusinessOwnerRequest(
                        "",
                        "",
                        "President",
                        new BigDecimal("125.00"),
                        "invalid-email",
                        "404-555-0101",
                        true
                );

        mockMvc.perform(
                        post(
                                "/api/v1/businesses/{businessId}/owners",
                                business.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createOwner_shouldReturn409WhenOwnershipExceeds100()
            throws Exception {

        BusinessOwner existingOwner =
                businessOwnerMapper.toEntity(
                        createOwnerRequest("70.00"),
                        business
                );

        businessOwnerRepository.save(existingOwner);

        mockMvc.perform(
                        post(
                                "/api/v1/businesses/{businessId}/owners",
                                business.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                createOwnerRequest("40.00")
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error")
                        .value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Combined ownership percentage cannot exceed 100%"
                        ));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void getOwnerById_shouldReturn200ForAuthorizedUser()
            throws Exception {

        BusinessOwner owner = saveOwner(
                business,
                "60.00"
        );

        mockMvc.perform(
                        get(
                                "/api/v1/businesses/{businessId}/owners/{ownerId}",
                                business.getId(),
                                owner.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(owner.getId()))
                .andExpect(jsonPath("$.businessId")
                        .value(business.getId()))
                .andExpect(jsonPath("$.firstName")
                        .value("Daniel"));
    }

    @Test
    @WithMockUser(roles = "LOAN_OFFICER")
    void getOwnerById_shouldReturn404WhenOwnerDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/businesses/{businessId}/owners/{ownerId}",
                                business.getId(),
                                999999L
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Business owner not found with id: 999999"
                        ));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void getOwnerById_shouldReturn404WhenOwnerBelongsToDifferentBusiness()
            throws Exception {

        Business otherBusiness =
                businessRepository.save(
                        businessMapper.toEntity(
                                createSecondBusinessRequest()
                        )
                );

        BusinessOwner owner =
                saveOwner(
                        otherBusiness,
                        "60.00"
                );

        mockMvc.perform(
                        get(
                                "/api/v1/businesses/{businessId}/owners/{ownerId}",
                                business.getId(),
                                owner.getId()
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteOwner_shouldReturn204ForAdmin()
            throws Exception {

        BusinessOwner owner =
                saveOwner(
                        business,
                        "60.00"
                );

        mockMvc.perform(
                        delete(
                                "/api/v1/businesses/{businessId}/owners/{ownerId}",
                                business.getId(),
                                owner.getId()
                        )
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get(
                                "/api/v1/businesses/{businessId}/owners/{ownerId}",
                                business.getId(),
                                owner.getId()
                        )
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void deleteOwner_shouldReturn403ForNonAdmin()
            throws Exception {

        BusinessOwner owner =
                saveOwner(
                        business,
                        "60.00"
                );

        mockMvc.perform(
                        delete(
                                "/api/v1/businesses/{businessId}/owners/{ownerId}",
                                business.getId(),
                                owner.getId()
                        )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getOwners_shouldReturn401WithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/businesses/{businessId}/owners",
                                business.getId()
                        )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401))
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"));
    }

    private BusinessOwner saveOwner(
            Business targetBusiness,
            String percentage
    ) {

        BusinessOwner owner =
                businessOwnerMapper.toEntity(
                        createOwnerRequest(percentage),
                        targetBusiness
                );

        return businessOwnerRepository.save(owner);
    }

    private BusinessOwnerRequest createOwnerRequest(
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

    private BusinessRequest createBusinessRequest() {

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

    private BusinessRequest createSecondBusinessRequest() {

        return new BusinessRequest(
                "Sunrise Equipment LLC",
                "Sunrise Equipment",
                "98-7654321",
                BusinessType.LLC,
                "Equipment Rental",
                "532412",
                LocalDate.of(2017, 8, 10),
                "305-555-0200",
                "operations@sunriseequipment.com",
                "https://sunriseequipment.com",
                "800 Industrial Way",
                null,
                "Miami",
                "FL",
                "33101",
                new BigDecimal("1800000.00"),
                12
        );
    }
}