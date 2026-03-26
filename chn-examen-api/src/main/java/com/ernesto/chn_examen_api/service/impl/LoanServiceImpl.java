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
import com.ernesto.chn_examen_api.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final LoanMapper loanMapper;

    @Override
    @Transactional
    public LoanDTO createLoan(LoanDTO dto) {
        if (loanRepository.findByLoanApplicationId(dto.getLoanApplicationId()).isPresent()) {
            throw new IllegalArgumentException("A loan already exists for this application");
        }

        LoanApplication application = loanApplicationRepository.findById(dto.getLoanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan Application not found with id: " + dto.getLoanApplicationId()));

        if (!"APPROVED".equalsIgnoreCase(application.getStatus())) {
            throw new IllegalArgumentException("Cannot create a loan for an application that is not APPROVED");
        }

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));

        Loan loan = Loan.builder()
                .loanApplication(application)
                .customer(customer)
                .approvedAmount(dto.getApprovedAmount())
                .outstandingBalance(dto.getOutstandingBalance())
                .paymentStatus(dto.getPaymentStatus() != null ? dto.getPaymentStatus() : "CURRENT")
                .build();

        Loan savedLoan = loanRepository.save(loan);
        return loanMapper.toDto(savedLoan);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanDTO getLoanById(Integer id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
        return loanMapper.toDto(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanDTO> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(loanMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanDTO> getLoansByCustomerId(Integer customerId) {
        return loanRepository.findByCustomerId(customerId).stream()
                .map(loanMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LoanDTO getLoanByApplicationId(Integer loanApplicationId) {
        Loan loan = loanRepository.findByLoanApplicationId(loanApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found for application id: " + loanApplicationId));
        return loanMapper.toDto(loan);
    }

    @Override
    @Transactional
    public LoanDTO updateLoan(Integer id, LoanDTO dto) {
        Loan existingLoan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));

        existingLoan.setApprovedAmount(dto.getApprovedAmount());
        existingLoan.setOutstandingBalance(dto.getOutstandingBalance());
        
        if (dto.getPaymentStatus() != null) {
            existingLoan.setPaymentStatus(dto.getPaymentStatus());
        }

        if (!existingLoan.getCustomer().getId().equals(dto.getCustomerId())) {
            Customer newCustomer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
            existingLoan.setCustomer(newCustomer);
        }

        return loanMapper.toDto(loanRepository.save(existingLoan));
    }

    @Override
    @Transactional
    public void deleteLoan(Integer id) {
        if (!loanRepository.existsById(id)) {
            throw new ResourceNotFoundException("Loan not found with id: " + id);
        }
        loanRepository.deleteById(id);
    }

    @Override
    @Transactional
    public LoanDTO updatePaymentStatus(Integer loanId, String newStatus) {
        Loan existingLoan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));
                
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        
        existingLoan.setPaymentStatus(newStatus.trim().toUpperCase());
        return loanMapper.toDto(loanRepository.save(existingLoan));
    }
}
