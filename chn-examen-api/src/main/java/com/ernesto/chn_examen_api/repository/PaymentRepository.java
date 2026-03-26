package com.ernesto.chn_examen_api.repository;

import com.ernesto.chn_examen_api.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByLoanId(Integer loanId);

    List<Payment> findByRegisteredById(Integer registeredById);
}
