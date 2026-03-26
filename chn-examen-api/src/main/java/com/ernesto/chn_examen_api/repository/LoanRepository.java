package com.ernesto.chn_examen_api.repository;

import com.ernesto.chn_examen_api.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {
    
    Optional<Loan> findByLoanApplicationId(Integer loanApplicationId);
    
    List<Loan> findByCustomerId(Integer customerId);
    
    List<Loan> findByPaymentStatus(String paymentStatus);
}
