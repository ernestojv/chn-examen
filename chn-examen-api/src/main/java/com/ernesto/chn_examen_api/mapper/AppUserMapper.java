package com.ernesto.chn_examen_api.mapper;

import com.ernesto.chn_examen_api.dto.AppUserDTO;
import com.ernesto.chn_examen_api.entity.AppUser;
import org.springframework.stereotype.Component;

@Component
public class AppUserMapper {

    public AppUserDTO toDto(AppUser entity) {
        if (entity == null) {
            return null;
        }

        return AppUserDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .build();
    }
}
