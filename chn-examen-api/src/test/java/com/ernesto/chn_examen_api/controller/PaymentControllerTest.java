package com.ernesto.chn_examen_api.controller;

import com.ernesto.chn_examen_api.dto.PaymentDTO;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void registerPayment_shouldReturnCreated() {
        PaymentDTO request = samplePaymentDto(null, 1, "CASH", 1, new BigDecimal("100.00"));
        PaymentDTO created = samplePaymentDto(1, 1, "CASH", 1, new BigDecimal("100.00"));

        when(paymentService.registerPayment(request)).thenReturn(created);

        ResponseEntity<PaymentDTO> response = paymentController.registerPayment(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().getId());
    }

    @Test
    void getAllPayments_shouldReturnOk() {
        when(paymentService.getAllPayments()).thenReturn(List.of(
                samplePaymentDto(1, 1, "CASH", 1, new BigDecimal("100.00")),
                samplePaymentDto(2, 1, "CARD", 1, new BigDecimal("50.00"))
        ));

        ResponseEntity<List<PaymentDTO>> response = paymentController.getAllPayments();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getPaymentById_shouldReturnOk() {
        when(paymentService.getPaymentById(1)).thenReturn(samplePaymentDto(1, 1, "CASH", 1, new BigDecimal("100.00")));

        ResponseEntity<PaymentDTO> response = paymentController.getPaymentById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getId());
    }

    @Test
    void getPaymentById_shouldPropagateNotFound() {
        when(paymentService.getPaymentById(99)).thenThrow(new ResourceNotFoundException("Payment not found with id: 99"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentController.getPaymentById(99)
        );

        assertEquals("Payment not found with id: 99", ex.getMessage());
    }

    @Test
    void getPaymentsByLoanId_shouldReturnOk() {
        when(paymentService.getPaymentsByLoanId(1))
                .thenReturn(List.of(samplePaymentDto(1, 1, "CASH", 1, new BigDecimal("100.00"))));

        ResponseEntity<List<PaymentDTO>> response = paymentController.getPaymentsByLoanId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getPaymentsByRegisteredById_shouldReturnOk() {
        when(paymentService.getPaymentsByRegisteredById(1))
                .thenReturn(List.of(samplePaymentDto(1, 1, "CASH", 1, new BigDecimal("100.00"))));

        ResponseEntity<List<PaymentDTO>> response = paymentController.getPaymentsByRegisteredById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void deletePayment_shouldReturnNoContentAndCallService() {
        ResponseEntity<Void> response = paymentController.deletePayment(5);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(paymentService).deletePayment(5);
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
}
