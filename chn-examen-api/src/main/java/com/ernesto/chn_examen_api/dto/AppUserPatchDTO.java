package com.ernesto.chn_examen_api.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUserPatchDTO {

    @Size(max = 50, message = "Username must not exceed 50 characters")
    private String username;

    @Size(max = 255, message = "Password must not exceed 255 characters")
    private String password;

    private Integer employeeId;
}
