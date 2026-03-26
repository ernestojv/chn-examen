package com.ernesto.chn_examen_api.dto;

import jakarta.validation.constraints.DecimalMin;
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
public class LoanDTO {

    private Integer id;

    @NotNull(message = "Loan Application ID is required")
    private Integer loanApplicationId;

    @NotNull(message = "Customer ID is required")
    private Integer customerId;

    @NotNull(message = "Approved amount is required")
    @DecimalMin(value = "0.01", message = "Approved amount must be greater than zero")
    private BigDecimal approvedAmount;

    @NotNull(message = "Outstanding balance is required")
    @DecimalMin(value = "0.00", message = "Outstanding balance cannot be negative")
    private BigDecimal outstandingBalance;

    private String paymentStatus;
}
