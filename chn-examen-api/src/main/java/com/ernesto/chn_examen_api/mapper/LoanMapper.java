package com.ernesto.chn_examen_api.mapper;

import com.ernesto.chn_examen_api.dto.LoanDTO;
import com.ernesto.chn_examen_api.entity.Loan;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    public LoanDTO toDto(Loan entity) {
        if (entity == null) {
            return null;
        }

        return LoanDTO.builder()
                .id(entity.getId())
                .loanApplicationId(entity.getLoanApplication() != null ? entity.getLoanApplication().getId() : null)
                .customerId(entity.getCustomer() != null ? entity.getCustomer().getId() : null)
                .approvedAmount(entity.getApprovedAmount())
                .outstandingBalance(entity.getOutstandingBalance())
                .paymentStatus(entity.getPaymentStatus())
                .build();
    }
}
