package com.ernesto.chn_examen_api.controller;

import com.ernesto.chn_examen_api.dto.CustomerDTO;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

        @Mock
    private CustomerService customerService;

        @InjectMocks
        private CustomerController customerController;

    @Test
    void createCustomer_shouldReturnCreated() throws Exception {
        CustomerDTO request = sampleCustomerDto(null, "1234567-8");
        CustomerDTO created = sampleCustomerDto(1, "1234567-8");

                when(customerService.createCustomer(request)).thenReturn(created);

                ResponseEntity<CustomerDTO> response = customerController.createCustomer(request);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                assertEquals(1, response.getBody().getId());
                assertEquals("Juan", response.getBody().getFirstName());
    }

    @Test
        void getAllCustomers_shouldReturnOkWithList() {
        when(customerService.getAllCustomers()).thenReturn(List.of(
                sampleCustomerDto(1, "1234567-8"),
                sampleCustomerDto(2, "999999-1")
        ));

                ResponseEntity<List<CustomerDTO>> response = customerController.getAllCustomers();

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(2, response.getBody().size());
                assertEquals(1, response.getBody().getFirst().getId());
    }

    @Test
        void getCustomerById_shouldReturnOkWhenFound() {
                CustomerDTO dto = sampleCustomerDto(1, "1234567-8");
                when(customerService.getCustomerById(1)).thenReturn(dto);

                ResponseEntity<CustomerDTO> response = customerController.getCustomerById(1);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("1234567-8", response.getBody().getNit());
        }

        @Test
        void getCustomerById_shouldPropagateNotFound() {
        when(customerService.getCustomerById(999))
                .thenThrow(new ResourceNotFoundException("Customer not found with id: 999"));

                ResourceNotFoundException ex = assertThrows(
                                ResourceNotFoundException.class,
                                () -> customerController.getCustomerById(999)
                );

                assertEquals("Customer not found with id: 999", ex.getMessage());
    }

    @Test
        void updateCustomer_shouldReturnOk() {
        CustomerDTO request = sampleCustomerDto(null, "1234567-8");
        CustomerDTO updated = sampleCustomerDto(1, "1234567-8");

                when(customerService.updateCustomer(1, request)).thenReturn(updated);

                ResponseEntity<CustomerDTO> response = customerController.updateCustomer(1, request);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals(1, response.getBody().getId());
    }

    @Test
        void updateCustomer_shouldPropagateBusinessError() {
        CustomerDTO request = sampleCustomerDto(null, "1234567-8");

                when(customerService.updateCustomer(1, request))
                .thenThrow(new IllegalArgumentException("Customer with this NIT already exists"));

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> customerController.updateCustomer(1, request)
                );

                assertEquals("Customer with this NIT already exists", ex.getMessage());
    }

    @Test
        void deleteCustomer_shouldReturnNoContent() {
                ResponseEntity<Void> response = customerController.deleteCustomer(1);
                assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
        void deleteCustomer_shouldPropagateNotFound() {
                org.mockito.Mockito.doThrow(new ResourceNotFoundException("Customer not found with id: 1"))
                                .when(customerService)
                                .deleteCustomer(1);

                ResourceNotFoundException ex = assertThrows(
                                ResourceNotFoundException.class,
                                () -> customerController.deleteCustomer(1)
                );

                assertEquals("Customer not found with id: 1", ex.getMessage());
        }

        @Test
        void deleteCustomer_shouldCallService() {
                customerController.deleteCustomer(2);
                verify(customerService).deleteCustomer(2);
    }

    private static CustomerDTO sampleCustomerDto(Integer id, String nit) {
        return CustomerDTO.builder()
                .id(id)
                .firstName("Juan")
                .lastName("Perez")
                .nit(nit)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Ciudad")
                .email("juan@example.com")
                .phoneNumber("5555-5555")
                .build();
    }
}
