package com.ernesto.chn_examen_api.controller;

import com.ernesto.chn_examen_api.dto.EmployeeDTO;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @Test
    void createEmployee_shouldReturnCreated() {
        EmployeeDTO request = sampleEmployeeDto(null, "EMP-001");
        EmployeeDTO created = sampleEmployeeDto(1, "EMP-001");

        when(employeeService.createEmployee(request)).thenReturn(created);

        ResponseEntity<EmployeeDTO> response = employeeController.createEmployee(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().getId());
        assertEquals("Ana", response.getBody().getFirstName());
    }

    @Test
    void getAllEmployees_shouldReturnOkWithList() {
        when(employeeService.getAllEmployees()).thenReturn(List.of(
                sampleEmployeeDto(1, "EMP-001"),
                sampleEmployeeDto(2, "EMP-002")
        ));

        ResponseEntity<List<EmployeeDTO>> response = employeeController.getAllEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals(1, response.getBody().getFirst().getId());
    }

    @Test
    void getEmployeeById_shouldReturnOkWhenFound() {
        EmployeeDTO dto = sampleEmployeeDto(1, "EMP-001");
        when(employeeService.getEmployeeById(1)).thenReturn(dto);

        ResponseEntity<EmployeeDTO> response = employeeController.getEmployeeById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("EMP-001", response.getBody().getEmployeeCode());
    }

    @Test
    void getEmployeeById_shouldPropagateNotFound() {
        when(employeeService.getEmployeeById(999))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 999"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> employeeController.getEmployeeById(999)
        );

        assertEquals("Employee not found with id: 999", ex.getMessage());
    }

    @Test
    void updateEmployee_shouldReturnOk() {
        EmployeeDTO request = sampleEmployeeDto(null, "EMP-001");
        EmployeeDTO updated = sampleEmployeeDto(1, "EMP-001");

        when(employeeService.updateEmployee(1, request)).thenReturn(updated);

        ResponseEntity<EmployeeDTO> response = employeeController.updateEmployee(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getId());
    }

    @Test
    void updateEmployee_shouldPropagateBusinessError() {
        EmployeeDTO request = sampleEmployeeDto(null, "EMP-001");

        when(employeeService.updateEmployee(1, request))
                .thenThrow(new IllegalArgumentException("Employee with this code already exists"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> employeeController.updateEmployee(1, request)
        );

        assertEquals("Employee with this code already exists", ex.getMessage());
    }

    @Test
    void deleteEmployee_shouldReturnNoContent() {
        ResponseEntity<Void> response = employeeController.deleteEmployee(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteEmployee_shouldPropagateNotFound() {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Employee not found with id: 1"))
                .when(employeeService)
                .deleteEmployee(1);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> employeeController.deleteEmployee(1)
        );

        assertEquals("Employee not found with id: 1", ex.getMessage());
    }

    @Test
    void deleteEmployee_shouldCallService() {
        employeeController.deleteEmployee(2);

        verify(employeeService).deleteEmployee(2);
    }

    private static EmployeeDTO sampleEmployeeDto(Integer id, String code) {
        return EmployeeDTO.builder()
                .id(id)
                .firstName("Ana")
                .lastName("Lopez")
                .employeeCode(code)
                .position("Analista")
                .build();
    }
}
