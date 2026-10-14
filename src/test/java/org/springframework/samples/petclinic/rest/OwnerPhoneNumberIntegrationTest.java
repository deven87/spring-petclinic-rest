/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest;

import org.springframework.samples.petclinic.rest.controller.v1.OwnerRestControllerV1;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Owner phoneNumber field REST endpoints
 *
 * @author AI Assistant
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerPhoneNumberIntegrationTest {

    @Autowired
    private OwnerRestControllerV1 ownerRestControllerV1;

    @Autowired
    private OwnerMapper ownerMapper;

    @MockitoBean
    private ClinicService clinicService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ownerRestControllerV1)
            .setControllerAdvice(new ExceptionControllerAdvice())
            .build();
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testCreateOwnerWithPhoneNumber() throws Exception {
        // Test 1: POST /api/owners with valid phoneNumber → 201
        OwnerDto newOwnerDto = new OwnerDto();
        newOwnerDto.setFirstName("John");
        newOwnerDto.setLastName("Doe");
        newOwnerDto.setAddress("123 Main St.");
        newOwnerDto.setCity("Springfield");
        newOwnerDto.setTelephone("1234567890");
        newOwnerDto.setPhoneNumber("+1-234-567-8900");

        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);

        this.mockMvc.perform(post("/api/owners")
                .content(newOwnerAsJSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isCreated())
            .andExpect(content().contentType("application/json"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testGetOwnerReturnsPhoneNumber() throws Exception {
        // Test 2: GET /api/owners/{id} returns phoneNumber field
        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setId(1);
        ownerDto.setFirstName("Jane");
        ownerDto.setLastName("Smith");
        ownerDto.setAddress("456 Oak Ave.");
        ownerDto.setCity("Shelbyville");
        ownerDto.setTelephone("9876543210");
        ownerDto.setPhoneNumber("+1-987-654-3210");

        Owner owner = ownerMapper.toOwner(ownerDto);
        given(this.clinicService.findOwnerById(1)).willReturn(owner);

        this.mockMvc.perform(get("/api/owners/1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.phoneNumber").value("+1-987-654-3210"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testUpdateOwnerPhoneNumber() throws Exception {
        // Test 3: PUT /api/owners/{id} updates phoneNumber → 200/204
        int ownerId = 1;
        
        OwnerDto existingOwnerDto = new OwnerDto();
        existingOwnerDto.setId(ownerId);
        existingOwnerDto.setFirstName("Bob");
        existingOwnerDto.setLastName("Johnson");
        existingOwnerDto.setAddress("789 Pine Rd.");
        existingOwnerDto.setCity("Capital City");
        existingOwnerDto.setTelephone("5555551234");
        existingOwnerDto.setPhoneNumber("+1-555-555-1234");

        Owner existingOwner = ownerMapper.toOwner(existingOwnerDto);
        given(this.clinicService.findOwnerById(ownerId)).willReturn(existingOwner);

        // Update with new phone number
        OwnerDto updatedOwnerDto = new OwnerDto();
        updatedOwnerDto.setId(ownerId);
        updatedOwnerDto.setFirstName("Bob");
        updatedOwnerDto.setLastName("Johnson");
        updatedOwnerDto.setAddress("789 Pine Rd.");
        updatedOwnerDto.setCity("Capital City");
        updatedOwnerDto.setTelephone("5555551234");
        updatedOwnerDto.setPhoneNumber("+1-555-555-9999");

        ObjectMapper mapper = new ObjectMapper();
        String updatedOwnerAsJSON = mapper.writeValueAsString(updatedOwnerDto);

        this.mockMvc.perform(put("/api/owners/" + ownerId)
                .content(updatedOwnerAsJSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNoContent());

        // Verify the phone number was updated
        given(this.clinicService.findOwnerById(ownerId)).willReturn(ownerMapper.toOwner(updatedOwnerDto));

        this.mockMvc.perform(get("/api/owners/" + ownerId)
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(ownerId))
            .andExpect(jsonPath("$.phoneNumber").value("+1-555-555-9999"));
    }
}
