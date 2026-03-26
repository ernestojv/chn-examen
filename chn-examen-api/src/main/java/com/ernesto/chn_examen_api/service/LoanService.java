package com.ernesto.chn_examen_api.service;

import com.ernesto.chn_examen_api.dto.LoanDTO;

import java.util.List;

public interface LoanService {

    LoanDTO createLoan(LoanDTO dto);

    LoanDTO getLoanById(Integer id);

    List<LoanDTO> getAllLoans();
    
    List<LoanDTO> getLoansByCustomerId(Integer customerId);
    
    LoanDTO getLoanByApplicationId(Integer loanApplicationId);

    LoanDTO updateLoan(Integer id, LoanDTO dto);

    void deleteLoan(Integer id);
    
    LoanDTO updatePaymentStatus(Integer loanId, String newStatus);
}
