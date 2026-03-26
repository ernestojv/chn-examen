package com.ernesto.chn_examen_api.controller;

import com.ernesto.chn_examen_api.dto.LoanDTO;
import com.ernesto.chn_examen_api.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping
    public ResponseEntity<List<LoanDTO>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanDTO> getLoanById(@PathVariable Integer id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanDTO>> getLoansByCustomerId(@PathVariable Integer customerId) {
        return ResponseEntity.ok(loanService.getLoansByCustomerId(customerId));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<LoanDTO> getLoanByApplicationId(@PathVariable Integer applicationId) {
        return ResponseEntity.ok(loanService.getLoanByApplicationId(applicationId));
    }
}

