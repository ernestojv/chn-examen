package com.ernesto.chn_examen_api.service.impl;

import com.ernesto.chn_examen_api.dto.PaymentDTO;
import com.ernesto.chn_examen_api.entity.AppUser;
import com.ernesto.chn_examen_api.entity.Loan;
import com.ernesto.chn_examen_api.entity.Payment;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.mapper.PaymentMapper;
import com.ernesto.chn_examen_api.repository.AppUserRepository;
import com.ernesto.chn_examen_api.repository.LoanRepository;
import com.ernesto.chn_examen_api.repository.PaymentRepository;
import com.ernesto.chn_examen_api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final LoanRepository loanRepository;
    private final AppUserRepository appUserRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentDTO registerPayment(PaymentDTO dto) {
        Loan loan = loanRepository.findById(dto.getLoanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + dto.getLoanId()));

        if ("PAID_OFF".equalsIgnoreCase(loan.getPaymentStatus())) {
            throw new IllegalArgumentException("Cannot register a payment for a loan that is already PAID_OFF");
        }

        AppUser registeredBy = appUserRepository.findById(dto.getRegisteredById())
                .orElseThrow(() -> new ResourceNotFoundException("AppUser not found with id: " + dto.getRegisteredById()));

        if (dto.getAmountPaid().compareTo(loan.getOutstandingBalance()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds outstanding balance of " + loan.getOutstandingBalance());
        }

        Payment payment = Payment.builder()
                .loan(loan)
                .amountPaid(dto.getAmountPaid())
                .paymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDateTime.now())
                .paymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "CASH")
                .registeredBy(registeredBy)
                .build();

        // Reduce the outstanding balance on the loan
        BigDecimal newBalance = loan.getOutstandingBalance().subtract(dto.getAmountPaid());
        loan.setOutstandingBalance(newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            loan.setPaymentStatus("PAID_OFF");
        }
        loanRepository.save(loan);

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Integer id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return paymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByLoanId(Integer loanId) {
        return paymentRepository.findByLoanId(loanId).stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByRegisteredById(Integer registeredById) {
        return paymentRepository.findByRegisteredById(registeredById).stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePayment(Integer id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }
}
