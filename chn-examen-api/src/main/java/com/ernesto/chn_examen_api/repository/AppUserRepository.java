package com.ernesto.chn_examen_api.repository;

import com.ernesto.chn_examen_api.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Integer> {

    Optional<AppUser> findByUsername(String username);
    
    boolean existsByEmployeeId(Integer employeeId);
}
