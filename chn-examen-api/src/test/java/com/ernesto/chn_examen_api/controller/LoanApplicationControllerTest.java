package com.ernesto.chn_examen_api.controller;

import com.ernesto.chn_examen_api.dto.LoanApplicationDTO;
import com.ernesto.chn_examen_api.dto.LoanApplicationResolutionDTO;
import com.ernesto.chn_examen_api.dto.LoanApplicationResolutionResultDTO;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.service.LoanApplicationService;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationControllerTest {

    @Mock
    private LoanApplicationService loanApplicationService;

    @InjectMocks
    private LoanApplicationController loanApplicationController;

    @Test
    void createLoanApplication_shouldReturnCreated() {
        LoanApplicationDTO request = sampleLoanAppDto(null, 1, "PENDING");
        LoanApplicationDTO created = sampleLoanAppDto(1, 1, "PENDING");

        when(loanApplicationService.createLoanApplication(request)).thenReturn(created);

        ResponseEntity<LoanApplicationDTO> response = loanApplicationController.createLoanApplication(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().getId());
    }

    @Test
    void getAllLoanApplications_shouldReturnOk() {
        when(loanApplicationService.getAllLoanApplications()).thenReturn(List.of(
                sampleLoanAppDto(1, 1, "PENDING"),
                sampleLoanAppDto(2, 2, "APPROVED")
        ));

        ResponseEntity<List<LoanApplicationDTO>> response = loanApplicationController.getAllLoanApplications();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getLoanApplicationById_shouldReturnOk() {
        when(loanApplicationService.getLoanApplicationById(1)).thenReturn(sampleLoanAppDto(1, 1, "PENDING"));

        ResponseEntity<LoanApplicationDTO> response = loanApplicationController.getLoanApplicationById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getId());
    }

    @Test
    void getLoanApplicationById_shouldPropagateNotFound() {
        when(loanApplicationService.getLoanApplicationById(99))
                .thenThrow(new ResourceNotFoundException("Loan Application not found with id: 99"));

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanApplicationController.getLoanApplicationById(99)
        );

        assertEquals("Loan Application not found with id: 99", ex.getMessage());
    }

    @Test
    void getByCustomerAndStatus_shouldReturnOk() {
        when(loanApplicationService.getLoanApplicationsByCustomerAndStatus(1, "PENDING"))
                .thenReturn(List.of(sampleLoanAppDto(1, 1, "PENDING")));

        ResponseEntity<List<LoanApplicationDTO>> response =
                loanApplicationController.getLoanApplicationsByCustomerAndStatus(1, "PENDING");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void updateLoanApplication_shouldReturnOk() {
        LoanApplicationDTO request = sampleLoanAppDto(null, 1, "PENDING");
        LoanApplicationDTO updated = sampleLoanAppDto(1, 1, "APPROVED");

        when(loanApplicationService.updateLoanApplication(1, request)).thenReturn(updated);

        ResponseEntity<LoanApplicationDTO> response = loanApplicationController.updateLoanApplication(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("APPROVED", response.getBody().getStatus());
    }

    @Test
    void updateStatus_shouldDelegatePayloadValues() {
        LoanApplicationDTO updated = sampleLoanAppDto(1, 1, "APPROVED");
        when(loanApplicationService.updateStatus(1, "APPROVED", "ok")).thenReturn(updated);

        ResponseEntity<LoanApplicationDTO> response = loanApplicationController.updateStatus(
                1,
                Map.of("status", "APPROVED", "resolutionDetails", "ok")
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(loanApplicationService).updateStatus(1, "APPROVED", "ok");
    }

    @Test
    void resolveLoanApplication_shouldReturnOk() {
        LoanApplicationResolutionDTO request = LoanApplicationResolutionDTO.builder()
                .status("REJECTED")
                .evaluatedById(1)
                .resolutionDetails("No procede")
                .build();

        LoanApplicationResolutionResultDTO result = LoanApplicationResolutionResultDTO.builder()
                .loanApplication(sampleLoanAppDto(1, 1, "REJECTED"))
                .createdLoan(null)
                .build();

        when(loanApplicationService.resolveLoanApplication(1, request)).thenReturn(result);

        ResponseEntity<LoanApplicationResolutionResultDTO> response =
                loanApplicationController.resolveLoanApplication(1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("REJECTED", response.getBody().getLoanApplication().getStatus());
    }

    @Test
    void deleteLoanApplication_shouldReturnNoContentAndCallService() {
        ResponseEntity<Void> response = loanApplicationController.deleteLoanApplication(3);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(loanApplicationService).deleteLoanApplication(3);
    }

    private static LoanApplicationDTO sampleLoanAppDto(Integer id, Integer customerId, String status) {
        return LoanApplicationDTO.builder()
                .id(id)
                .customerId(customerId)
                .requestedAmount(new BigDecimal("1000.00"))
                .termInMonths(12)
                .status(status)
                .applicationDate(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
    }
}
