package com.ernesto.chn_examen_api.service.impl;

import com.ernesto.chn_examen_api.dto.CustomerDTO;
import com.ernesto.chn_examen_api.entity.Customer;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.mapper.CustomerMapper;
import com.ernesto.chn_examen_api.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void createCustomer_shouldThrowWhenNitAlreadyExists() {
        CustomerDTO request = sampleCustomerDto(null, "1234567-8");

        when(customerRepository.findByNit("1234567-8")).thenReturn(Optional.of(new Customer()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(request)
        );

        assertEquals("Customer with this NIT already exists", ex.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void createCustomer_shouldPersistAndReturnDto() {
        CustomerDTO request = sampleCustomerDto(null, "1234567-8");
        Customer mappedEntity = sampleCustomerEntity(null, "1234567-8");
        Customer savedEntity = sampleCustomerEntity(1, "1234567-8");
        CustomerDTO expected = sampleCustomerDto(1, "1234567-8");

        when(customerRepository.findByNit("1234567-8")).thenReturn(Optional.empty());
        when(customerMapper.toEntity(request)).thenReturn(mappedEntity);
        when(customerRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(customerMapper.toDto(savedEntity)).thenReturn(expected);

        CustomerDTO result = customerService.createCustomer(request);

        assertEquals(1, result.getId());
        assertEquals("Juan", result.getFirstName());
        verify(customerRepository).save(mappedEntity);
    }

    @Test
    void getCustomerById_shouldThrowWhenNotFound() {
        when(customerRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.getCustomerById(999)
        );

        assertEquals("Customer not found with id: 999", ex.getMessage());
    }

    @Test
    void getAllCustomers_shouldReturnMappedList() {
        Customer customer = sampleCustomerEntity(1, "1234567-8");
        CustomerDTO dto = sampleCustomerDto(1, "1234567-8");

        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(customerMapper.toDto(customer)).thenReturn(dto);

        List<CustomerDTO> result = customerService.getAllCustomers();

        assertEquals(1, result.size());
        assertEquals("1234567-8", result.getFirst().getNit());
    }

    @Test
    void updateCustomer_shouldThrowWhenNitChangedAndAlreadyExists() {
        Customer existing = sampleCustomerEntity(1, "1234567-8");
        CustomerDTO updateRequest = sampleCustomerDto(1, "777777-1");

        when(customerRepository.findById(1)).thenReturn(Optional.of(existing));
        when(customerRepository.findByNit("777777-1")).thenReturn(Optional.of(new Customer()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.updateCustomer(1, updateRequest)
        );

        assertEquals("Customer with this NIT already exists", ex.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void updateCustomer_shouldUpdateAndReturnDto() {
        Customer existing = sampleCustomerEntity(1, "1234567-8");
        CustomerDTO updateRequest = sampleCustomerDto(1, "999999-9");
        CustomerDTO expected = sampleCustomerDto(1, "999999-9");

        when(customerRepository.findById(1)).thenReturn(Optional.of(existing));
        when(customerRepository.findByNit("999999-9")).thenReturn(Optional.empty());
        when(customerRepository.save(existing)).thenReturn(existing);
        when(customerMapper.toDto(existing)).thenReturn(expected);

        CustomerDTO result = customerService.updateCustomer(1, updateRequest);

        assertEquals("999999-9", result.getNit());
        assertEquals("Juan", existing.getFirstName());
        assertEquals("999999-9", existing.getNit());
        verify(customerRepository).save(existing);
    }

    @Test
    void deleteCustomer_shouldThrowWhenNotFound() {
        when(customerRepository.existsById(10)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.deleteCustomer(10)
        );

        assertEquals("Customer not found with id: 10", ex.getMessage());
        verify(customerRepository, never()).deleteById(any(Integer.class));
    }

    @Test
    void deleteCustomer_shouldDeleteWhenFound() {
        when(customerRepository.existsById(10)).thenReturn(true);

        customerService.deleteCustomer(10);

        verify(customerRepository).deleteById(10);
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

    private static Customer sampleCustomerEntity(Integer id, String nit) {
        return Customer.builder()
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
