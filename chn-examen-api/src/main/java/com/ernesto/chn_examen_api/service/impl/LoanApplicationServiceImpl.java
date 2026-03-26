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
import com.ernesto.chn_examen_api.service.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final AppUserRepository appUserRepository;
    private final LoanApplicationMapper loanApplicationMapper;
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;

    @Override
    @Transactional
    public LoanApplicationDTO createLoanApplication(LoanApplicationDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));

        AppUser evaluator = null;
        if (dto.getEvaluatedBy() != null) {
            evaluator = appUserRepository.findById(dto.getEvaluatedBy())
                    .orElseThrow(() -> new ResourceNotFoundException("Evaluator AppUser not found with id: " + dto.getEvaluatedBy()));
        }

        LoanApplication loanApplication = LoanApplication.builder()
                .customer(customer)
                .requestedAmount(dto.getRequestedAmount())
                .termInMonths(dto.getTermInMonths())
                .status(dto.getStatus() != null ? dto.getStatus() : "PENDING")
                .applicationDate(dto.getApplicationDate() != null ? dto.getApplicationDate() : LocalDateTime.now())
                .evaluatedBy(evaluator)
                .resolutionDetails(dto.getResolutionDetails())
                .build();

        LoanApplication savedApp = loanApplicationRepository.save(loanApplication);
        return loanApplicationMapper.toDto(savedApp);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationDTO getLoanApplicationById(Integer id) {
        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan Application not found with id: " + id));
        return loanApplicationMapper.toDto(loanApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationDTO> getAllLoanApplications() {
        return loanApplicationRepository.findAll().stream()
                .map(loanApplicationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationDTO> getLoanApplicationsByCustomerId(Integer customerId) {
        return loanApplicationRepository.findByCustomerId(customerId).stream()
                .map(loanApplicationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationDTO> getLoanApplicationsByEvaluatedBy(Integer evaluatedById) {
        return loanApplicationRepository.findByEvaluatedById(evaluatedById).stream()
                .map(loanApplicationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationDTO> getLoanApplicationsByStatus(String status) {
        return loanApplicationRepository.findByStatusIgnoreCase(status).stream()
                .map(loanApplicationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationDTO> getLoanApplicationsByCustomerAndStatus(Integer customerId, String status) {
        return loanApplicationRepository.findByCustomerIdAndStatusIgnoreCase(customerId, status).stream()
                .map(loanApplicationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LoanApplicationDTO updateLoanApplication(Integer id, LoanApplicationDTO dto) {
        LoanApplication existingApp = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan Application not found with id: " + id));

        existingApp.setRequestedAmount(dto.getRequestedAmount());
        existingApp.setTermInMonths(dto.getTermInMonths());
        existingApp.setResolutionDetails(dto.getResolutionDetails());
        
        if (dto.getStatus() != null) {
            existingApp.setStatus(dto.getStatus());
        }

        if (!existingApp.getCustomer().getId().equals(dto.getCustomerId())) {
            Customer newCustomer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
            existingApp.setCustomer(newCustomer);
        }

        if (dto.getEvaluatedBy() != null) {
            if (existingApp.getEvaluatedBy() == null || !existingApp.getEvaluatedBy().getId().equals(dto.getEvaluatedBy())) {
                AppUser newEvaluator = appUserRepository.findById(dto.getEvaluatedBy())
                        .orElseThrow(() -> new ResourceNotFoundException("Evaluator AppUser not found with id: " + dto.getEvaluatedBy()));
                existingApp.setEvaluatedBy(newEvaluator);
            }
        } else {
            existingApp.setEvaluatedBy(null);
        }

        return loanApplicationMapper.toDto(loanApplicationRepository.save(existingApp));
    }

    @Override
    @Transactional
    public void deleteLoanApplication(Integer id) {
        if (!loanApplicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Loan Application not found with id: " + id);
        }
        loanApplicationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public LoanApplicationDTO assignEvaluator(Integer loanApplicationId, Integer evaluatedById) {
        LoanApplication existingApp = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan Application not found with id: " + loanApplicationId));
                
        AppUser evaluator = appUserRepository.findById(evaluatedById)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator AppUser not found with id: " + evaluatedById));
                
        existingApp.setEvaluatedBy(evaluator);
        return loanApplicationMapper.toDto(loanApplicationRepository.save(existingApp));
    }

    @Override
    @Transactional
    public LoanApplicationDTO updateStatus(Integer loanApplicationId, String newStatus, String resolutionDetails) {
        LoanApplication existingApp = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan Application not found with id: " + loanApplicationId));
                
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        
        existingApp.setStatus(newStatus.trim().toUpperCase());
        if (resolutionDetails != null) {
            existingApp.setResolutionDetails(resolutionDetails);
        }
        
        return loanApplicationMapper.toDto(loanApplicationRepository.save(existingApp));
    }

    @Override
    @Transactional
    public LoanApplicationResolutionResultDTO resolveLoanApplication(Integer loanApplicationId, LoanApplicationResolutionDTO dto) {
        LoanApplication existingApp = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan Application not found with id: " + loanApplicationId));

        if (!"PENDING".equalsIgnoreCase(existingApp.getStatus())) {
            throw new IllegalArgumentException("Only PENDING applications can be resolved. Current status: " + existingApp.getStatus());
        }

        String normalizedStatus = dto.getStatus().trim().toUpperCase();
        if (!normalizedStatus.equals("APPROVED") && !normalizedStatus.equals("REJECTED")) {
            throw new IllegalArgumentException("Status must be either APPROVED or REJECTED");
        }

        AppUser evaluator = appUserRepository.findById(dto.getEvaluatedById())
                .orElseThrow(() -> new ResourceNotFoundException("Evaluator AppUser not found with id: " + dto.getEvaluatedById()));

        existingApp.setStatus(normalizedStatus);
        existingApp.setResolutionDetails(dto.getResolutionDetails());
        existingApp.setEvaluatedBy(evaluator);
        LoanApplication savedApp = loanApplicationRepository.save(existingApp);

        LoanDTO createdLoanDTO = null;
        if ("APPROVED".equals(normalizedStatus)) {
            if (dto.getApprovedAmount() == null) {
                throw new IllegalArgumentException("approvedAmount is required when approving an application");
            }
            if (loanRepository.findByLoanApplicationId(savedApp.getId()).isPresent()) {
                throw new IllegalArgumentException("A Loan already exists for this application");
            }
            Loan loan = Loan.builder()
                    .loanApplication(savedApp)
                    .customer(savedApp.getCustomer())
                    .approvedAmount(dto.getApprovedAmount())
                    .outstandingBalance(dto.getApprovedAmount())
                    .paymentStatus("CURRENT")
                    .build();
            Loan savedLoan = loanRepository.save(loan);
            createdLoanDTO = loanMapper.toDto(savedLoan);
        }

        return LoanApplicationResolutionResultDTO.builder()
                .loanApplication(loanApplicationMapper.toDto(savedApp))
                .createdLoan(createdLoanDTO)
                .build();
    }
}
