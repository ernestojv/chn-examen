package com.ernesto.chn_examen_api.service.impl;

import com.ernesto.chn_examen_api.dto.PaymentDTO;
import com.ernesto.chn_examen_api.entity.AppUser;
import com.ernesto.chn_examen_api.entity.Loan;
import com.ernesto.chn_examen_api.entity.LoanApplication;
import com.ernesto.chn_examen_api.entity.Payment;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.mapper.PaymentMapper;
import com.ernesto.chn_examen_api.repository.AppUserRepository;
import com.ernesto.chn_examen_api.repository.LoanRepository;
import com.ernesto.chn_examen_api.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void registerPayment_shouldThrowWhenLoanNotFound() {
        PaymentDTO request = samplePaymentDto(null, 1, "CASH", 1, new BigDecimal("100.00"));
        when(loanRepository.findById(1)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.registerPayment(request)
        );

        assertEquals("Loan not found with id: 1", ex.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void registerPayment_shouldThrowWhenLoanIsPaidOff() {
        Loan paidOffLoan = sampleLoan(1, new BigDecimal("0.00"), "PAID_OFF");
        PaymentDTO request = samplePaymentDto(null, 1, "CASH", 1, new BigDecimal("100.00"));

        when(loanRepository.findById(1)).thenReturn(Optional.of(paidOffLoan));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.registerPayment(request)
        );

        assertEquals("Cannot register a payment for a loan that is already PAID_OFF", ex.getMessage());
    }

    @Test
    void registerPayment_shouldThrowWhenUserNotFound() {
        Loan loan = sampleLoan(1, new BigDecimal("500.00"), "CURRENT");
        PaymentDTO request = samplePaymentDto(null, 1, "CASH", 9, new BigDecimal("100.00"));

        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));
        when(appUserRepository.findById(9)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.registerPayment(request)
        );

        assertEquals("AppUser not found with id: 9", ex.getMessage());
    }

    @Test
    void registerPayment_shouldThrowWhenAmountExceedsOutstandingBalance() {
        Loan loan = sampleLoan(1, new BigDecimal("100.00"), "CURRENT");
        PaymentDTO request = samplePaymentDto(null, 1, "CASH", 1, new BigDecimal("150.00"));

        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));
        when(appUserRepository.findById(1)).thenReturn(Optional.of(sampleUser(1)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.registerPayment(request)
        );

        assertEquals("Payment amount exceeds outstanding balance of 100.00", ex.getMessage());
    }

    @Test
    void registerPayment_shouldRegisterAndReduceBalance() {
        Loan loan = sampleLoan(1, new BigDecimal("500.00"), "CURRENT");
        AppUser user = sampleUser(1);
        PaymentDTO request = samplePaymentDto(null, 1, "CARD", 1, new BigDecimal("200.00"));
        Payment saved = samplePaymentEntity(10, loan, user, new BigDecimal("200.00"), "CARD");
        PaymentDTO expected = samplePaymentDto(10, 1, "CARD", 1, new BigDecimal("200.00"));

        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));
        when(appUserRepository.findById(1)).thenReturn(Optional.of(user));
        when(loanRepository.save(loan)).thenReturn(loan);
        when(paymentRepository.save(any(Payment.class))).thenReturn(saved);
        when(paymentMapper.toDto(saved)).thenReturn(expected);

        PaymentDTO result = paymentService.registerPayment(request);

        assertEquals(10, result.getId());
        assertEquals(new BigDecimal("300.00"), loan.getOutstandingBalance());
        verify(loanRepository).save(loan);
    }

    @Test
    void registerPayment_shouldMarkLoanPaidOffWhenBalanceBecomesZero() {
        Loan loan = sampleLoan(1, new BigDecimal("200.00"), "CURRENT");
        AppUser user = sampleUser(1);
        PaymentDTO request = samplePaymentDto(null, 1, "CASH", 1, new BigDecimal("200.00"));
        Payment saved = samplePaymentEntity(11, loan, user, new BigDecimal("200.00"), "CASH");

        when(loanRepository.findById(1)).thenReturn(Optional.of(loan));
        when(appUserRepository.findById(1)).thenReturn(Optional.of(user));
        when(loanRepository.save(loan)).thenReturn(loan);
        when(paymentRepository.save(any(Payment.class))).thenReturn(saved);
        when(paymentMapper.toDto(saved)).thenReturn(samplePaymentDto(11, 1, "CASH", 1, new BigDecimal("200.00")));

        PaymentDTO result = paymentService.registerPayment(request);

        assertNotNull(result);
        assertEquals(0, loan.getOutstandingBalance().compareTo(BigDecimal.ZERO));
        assertEquals("PAID_OFF", loan.getPaymentStatus());
    }

    @Test
    void getPaymentById_shouldThrowWhenNotFound() {
        when(paymentRepository.findById(4)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.getPaymentById(4)
        );

        assertEquals("Payment not found with id: 4", ex.getMessage());
    }

    @Test
    void getAllPayments_shouldReturnMappedList() {
        Payment payment = samplePaymentEntity(1, sampleLoan(1, new BigDecimal("100"), "CURRENT"), sampleUser(1), new BigDecimal("50.00"), "CASH");
        PaymentDTO dto = samplePaymentDto(1, 1, "CASH", 1, new BigDecimal("50.00"));

        when(paymentRepository.findAll()).thenReturn(List.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(dto);

        List<PaymentDTO> result = paymentService.getAllPayments();

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getId());
    }

    @Test
    void getPaymentsByLoanId_shouldReturnMappedList() {
        Payment payment = samplePaymentEntity(1, sampleLoan(1, new BigDecimal("100"), "CURRENT"), sampleUser(1), new BigDecimal("50.00"), "CASH");
        PaymentDTO dto = samplePaymentDto(1, 1, "CASH", 1, new BigDecimal("50.00"));

        when(paymentRepository.findByLoanId(1)).thenReturn(List.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(dto);

        List<PaymentDTO> result = paymentService.getPaymentsByLoanId(1);

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getLoanId());
    }

    @Test
    void deletePayment_shouldThrowWhenNotFound() {
        when(paymentRepository.existsById(9)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.deletePayment(9)
        );

        assertEquals("Payment not found with id: 9", ex.getMessage());
    }

    @Test
    void deletePayment_shouldDeleteWhenFound() {
        when(paymentRepository.existsById(9)).thenReturn(true);

        paymentService.deletePayment(9);

        verify(paymentRepository).deleteById(9);
    }

    private static PaymentDTO samplePaymentDto(Integer id, Integer loanId, String method, Integer userId, BigDecimal amount) {
        return PaymentDTO.builder()
                .id(id)
                .loanId(loanId)
                .amountPaid(amount)
                .paymentDate(LocalDateTime.of(2026, 1, 1, 10, 0))
                .paymentMethod(method)
                .registeredById(userId)
                .build();
    }

    private static Loan sampleLoan(Integer id, BigDecimal balance, String status) {
        return Loan.builder()
                .id(id)
                .loanApplication(LoanApplication.builder().id(100).status("APPROVED").build())
                .customer(com.ernesto.chn_examen_api.entity.Customer.builder().id(1).build())
                .approvedAmount(new BigDecimal("1000.00"))
                .outstandingBalance(balance)
                .paymentStatus(status)
                .build();
    }

    private static AppUser sampleUser(Integer id) {
        return AppUser.builder().id(id).username("user" + id).password("x").build();
    }

    private static Payment samplePaymentEntity(Integer id, Loan loan, AppUser user, BigDecimal amount, String method) {
        return Payment.builder()
                .id(id)
                .loan(loan)
                .registeredBy(user)
                .amountPaid(amount)
                .paymentMethod(method)
                .paymentDate(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
    }
}
