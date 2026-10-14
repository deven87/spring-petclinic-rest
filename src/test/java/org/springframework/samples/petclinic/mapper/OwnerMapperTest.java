/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.samples.petclinic.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OwnerMapper.
 *
 * Tests MapStruct-based mapping between Owner entity and OwnerDto,
 * with special focus on phoneNumber field and all Owner properties.
 *
 * @author Spring PetClinic Team
 */
@DisplayName("OwnerMapper Tests")
class OwnerMapperTest {

    private OwnerMapper mapper;
    private Owner testOwner;
    private OwnerDto testOwnerDto;

    @BeforeEach
    void setUp() {
        mapper = new OwnerMapperImpl();
        initializeTestData();
    }

    /**
     * Initialize common test data objects
     */
    private void initializeTestData() {
        testOwner = new Owner();
        testOwner.setId(1);
        testOwner.setFirstName("George");
        testOwner.setLastName("Franklin");
        testOwner.setAddress("110 W. Liberty St.");
        testOwner.setCity("Madison");
        testOwner.setTelephone("6085551023");
        testOwner.setPhoneNumber("+1-608-555-1023");

        testOwnerDto = new OwnerDto();
        testOwnerDto.setId(1);
        testOwnerDto.setFirstName("George");
        testOwnerDto.setLastName("Franklin");
        testOwnerDto.setAddress("110 W. Liberty St.");
        testOwnerDto.setCity("Madison");
        testOwnerDto.setTelephone("6085551023");
        testOwnerDto.setPhoneNumber("+1-608-555-1023");
    }

    // ==================== Entity to DTO Mapping Tests ====================

    @Test
    @DisplayName("Should map Owner entity to OwnerDto successfully")
    void testToOwnerDto_Success() {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St.");
        owner.setCity("Springfield");
        owner.setTelephone("5551234567");
        owner.setPhoneNumber("+1-555-123-4567");

        // Act
        OwnerDto ownerDto = mapper.toOwnerDto(owner);

        // Assert
        assertThat(ownerDto).isNotNull();
        assertThat(ownerDto.getId()).isEqualTo(1);
        assertThat(ownerDto.getFirstName()).isEqualTo("John");
        assertThat(ownerDto.getLastName()).isEqualTo("Doe");
        assertThat(ownerDto.getAddress()).isEqualTo("123 Main St.");
        assertThat(ownerDto.getCity()).isEqualTo("Springfield");
        assertThat(ownerDto.getTelephone()).isEqualTo("5551234567");
        assertThat(ownerDto.getPhoneNumber()).isEqualTo("+1-555-123-4567");
    }

    @Test
    @DisplayName("Should map phoneNumber field from Owner to OwnerDto")
    void testToOwnerDto_MapsPhoneNumber() {
        // Arrange
        Owner owner = testOwner;

        // Act
        OwnerDto ownerDto = mapper.toOwnerDto(owner);

        // Assert
        assertThat(ownerDto.getPhoneNumber()).isEqualTo(testOwner.getPhoneNumber());
        assertThat(ownerDto.getPhoneNumber()).isEqualTo("+1-608-555-1023");
    }

    @Test
    @DisplayName("Should preserve null phoneNumber when mapping from Owner to OwnerDto")
    void testToOwnerDto_PreservesNullPhoneNumber() {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("Jane");
        owner.setLastName("Smith");
        owner.setAddress("456 Oak St.");
        owner.setCity("Shelbyville");
        owner.setTelephone("5559876543");
        owner.setPhoneNumber(null);

        // Act
        OwnerDto ownerDto = mapper.toOwnerDto(owner);

        // Assert
        assertThat(ownerDto.getPhoneNumber()).isNull();
    }

