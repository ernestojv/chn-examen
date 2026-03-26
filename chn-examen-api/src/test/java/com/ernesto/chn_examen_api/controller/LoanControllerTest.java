package com.ernesto.chn_examen_api.controller;

import com.ernesto.chn_examen_api.dto.LoanDTO;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.service.LoanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanControllerTest {

    @Mock
    private LoanService loanService;

    @InjectMocks
    private LoanController loanController;

    @Test
    void getAllLoans_shouldReturnOkWithList() {
        when(loanService.getAllLoans()).thenReturn(List.of(
                sampleLoanDto(1, 10, 1, "CURRENT"),
                sampleLoanDto(2, 11, 2, "PAID")
        ));

        ResponseEntity<List<LoanDTO>> response = loanController.getAllLoans();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getLoanById_shouldReturnOk() {
        when(loanService.getLoanById(1)).thenReturn(sampleLoanDto(1, 10, 1, "CURRENT"));

        ResponseEntity<LoanDTO> response = loanController.getLoanById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getId());
    }

    @Test
    void getLoanById_shouldPropagateNotFound() {
        when(loanService.getLoanById(99)).thenThrow(new ResourceNotFoundException("Loan not found with id: 99"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanController.getLoanById(99)
        );

        assertEquals("Loan not found with id: 99", ex.getMessage());
    }

    @Test
    void getLoansByCustomerId_shouldReturnOk() {
        when(loanService.getLoansByCustomerId(1)).thenReturn(List.of(sampleLoanDto(1, 10, 1, "CURRENT")));

        ResponseEntity<List<LoanDTO>> response = loanController.getLoansByCustomerId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getLoanByApplicationId_shouldReturnOk() {
        when(loanService.getLoanByApplicationId(10)).thenReturn(sampleLoanDto(1, 10, 1, "CURRENT"));

        ResponseEntity<LoanDTO> response = loanController.getLoanByApplicationId(10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10, response.getBody().getLoanApplicationId());
    }

    @Test
    void getLoanByApplicationId_shouldPropagateNotFound() {
        when(loanService.getLoanByApplicationId(10))
                .thenThrow(new ResourceNotFoundException("Loan not found for application id: 10"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanController.getLoanByApplicationId(10)
        );

        assertEquals("Loan not found for application id: 10", ex.getMessage());
    }

    private static LoanDTO sampleLoanDto(Integer id, Integer appId, Integer customerId, String status) {
        return LoanDTO.builder()
                .id(id)
                .loanApplicationId(appId)
                .customerId(customerId)
                .approvedAmount(new BigDecimal("5000.00"))
                .outstandingBalance(new BigDecimal("2500.00"))
                .paymentStatus(status)
                .build();
    }
}
