package com.ernesto.chn_examen_api.mapper;

import com.ernesto.chn_examen_api.dto.LoanApplicationDTO;
import com.ernesto.chn_examen_api.entity.LoanApplication;
import org.springframework.stereotype.Component;

@Component
public class LoanApplicationMapper {

    public LoanApplicationDTO toDto(LoanApplication entity) {
        if (entity == null) {
            return null;
        }

        return LoanApplicationDTO.builder()
                .id(entity.getId())
                .customerId(entity.getCustomer() != null ? entity.getCustomer().getId() : null)
                .requestedAmount(entity.getRequestedAmount())
                .termInMonths(entity.getTermInMonths())
                .status(entity.getStatus())
                .resolutionDetails(entity.getResolutionDetails())
                .applicationDate(entity.getApplicationDate())
                .evaluatedBy(entity.getEvaluatedBy() != null ? entity.getEvaluatedBy().getId() : null)
                .build();
    }
}
