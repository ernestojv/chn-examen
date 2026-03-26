package com.ernesto.chn_examen_api.repository;

import com.ernesto.chn_examen_api.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    
    /**
     * Find an employee by their employee code.
     * @param employeeCode The employee code to search for.
     * @return An Optional containing the employee if found.
     */
    Optional<Employee> findByEmployeeCode(String employeeCode);
}
