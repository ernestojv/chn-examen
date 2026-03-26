package com.ernesto.chn_examen_api.service.impl;

import com.ernesto.chn_examen_api.dto.LoanDTO;
import com.ernesto.chn_examen_api.entity.Customer;
import com.ernesto.chn_examen_api.entity.Loan;
import com.ernesto.chn_examen_api.entity.LoanApplication;
import com.ernesto.chn_examen_api.exception.ResourceNotFoundException;
import com.ernesto.chn_examen_api.mapper.LoanMapper;
import com.ernesto.chn_examen_api.repository.CustomerRepository;
import com.ernesto.chn_examen_api.repository.LoanApplicationRepository;
import com.ernesto.chn_examen_api.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanMapper loanMapper;

    @InjectMocks
    private LoanServiceImpl loanService;

    @Test
    void createLoan_shouldThrowWhenLoanAlreadyExistsForApplication() {
        LoanDTO request = sampleLoanDto(null, 10, 1, "CURRENT");

        when(loanRepository.findByLoanApplicationId(10)).thenReturn(Optional.of(new Loan()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanService.createLoan(request)
        );

        assertEquals("A loan already exists for this application", ex.getMessage());
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void createLoan_shouldThrowWhenApplicationNotFound() {
        LoanDTO request = sampleLoanDto(null, 10, 1, "CURRENT");

        when(loanRepository.findByLoanApplicationId(10)).thenReturn(Optional.empty());
        when(loanApplicationRepository.findById(10)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.createLoan(request)
        );

        assertEquals("Loan Application not found with id: 10", ex.getMessage());
    }

    @Test
    void createLoan_shouldThrowWhenApplicationNotApproved() {
        LoanDTO request = sampleLoanDto(null, 10, 1, "CURRENT");
        LoanApplication application = sampleLoanApplication(10, "PENDING");

        when(loanRepository.findByLoanApplicationId(10)).thenReturn(Optional.empty());
        when(loanApplicationRepository.findById(10)).thenReturn(Optional.of(application));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanService.createLoan(request)
        );

        assertEquals("Cannot create a loan for an application that is not APPROVED", ex.getMessage());
    }

    @Test
    void createLoan_shouldThrowWhenCustomerNotFound() {
        LoanDTO request = sampleLoanDto(null, 10, 1, "CURRENT");
        LoanApplication application = sampleLoanApplication(10, "APPROVED");

        when(loanRepository.findByLoanApplicationId(10)).thenReturn(Optional.empty());
        when(loanApplicationRepository.findById(10)).thenReturn(Optional.of(application));
        when(customerRepository.findById(1)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.createLoan(request)
        );

        assertEquals("Customer not found with id: 1", ex.getMessage());
    }

    @Test
    void createLoan_shouldPersistAndReturnDto() {
        LoanDTO request = sampleLoanDto(null, 10, 1, null);
        LoanApplication application = sampleLoanApplication(10, "APPROVED");
        Customer customer = sampleCustomer(1);
        Loan savedLoan = sampleLoanEntity(1, application, customer, "CURRENT");
        LoanDTO expected = sampleLoanDto(1, 10, 1, "CURRENT");

        when(loanRepository.findByLoanApplicationId(10)).thenReturn(Optional.empty());
        when(loanApplicationRepository.findById(10)).thenReturn(Optional.of(application));
        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);
        when(loanMapper.toDto(savedLoan)).thenReturn(expected);

        LoanDTO result = loanService.createLoan(request);

        assertEquals(1, result.getId());
        assertEquals("CURRENT", result.getPaymentStatus());
    }

    @Test
    void getLoanById_shouldThrowWhenNotFound() {
        when(loanRepository.findById(55)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.getLoanById(55)
        );

        assertEquals("Loan not found with id: 55", ex.getMessage());
    }

    @Test
    void getAllLoans_shouldReturnMappedList() {
        Loan loan = sampleLoanEntity(1, sampleLoanApplication(10, "APPROVED"), sampleCustomer(1), "CURRENT");
        LoanDTO dto = sampleLoanDto(1, 10, 1, "CURRENT");

        when(loanRepository.findAll()).thenReturn(List.of(loan));
        when(loanMapper.toDto(loan)).thenReturn(dto);

        List<LoanDTO> result = loanService.getAllLoans();

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getId());
    }

    @Test
    void getLoanByApplicationId_shouldThrowWhenNotFound() {
        when(loanRepository.findByLoanApplicationId(10)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.getLoanByApplicationId(10)
        );

        assertEquals("Loan not found for application id: 10", ex.getMessage());
    }

    @Test
    void updateLoan_shouldThrowWhenNotFound() {
        when(loanRepository.findById(20)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.updateLoan(20, sampleLoanDto(null, 10, 1, "CURRENT"))
        );

        assertEquals("Loan not found with id: 20", ex.getMessage());
    }

    @Test
    void updateLoan_shouldThrowWhenReassignCustomerNotFound() {
        Loan existing = sampleLoanEntity(1, sampleLoanApplication(10, "APPROVED"), sampleCustomer(1), "CURRENT");
        LoanDTO update = sampleLoanDto(1, 10, 2, "OVERDUE");

        when(loanRepository.findById(1)).thenReturn(Optional.of(existing));
        when(customerRepository.findById(2)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.updateLoan(1, update)
        );

        assertEquals("Customer not found with id: 2", ex.getMessage());
    }

    @Test
    void updateLoan_shouldUpdateAndReturnDto() {
        Loan existing = sampleLoanEntity(1, sampleLoanApplication(10, "APPROVED"), sampleCustomer(1), "CURRENT");
        Customer newCustomer = sampleCustomer(2);
        LoanDTO update = sampleLoanDto(1, 10, 2, "OVERDUE");
        LoanDTO expected = sampleLoanDto(1, 10, 2, "OVERDUE");

        when(loanRepository.findById(1)).thenReturn(Optional.of(existing));
        when(customerRepository.findById(2)).thenReturn(Optional.of(newCustomer));
        when(loanRepository.save(existing)).thenReturn(existing);
        when(loanMapper.toDto(existing)).thenReturn(expected);

        LoanDTO result = loanService.updateLoan(1, update);

        assertEquals("OVERDUE", result.getPaymentStatus());
        assertEquals(2, existing.getCustomer().getId());
    }

    @Test
    void updatePaymentStatus_shouldThrowWhenStatusEmpty() {
        Loan existing = sampleLoanEntity(1, sampleLoanApplication(10, "APPROVED"), sampleCustomer(1), "CURRENT");
        when(loanRepository.findById(1)).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> loanService.updatePaymentStatus(1, " ")
        );

        assertEquals("Status cannot be empty", ex.getMessage());
    }

    @Test
    void updatePaymentStatus_shouldNormalizeAndPersist() {
        Loan existing = sampleLoanEntity(1, sampleLoanApplication(10, "APPROVED"), sampleCustomer(1), "CURRENT");
        LoanDTO expected = sampleLoanDto(1, 10, 1, "PAID");

        when(loanRepository.findById(1)).thenReturn(Optional.of(existing));
        when(loanRepository.save(existing)).thenReturn(existing);
        when(loanMapper.toDto(existing)).thenReturn(expected);

        LoanDTO result = loanService.updatePaymentStatus(1, " paid ");

        assertEquals("PAID", result.getPaymentStatus());
    }

    @Test
    void deleteLoan_shouldThrowWhenNotFound() {
        when(loanRepository.existsById(40)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.deleteLoan(40)
        );

        assertEquals("Loan not found with id: 40", ex.getMessage());
    }

    @Test
    void deleteLoan_shouldDeleteWhenFound() {
        when(loanRepository.existsById(40)).thenReturn(true);

        loanService.deleteLoan(40);

        verify(loanRepository).deleteById(40);
    }

    private static LoanDTO sampleLoanDto(Integer id, Integer appId, Integer customerId, String status) {
        return LoanDTO.builder()
                .id(id)
                .loanApplicationId(appId)
                .customerId(customerId)
                .approvedAmount(new BigDecimal("5000.00"))
                .outstandingBalance(new BigDecimal("5000.00"))
                .paymentStatus(status)
                .build();
    }

    private static LoanApplication sampleLoanApplication(Integer id, String status) {
        return LoanApplication.builder()
                .id(id)
                .status(status)
                .build();
    }

    private static Customer sampleCustomer(Integer id) {
        return Customer.builder()
                .id(id)
                .firstName("Juan")
                .lastName("Perez")
                .nit("NIT" + id)
                .build();
    }

    private static Loan sampleLoanEntity(Integer id, LoanApplication app, Customer customer, String status) {
        return Loan.builder()
                .id(id)
                .loanApplication(app)
                .customer(customer)
                .approvedAmount(new BigDecimal("5000.00"))
                .outstandingBalance(new BigDecimal("5000.00"))
                .paymentStatus(status)
                .build();
    }
}
