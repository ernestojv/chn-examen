package com.ernesto.chn_examen_api.service;

import com.ernesto.chn_examen_api.dto.PaymentDTO;

import java.util.List;

public interface PaymentService {

    PaymentDTO registerPayment(PaymentDTO dto);

    PaymentDTO getPaymentById(Integer id);

    List<PaymentDTO> getAllPayments();

    List<PaymentDTO> getPaymentsByLoanId(Integer loanId);

    List<PaymentDTO> getPaymentsByRegisteredById(Integer registeredById);

    void deletePayment(Integer id);
}
