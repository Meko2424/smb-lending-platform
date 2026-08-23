package com.lending.platform.controller;

import com.lending.platform.dto.request.BusinessRequest;
import com.lending.platform.entity.Business;
import com.lending.platform.entity.BusinessType;
import com.lending.platform.repository.BusinessRepository;
import com.lending.platform.mapper.BusinessMapper;
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
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private BusinessMapper businessMapper;

    @BeforeEach
    void setUp() {
        businessRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBusiness_shouldReturn201ForAdmin() throws Exception {

        BusinessRequest request = createRequest();

        mockMvc.perform(
                        post("/api/v1/businesses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.legalName")
                        .value("Atlanta Logistics LLC"))
                .andExpect(jsonPath("$.ein")
                        .value("123456789"))
                .andExpect(jsonPath("$.businessType")
                        .value("LLC"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBusiness_shouldReturn400ForInvalidRequest() throws Exception {

        BusinessRequest request = new BusinessRequest(
                "",
                null,
                "123",
                null,
                "",
                null,
                null,
                null,
                null,
                null,
                "",
                null,
                "",
                "",
                "",
                new BigDecimal("-1000"),
                -5
        );

        mockMvc.perform(
                        post("/api/v1/businesses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBusiness_shouldReturn409WhenEinAlreadyExists()
            throws Exception {

        Business existing =
                businessMapper.toEntity(createRequest());

        businessRepository.save(existing);

        mockMvc.perform(
                        post("/api/v1/businesses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        createRequest()
                                ))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("A business with this EIN already exists"));
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void getBusinessById_shouldReturn200ForAuthorizedUser()
            throws Exception {

        Business savedBusiness =
                businessRepository.save(
                        businessMapper.toEntity(createRequest())
                );

        mockMvc.perform(
                        get("/api/v1/businesses/{id}",
                                savedBusiness.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(savedBusiness.getId()))
                .andExpect(jsonPath("$.legalName")
                        .value("Atlanta Logistics LLC"))
                .andExpect(jsonPath("$.ein")
                        .value("123456789"));
    }

    @Test
    @WithMockUser(roles = "LOAN_OFFICER")
    void getBusinessById_shouldReturn404WhenBusinessDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/businesses/{id}", 999999L)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Business not found with id: 999999"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBusiness_shouldReturn204ForAdmin()
            throws Exception {

        Business savedBusiness =
                businessRepository.save(
                        businessMapper.toEntity(createRequest())
                );

        mockMvc.perform(
                        delete("/api/v1/businesses/{id}",
                                savedBusiness.getId())
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/api/v1/businesses/{id}",
                                savedBusiness.getId())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "UNDERWRITER")
    void deleteBusiness_shouldReturn403ForNonAdmin()
            throws Exception {

        Business savedBusiness =
                businessRepository.save(
                        businessMapper.toEntity(createRequest())
                );

        mockMvc.perform(
                        delete("/api/v1/businesses/{id}",
                                savedBusiness.getId())
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllBusinesses_shouldReturn401WithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/businesses")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error")
                        .value("Unauthorized"));
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
}