    @Test
    @DisplayName("Should handle empty phoneNumber when mapping from Owner to OwnerDto")
    void testToOwnerDto_HandlesEmptyPhoneNumber() {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("Bob");
        owner.setLastName("Parker");
        owner.setAddress("789 Pine St.");
        owner.setCity("Capital City");
        owner.setTelephone("5556789012");
        owner.setPhoneNumber("");

        // Act
        OwnerDto ownerDto = mapper.toOwnerDto(owner);

        // Assert
        assertThat(ownerDto.getPhoneNumber()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "+1-234-567-8900",
        "+1 234 567 8900",
        "(555) 123-4567",
        "555-123-4567",
        "+44-20-7946-0958",
        "+86-10-1234-5678"
    })
    @DisplayName("Should map various valid phone formats from Owner to OwnerDto")
    void testToOwnerDto_VariousPhoneFormats(String phoneNumber) {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("Test");
        owner.setLastName("User");
        owner.setAddress("Test St.");
        owner.setCity("Test City");
        owner.setTelephone("1234567890");
        owner.setPhoneNumber(phoneNumber);

        // Act
        OwnerDto ownerDto = mapper.toOwnerDto(owner);

        // Assert
        assertThat(ownerDto.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("Should map Owner with pets to OwnerDto preserving phoneNumber")
    void testToOwnerDto_WithPets() {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("Mary");
        owner.setLastName("Anderson");
        owner.setAddress("359 Nations Dr.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");
        owner.setPhoneNumber("+1-608-555-1023");

        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Rosy");
        pet.setBirthDate(LocalDate.of(2010, 3, 7));
        pet.setOwner(owner);
        PetType petType = new PetType();
        petType.setId(1);
        petType.setName("dog");
        pet.setType(petType);

        owner.addPet(pet);

        // Act
        OwnerDto ownerDto = mapper.toOwnerDto(owner);

        // Assert
        assertThat(ownerDto.getId()).isEqualTo(1);
        assertThat(ownerDto.getFirstName()).isEqualTo("Mary");
        assertThat(ownerDto.getLastName()).isEqualTo("Anderson");
        assertThat(ownerDto.getPhoneNumber()).isEqualTo("+1-608-555-1023");
        assertThat(ownerDto.getPets()).isNotNull();
        assertThat(ownerDto.getPets()).hasSize(1);
        assertThat(ownerDto.getPets().get(0).getName()).isEqualTo("Rosy");
    }

    // ==================== DTO to Entity Mapping Tests ====================

    @Test
    @DisplayName("Should map OwnerDto to Owner entity successfully")
    void testToOwner_Success() {
        // Arrange
        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setId(1);
        ownerDto.setFirstName("John");
        ownerDto.setLastName("Doe");
        ownerDto.setAddress("123 Main St.");
        ownerDto.setCity("Springfield");
        ownerDto.setTelephone("5551234567");
        ownerDto.setPhoneNumber("+1-555-123-4567");

        // Act
        Owner owner = mapper.toOwner(ownerDto);

        // Assert
        assertThat(owner).isNotNull();
        assertThat(owner.getId()).isEqualTo(1);
        assertThat(owner.getFirstName()).isEqualTo("John");
        assertThat(owner.getLastName()).isEqualTo("Doe");
        assertThat(owner.getAddress()).isEqualTo("123 Main St.");
        assertThat(owner.getCity()).isEqualTo("Springfield");
        assertThat(owner.getTelephone()).isEqualTo("5551234567");
        assertThat(owner.getPhoneNumber()).isEqualTo("+1-555-123-4567");
    }

    @Test
    @DisplayName("Should map phoneNumber field from OwnerDto to Owner")
    void testToOwner_MapsPhoneNumber() {
        // Arrange
        OwnerDto ownerDto = testOwnerDto;

        // Act
        Owner owner = mapper.toOwner(ownerDto);

        // Assert
        assertThat(owner.getPhoneNumber()).isEqualTo(testOwnerDto.getPhoneNumber());
        assertThat(owner.getPhoneNumber()).isEqualTo("+1-608-555-1023");
    }

    @Test
    @DisplayName("Should preserve null phoneNumber when mapping from OwnerDto to Owner")
    void testToOwner_PreservesNullPhoneNumber() {
        // Arrange
        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setId(1);
        ownerDto.setFirstName("Jane");
        ownerDto.setLastName("Smith");
        ownerDto.setAddress("456 Oak St.");
        ownerDto.setCity("Shelbyville");
        ownerDto.setTelephone("5559876543");
        ownerDto.setPhoneNumber(null);

        // Act
        Owner owner = mapper.toOwner(ownerDto);

        // Assert
        assertThat(owner.getPhoneNumber()).isNull();
    }

    @Test
    @DisplayName("Should handle empty phoneNumber when mapping from OwnerDto to Owner")
    void testToOwner_HandlesEmptyPhoneNumber() {
        // Arrange
        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setId(1);
        ownerDto.setFirstName("Bob");
        ownerDto.setLastName("Parker");
        ownerDto.setAddress("789 Pine St.");
        ownerDto.setCity("Capital City");
        ownerDto.setTelephone("5556789012");
        ownerDto.setPhoneNumber("");

        // Act
        Owner owner = mapper.toOwner(ownerDto);

        // Assert
        assertThat(owner.getPhoneNumber()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "+1-234-567-8900",
        "+1 234 567 8900",
        "(555) 123-4567",
        "555-123-4567",
        "+44-20-7946-0958",
        "+86-10-1234-5678"
    })
    @DisplayName("Should map various valid phone formats from OwnerDto to Owner")
    void testToOwner_VariousPhoneFormats(String phoneNumber) {
        // Arrange
        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setId(1);
        ownerDto.setFirstName("Test");
        ownerDto.setLastName("User");
        ownerDto.setAddress("Test St.");
        ownerDto.setCity("Test City");
        ownerDto.setTelephone("1234567890");
        ownerDto.setPhoneNumber(phoneNumber);

        // Act
        Owner owner = mapper.toOwner(ownerDto);

        // Assert
        assertThat(owner.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("Should map OwnerDto with pets to Owner entity preserving phoneNumber")
    void testToOwner_WithPets() {
        // Arrange
        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setId(1);
        ownerDto.setFirstName("Mary");
        ownerDto.setLastName("Anderson");
        ownerDto.setAddress("359 Nations Dr.");
        ownerDto.setCity("Madison");
        ownerDto.setTelephone("6085551023");
        ownerDto.setPhoneNumber("+1-608-555-1023");

        // Act
        Owner owner = mapper.toOwner(ownerDto);

        // Assert
        assertThat(owner.getId()).isEqualTo(1);
        assertThat(owner.getFirstName()).isEqualTo("Mary");
        assertThat(owner.getLastName()).isEqualTo("Anderson");
        assertThat(owner.getPhoneNumber()).isEqualTo("+1-608-555-1023");
    }

    // ==================== Bidirectional Mapping Tests ====================

    @Test
    @DisplayName("Should maintain phoneNumber through bidirectional mapping (Entity -> DTO -> Entity)")
    void testBidirectionalMapping_EntityDtoEntity() {
        // Arrange
        Owner originalOwner = new Owner();
        originalOwner.setId(1);
        originalOwner.setFirstName("TestFirst");
        originalOwner.setLastName("TestLast");
        originalOwner.setAddress("Test Address");
        originalOwner.setCity("Test City");
        originalOwner.setTelephone("1234567890");
        originalOwner.setPhoneNumber("+1-234-567-8900");

        // Act
        OwnerDto ownerDto = mapper.toOwnerDto(originalOwner);
        Owner mappedOwner = mapper.toOwner(ownerDto);

        // Assert
        assertThat(mappedOwner.getPhoneNumber()).isEqualTo(originalOwner.getPhoneNumber());
        assertThat(mappedOwner.getFirstName()).isEqualTo(originalOwner.getFirstName());
        assertThat(mappedOwner.getLastName()).isEqualTo(originalOwner.getLastName());
        assertThat(mappedOwner.getAddress()).isEqualTo(originalOwner.getAddress());
        assertThat(mappedOwner.getCity()).isEqualTo(originalOwner.getCity());
        assertThat(mappedOwner.getTelephone()).isEqualTo(originalOwner.getTelephone());
    }

    @Test
    @DisplayName("Should maintain phoneNumber through bidirectional mapping (DTO -> Entity -> DTO)")
    void testBidirectionalMapping_DtoEntityDto() {
        // Arrange
        OwnerDto originalDto = new OwnerDto();
        originalDto.setId(1);
        originalDto.setFirstName("TestFirst");
        originalDto.setLastName("TestLast");
        originalDto.setAddress("Test Address");
        originalDto.setCity("Test City");
        originalDto.setTelephone("1234567890");
        originalDto.setPhoneNumber("+1-234-567-8900");

        // Act
        Owner owner = mapper.toOwner(originalDto);
        OwnerDto mappedDto = mapper.toOwnerDto(owner);

        // Assert
        assertThat(mappedDto.getPhoneNumber()).isEqualTo(originalDto.getPhoneNumber());
        assertThat(mappedDto.getFirstName()).isEqualTo(originalDto.getFirstName());
        assertThat(mappedDto.getLastName()).isEqualTo(originalDto.getLastName());
        assertThat(mappedDto.getAddress()).isEqualTo(originalDto.getAddress());
        assertThat(mappedDto.getCity()).isEqualTo(originalDto.getCity());
        assertThat(mappedDto.getTelephone()).isEqualTo(originalDto.getTelephone());
    }

    @Test
    @DisplayName("Should map null values for all fields including phoneNumber")
    void testMapping_NullValues() {
        // Arrange
        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setId(null);
        ownerDto.setFirstName(null);
        ownerDto.setLastName(null);
        ownerDto.setAddress(null);
        ownerDto.setCity(null);
        ownerDto.setTelephone(null);
        ownerDto.setPhoneNumber(null);

        // Act
        Owner owner = mapper.toOwner(ownerDto);

        // Assert
        assertThat(owner.getId()).isNull();
        assertThat(owner.getFirstName()).isNull();
        assertThat(owner.getLastName()).isNull();
        assertThat(owner.getAddress()).isNull();
        assertThat(owner.getCity()).isNull();
        assertThat(owner.getTelephone()).isNull();
        assertThat(owner.getPhoneNumber()).isNull();
    }

    @Test
    @DisplayName("Should map Owner without phoneNumber set (null)")
    void testToOwnerDto_NullPhoneNumber_AllFieldsPreserved() {
        // Arrange
        Owner owner = new Owner();
        owner.setId(5);
        owner.setFirstName("Rachel");
        owner.setLastName("Green");
        owner.setAddress("123 Manhattan");
        owner.setCity("New York");
        owner.setTelephone("2125551234");
        // phoneNumber is not set (null)

        // Act
        OwnerDto ownerDto = mapper.toOwnerDto(owner);

        // Assert - Verify all other fields are mapped correctly even when phoneNumber is null
        assertThat(ownerDto.getId()).isEqualTo(5);
        assertThat(ownerDto.getFirstName()).isEqualTo("Rachel");
        assertThat(ownerDto.getLastName()).isEqualTo("Green");
        assertThat(ownerDto.getAddress()).isEqualTo("123 Manhattan");
        assertThat(ownerDto.getCity()).isEqualTo("New York");
        assertThat(ownerDto.getTelephone()).isEqualTo("2125551234");
        assertThat(ownerDto.getPhoneNumber()).isNull();
    }
}
