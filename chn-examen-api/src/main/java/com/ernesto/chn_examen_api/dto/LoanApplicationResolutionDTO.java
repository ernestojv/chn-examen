package com.ernesto.chn_examen_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationResolutionDTO {

    /**
     * Resolution status: must be "APPROVED" or "REJECTED".
     */
    @NotBlank(message = "Status is required")
    private String status;

    private String resolutionDetails;

    @NotNull(message = "Evaluator user ID is required")
    private Integer evaluatedById;

    /**
     * Required only when status is "APPROVED".
     */
    @DecimalMin(value = "0.01", message = "Approved amount must be greater than zero")
    private BigDecimal approvedAmount;
}
