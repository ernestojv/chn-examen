package com.ernesto.chn_examen_api.service;

import com.ernesto.chn_examen_api.dto.LoanApplicationDTO;
import com.ernesto.chn_examen_api.dto.LoanApplicationResolutionDTO;
import com.ernesto.chn_examen_api.dto.LoanApplicationResolutionResultDTO;

import java.util.List;

public interface LoanApplicationService {

    LoanApplicationDTO createLoanApplication(LoanApplicationDTO dto);

    LoanApplicationDTO getLoanApplicationById(Integer id);

    List<LoanApplicationDTO> getAllLoanApplications();
    
    List<LoanApplicationDTO> getLoanApplicationsByCustomerId(Integer customerId);
    
    List<LoanApplicationDTO> getLoanApplicationsByEvaluatedBy(Integer evaluatedById);

    List<LoanApplicationDTO> getLoanApplicationsByStatus(String status);

    List<LoanApplicationDTO> getLoanApplicationsByCustomerAndStatus(Integer customerId, String status);

    LoanApplicationDTO updateLoanApplication(Integer id, LoanApplicationDTO dto);

    void deleteLoanApplication(Integer id);
    
    LoanApplicationDTO assignEvaluator(Integer loanApplicationId, Integer evaluatedById);
    
    LoanApplicationDTO updateStatus(Integer loanApplicationId, String newStatus, String resolutionDetails);

    /**
     * Resolves a loan application as APPROVED or REJECTED.
     * When APPROVED, a Loan is automatically created and returned inside the response.
     */
    LoanApplicationResolutionResultDTO resolveLoanApplication(Integer loanApplicationId, LoanApplicationResolutionDTO dto);
}

