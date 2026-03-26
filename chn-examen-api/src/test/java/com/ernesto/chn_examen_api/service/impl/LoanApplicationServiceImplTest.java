package com.ernesto.chn_examen_api.service.impl;

import com.ernesto.chn_examen_api.dto.LoanApplicationDTO;
import com.ernesto.chn_examen_api.dto.LoanApplicationResolutionDTO;
import com.ernesto.chn_examen_api.dto.LoanApplicationResolutionResultDTO;
import com.ernesto.chn_examen_api.dto.LoanDTO;
import com.ernesto.chn_examen_api.entity.AppUser;
import com.ernesto.chn_examen_api.entity.Customer;
import com.ernesto.chn_examen_api.entity.Loan;
import com.ernesto.chn_examen_api.entity.LoanApplication;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.mapper.LoanApplicationMapper;
import com.ernesto.chn_examen_api.mapper.LoanMapper;
import com.ernesto.chn_examen_api.repository.AppUserRepository;
import com.ernesto.chn_examen_api.repository.CustomerRepository;
import com.ernesto.chn_examen_api.repository.LoanApplicationRepository;
import com.ernesto.chn_examen_api.repository.LoanRepository;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceImplTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private LoanApplicationMapper loanApplicationMapper;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanMapper loanMapper;

    @InjectMocks
    private LoanApplicationServiceImpl loanApplicationService;

    @Test
    void createLoanApplication_shouldThrowWhenCustomerNotFound() {
        LoanApplicationDTO request = sampleLoanAppDto(null, 1, "PENDING", null);
        when(customerRepository.findById(1)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanApplicationService.createLoanApplication(request)
        );

        assertEquals("Customer not found with id: 1", ex.getMessage());
        verify(loanApplicationRepository, never()).save(any(LoanApplication.class));
    }

    @Test
    void createLoanApplication_shouldThrowWhenEvaluatorNotFound() {
        LoanApplicationDTO request = sampleLoanAppDto(null, 1, "PENDING", 9);
        when(customerRepository.findById(1)).thenReturn(Optional.of(sampleCustomer(1)));
        when(appUserRepository.findById(9)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanApplicationService.createLoanApplication(request)
        );

        assertEquals("Evaluator AppUser not found with id: 9", ex.getMessage());
    }

    @Test
    void createLoanApplication_shouldPersistAndReturnDto() {
        LoanApplicationDTO request = sampleLoanAppDto(null, 1, null, null);
        Customer customer = sampleCustomer(1);
        LoanApplication saved = sampleLoanApplication(10, customer, "PENDING", null);
        LoanApplicationDTO expected = sampleLoanAppDto(10, 1, "PENDING", null);

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(saved);
        when(loanApplicationMapper.toDto(saved)).thenReturn(expected);

        LoanApplicationDTO result = loanApplicationService.createLoanApplication(request);

        assertEquals(10, result.getId());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void getLoanApplicationById_shouldThrowWhenNotFound() {
        when(loanApplicationRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanApplicationService.getLoanApplicationById(99)
        );

        assertEquals("Loan Application not found with id: 99", ex.getMessage());
    }

    @Test
    void getAllLoanApplications_shouldReturnMappedList() {
        LoanApplication entity = sampleLoanApplication(1, sampleCustomer(1), "PENDING", null);
        LoanApplicationDTO dto = sampleLoanAppDto(1, 1, "PENDING", null);

        when(loanApplicationRepository.findAll()).thenReturn(List.of(entity));
        when(loanApplicationMapper.toDto(entity)).thenReturn(dto);

        List<LoanApplicationDTO> result = loanApplicationService.getAllLoanApplications();

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getCustomerId());
    }

    @Test
    void updateStatus_shouldThrowWhenEmptyStatus() {
        LoanApplication existing = sampleLoanApplication(1, sampleCustomer(1), "PENDING", null);
        when(loanApplicationRepository.findById(1)).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanApplicationService.updateStatus(1, "   ", "x")
        );

        assertEquals("Status cannot be empty", ex.getMessage());
        verify(loanApplicationRepository, never()).save(any(LoanApplication.class));
    }

    @Test
    void updateStatus_shouldNormalizeAndPersist() {
        LoanApplication existing = sampleLoanApplication(1, sampleCustomer(1), "PENDING", null);
        LoanApplicationDTO expected = sampleLoanAppDto(1, 1, "APPROVED", null);

        when(loanApplicationRepository.findById(1)).thenReturn(Optional.of(existing));
        when(loanApplicationRepository.save(existing)).thenReturn(existing);
        when(loanApplicationMapper.toDto(existing)).thenReturn(expected);

        LoanApplicationDTO result = loanApplicationService.updateStatus(1, " approved ", "ok");

        assertEquals("APPROVED", result.getStatus());
        assertEquals("ok", existing.getResolutionDetails());
    }

    @Test
    void resolveLoanApplication_shouldThrowWhenStatusIsNotPending() {
        LoanApplication existing = sampleLoanApplication(1, sampleCustomer(1), "REJECTED", null);
        LoanApplicationResolutionDTO request = LoanApplicationResolutionDTO.builder()
                .status("APPROVED")
                .evaluatedById(1)
                .approvedAmount(new BigDecimal("5000.00"))
                .build();

        when(loanApplicationRepository.findById(1)).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanApplicationService.resolveLoanApplication(1, request)
        );

        assertEquals("Only PENDING applications can be resolved. Current status: REJECTED", ex.getMessage());
    }

    @Test
    void resolveLoanApplication_shouldThrowWhenApprovedWithoutAmount() {
        LoanApplication existing = sampleLoanApplication(1, sampleCustomer(1), "PENDING", null);
        LoanApplicationResolutionDTO request = LoanApplicationResolutionDTO.builder()
                .status("APPROVED")
                .evaluatedById(1)
                .build();

        when(loanApplicationRepository.findById(1)).thenReturn(Optional.of(existing));
        when(appUserRepository.findById(1)).thenReturn(Optional.of(sampleUser(1)));
        when(loanApplicationRepository.save(existing)).thenReturn(existing);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanApplicationService.resolveLoanApplication(1, request)
        );

        assertEquals("approvedAmount is required when approving an application", ex.getMessage());
    }

    @Test
    void resolveLoanApplication_shouldApproveAndCreateLoan() {
        Customer customer = sampleCustomer(1);
        LoanApplication existing = sampleLoanApplication(1, customer, "PENDING", null);
        AppUser evaluator = sampleUser(1);

        LoanApplicationResolutionDTO request = LoanApplicationResolutionDTO.builder()
                .status("APPROVED")
                .evaluatedById(1)
                .approvedAmount(new BigDecimal("1500.00"))
                .resolutionDetails("ok")
                .build();

        Loan savedLoan = Loan.builder()
                .id(50)
                .loanApplication(existing)
                .customer(customer)
                .approvedAmount(new BigDecimal("1500.00"))
                .outstandingBalance(new BigDecimal("1500.00"))
                .paymentStatus("CURRENT")
                .build();

        LoanApplicationDTO loanAppDto = sampleLoanAppDto(1, 1, "APPROVED", 1);
        LoanDTO loanDto = LoanDTO.builder()
                .id(50)
                .loanApplicationId(1)
                .customerId(1)
                .approvedAmount(new BigDecimal("1500.00"))
                .outstandingBalance(new BigDecimal("1500.00"))
                .paymentStatus("CURRENT")
                .build();

        when(loanApplicationRepository.findById(1)).thenReturn(Optional.of(existing));
        when(appUserRepository.findById(1)).thenReturn(Optional.of(evaluator));
        when(loanApplicationRepository.save(existing)).thenReturn(existing);
        when(loanRepository.findByLoanApplicationId(1)).thenReturn(Optional.empty());
        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);
        when(loanMapper.toDto(savedLoan)).thenReturn(loanDto);
        when(loanApplicationMapper.toDto(existing)).thenReturn(loanAppDto);

        LoanApplicationResolutionResultDTO result = loanApplicationService.resolveLoanApplication(1, request);

        assertNotNull(result.getCreatedLoan());
        assertEquals(50, result.getCreatedLoan().getId());
        assertEquals("APPROVED", result.getLoanApplication().getStatus());
    }

    @Test
    void resolveLoanApplication_shouldRejectWithoutCreatingLoan() {
        LoanApplication existing = sampleLoanApplication(1, sampleCustomer(1), "PENDING", null);
        AppUser evaluator = sampleUser(1);
        LoanApplicationResolutionDTO request = LoanApplicationResolutionDTO.builder()
                .status("REJECTED")
                .evaluatedById(1)
                .resolutionDetails("No cumple")
                .build();
        LoanApplicationDTO loanAppDto = sampleLoanAppDto(1, 1, "REJECTED", 1);

        when(loanApplicationRepository.findById(1)).thenReturn(Optional.of(existing));
        when(appUserRepository.findById(1)).thenReturn(Optional.of(evaluator));
        when(loanApplicationRepository.save(existing)).thenReturn(existing);
        when(loanApplicationMapper.toDto(existing)).thenReturn(loanAppDto);

        LoanApplicationResolutionResultDTO result = loanApplicationService.resolveLoanApplication(1, request);

        assertNull(result.getCreatedLoan());
        assertEquals("REJECTED", result.getLoanApplication().getStatus());
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void deleteLoanApplication_shouldThrowWhenNotFound() {
        when(loanApplicationRepository.existsById(8)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanApplicationService.deleteLoanApplication(8)
        );

        assertEquals("Loan Application not found with id: 8", ex.getMessage());
    }

    private static LoanApplicationDTO sampleLoanAppDto(Integer id, Integer customerId, String status, Integer evaluatedBy) {
        return LoanApplicationDTO.builder()
                .id(id)
                .customerId(customerId)
                .requestedAmount(new BigDecimal("1000.00"))
                .termInMonths(12)
                .status(status)
                .applicationDate(LocalDateTime.of(2026, 1, 1, 10, 0))
                .resolutionDetails(null)
                .evaluatedBy(evaluatedBy)
                .build();
    }

    private static LoanApplication sampleLoanApplication(Integer id, Customer customer, String status, AppUser evaluator) {
        return LoanApplication.builder()
                .id(id)
                .customer(customer)
                .requestedAmount(new BigDecimal("1000.00"))
                .termInMonths(12)
                .status(status)
                .applicationDate(LocalDateTime.of(2026, 1, 1, 10, 0))
                .resolutionDetails(null)
                .evaluatedBy(evaluator)
                .build();
    }

    private static Customer sampleCustomer(Integer id) {
        return Customer.builder().id(id).firstName("Juan").lastName("Perez").nit("1").build();
    }

    private static AppUser sampleUser(Integer id) {
        return AppUser.builder().id(id).username("user" + id).password("x").build();
    }
}
