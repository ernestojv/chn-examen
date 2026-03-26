package com.ernesto.chn_examen_api.repository;

import com.ernesto.chn_examen_api.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    
    /**
     * Find a customer by their NIT.
     * @param nit The NIT to search for.
     * @return An Optional containing the customer if found.
     */
    Optional<Customer> findByNit(String nit);
}
