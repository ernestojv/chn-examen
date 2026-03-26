package com.ernesto.chn_examen_api.repository;

import com.ernesto.chn_examen_api.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Integer> {
    
    List<LoanApplication> findByCustomerId(Integer customerId);
    
    List<LoanApplication> findByEvaluatedById(Integer evaluatedById);

    List<LoanApplication> findByStatusIgnoreCase(String status);

    List<LoanApplication> findByCustomerIdAndStatusIgnoreCase(Integer customerId, String status);
}
