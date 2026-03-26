package com.ernesto.chn_examen_api.service.impl;

import com.ernesto.chn_examen_api.dto.EmployeeDTO;
import com.ernesto.chn_examen_api.entity.Employee;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.mapper.EmployeeMapper;
import com.ernesto.chn_examen_api.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    void createEmployee_shouldThrowWhenCodeAlreadyExists() {
        EmployeeDTO request = sampleEmployeeDto(null, "EMP-001");

        when(employeeRepository.findByEmployeeCode("EMP-001")).thenReturn(Optional.of(new Employee()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.createEmployee(request)
        );

        assertEquals("Employee with this code already exists", ex.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void createEmployee_shouldPersistAndReturnDto() {
        EmployeeDTO request = sampleEmployeeDto(null, "EMP-001");
        Employee mapped = sampleEmployeeEntity(null, "EMP-001");
        Employee saved = sampleEmployeeEntity(1, "EMP-001");
        EmployeeDTO expected = sampleEmployeeDto(1, "EMP-001");

        when(employeeRepository.findByEmployeeCode("EMP-001")).thenReturn(Optional.empty());
        when(employeeMapper.toEntity(request)).thenReturn(mapped);
        when(employeeRepository.save(mapped)).thenReturn(saved);
        when(employeeMapper.toDto(saved)).thenReturn(expected);

        EmployeeDTO result = employeeService.createEmployee(request);

        assertEquals(1, result.getId());
        assertEquals("Ana", result.getFirstName());
        verify(employeeRepository).save(mapped);
    }

    @Test
    void getEmployeeById_shouldThrowWhenNotFound() {
        when(employeeRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.getEmployeeById(99)
        );

        assertEquals("Employee not found with id: 99", ex.getMessage());
    }

    @Test
    void getAllEmployees_shouldReturnMappedList() {
        Employee entity = sampleEmployeeEntity(1, "EMP-001");
        EmployeeDTO dto = sampleEmployeeDto(1, "EMP-001");

        when(employeeRepository.findAll()).thenReturn(List.of(entity));
        when(employeeMapper.toDto(entity)).thenReturn(dto);

        List<EmployeeDTO> result = employeeService.getAllEmployees();

        assertEquals(1, result.size());
        assertEquals("EMP-001", result.getFirst().getEmployeeCode());
    }

    @Test
    void updateEmployee_shouldThrowWhenCodeChangedAndAlreadyExists() {
        Employee existing = sampleEmployeeEntity(1, "EMP-001");
        EmployeeDTO request = sampleEmployeeDto(1, "EMP-777");

        when(employeeRepository.findById(1)).thenReturn(Optional.of(existing));
        when(employeeRepository.findByEmployeeCode("EMP-777")).thenReturn(Optional.of(new Employee()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeService.updateEmployee(1, request)
        );

        assertEquals("Employee with this code already exists", ex.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void updateEmployee_shouldUpdateAndReturnDto() {
        Employee existing = sampleEmployeeEntity(1, "EMP-001");
        EmployeeDTO request = sampleEmployeeDto(1, "EMP-999");
        EmployeeDTO expected = sampleEmployeeDto(1, "EMP-999");

        when(employeeRepository.findById(1)).thenReturn(Optional.of(existing));
        when(employeeRepository.findByEmployeeCode("EMP-999")).thenReturn(Optional.empty());
        when(employeeRepository.save(existing)).thenReturn(existing);
        when(employeeMapper.toDto(existing)).thenReturn(expected);

        EmployeeDTO result = employeeService.updateEmployee(1, request);

        assertEquals("EMP-999", result.getEmployeeCode());
        assertEquals("Analista", existing.getPosition());
        verify(employeeRepository).save(existing);
    }

    @Test
    void deleteEmployee_shouldThrowWhenNotFound() {
        when(employeeRepository.existsById(10)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.deleteEmployee(10)
        );

        assertEquals("Employee not found with id: 10", ex.getMessage());
        verify(employeeRepository, never()).deleteById(any(Integer.class));
    }

    @Test
    void deleteEmployee_shouldDeleteWhenFound() {
        when(employeeRepository.existsById(10)).thenReturn(true);

        employeeService.deleteEmployee(10);

        verify(employeeRepository).deleteById(10);
    }

    private static EmployeeDTO sampleEmployeeDto(Integer id, String employeeCode) {
        return EmployeeDTO.builder()
                .id(id)
                .firstName("Ana")
                .lastName("Lopez")
                .employeeCode(employeeCode)
                .position("Analista")
                .build();
    }

    private static Employee sampleEmployeeEntity(Integer id, String employeeCode) {
        return Employee.builder()
                .id(id)
                .firstName("Ana")
                .lastName("Lopez")
                .employeeCode(employeeCode)
                .position("Analista")
                .build();
    }
}
