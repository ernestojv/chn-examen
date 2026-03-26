package com.ernesto.chn_examen_api.service;

import com.ernesto.chn_examen_api.dto.EmployeeDTO;

import java.util.List;

public interface EmployeeService {

    /**
     * Create a new employee.
     * @param employeeDTO The employee information.
     * @return The created employee.
     */
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO);

    /**
     * Retrieve an employee by their ID.
     * @param id The employee ID.
     * @return The employee information.
     */
    EmployeeDTO getEmployeeById(Integer id);

    /**
     * Retrieve all employees.
     * @return A list of all employees.
     */
    List<EmployeeDTO> getAllEmployees();

    /**
     * Update an existing employee.
     * @param id The employee ID.
     * @param employeeDTO The updated information.
     * @return The updated employee.
     */
    EmployeeDTO updateEmployee(Integer id, EmployeeDTO employeeDTO);

    /**
     * Delete an employee.
     * @param id The employee ID.
     */
    void deleteEmployee(Integer id);
}
