package com.ernesto.chn_examen_api.mapper;

import com.ernesto.chn_examen_api.dto.CustomerDTO;
import com.ernesto.chn_examen_api.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    /**
     * Converts a Customer entity to a CustomerDTO.
     * @param entity The Customer entity.
     * @return The CustomerDTO.
     */
    public CustomerDTO toDto(Customer entity) {
        if (entity == null) {
            return null;
        }
        
        return CustomerDTO.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .nit(entity.getNit())
                .dateOfBirth(entity.getDateOfBirth())
                .address(entity.getAddress())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .build();
    }

    /**
     * Converts a CustomerDTO to a Customer entity.
     * @param dto The CustomerDTO.
     * @return The Customer entity.
     */
    public Customer toEntity(CustomerDTO dto) {
        if (dto == null) {
            return null;
        }

        return Customer.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .nit(dto.getNit())
                .dateOfBirth(dto.getDateOfBirth())
                .address(dto.getAddress())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .build();
    }
}
