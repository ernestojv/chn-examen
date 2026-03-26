package com.ernesto.chn_examen_api.mapper;

import com.ernesto.chn_examen_api.dto.PaymentDTO;
import com.ernesto.chn_examen_api.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentDTO toDto(Payment entity) {
        if (entity == null) {
            return null;
        }

        return PaymentDTO.builder()
                .id(entity.getId())
                .loanId(entity.getLoan() != null ? entity.getLoan().getId() : null)
                .amountPaid(entity.getAmountPaid())
                .paymentDate(entity.getPaymentDate())
                .paymentMethod(entity.getPaymentMethod())
                .registeredById(entity.getRegisteredBy() != null ? entity.getRegisteredBy().getId() : null)
                .build();
    }
}
