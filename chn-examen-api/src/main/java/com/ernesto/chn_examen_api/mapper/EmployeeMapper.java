package com.ernesto.chn_examen_api.mapper;

import com.ernesto.chn_examen_api.dto.EmployeeDTO;
import com.ernesto.chn_examen_api.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    /**
     * Converts an Employee entity to an EmployeeDTO.
     * @param entity The Employee entity.
     * @return The EmployeeDTO.
     */
    public EmployeeDTO toDto(Employee entity) {
        if (entity == null) {
            return null;
        }

        return EmployeeDTO.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .employeeCode(entity.getEmployeeCode())
                .position(entity.getPosition())
                .build();
    }

    /**
     * Converts an EmployeeDTO to an Employee entity.
     * @param dto The EmployeeDTO.
     * @return The Employee entity.
     */
    public Employee toEntity(EmployeeDTO dto) {
        if (dto == null) {
            return null;
        }

        return Employee.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .employeeCode(dto.getEmployeeCode())
                .position(dto.getPosition())
                .build();
    }
}
