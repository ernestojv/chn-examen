package com.ernesto.chn_examen_api.controller;

import com.ernesto.chn_examen_api.dto.LoanApplicationDTO;
import com.ernesto.chn_examen_api.dto.LoanApplicationResolutionDTO;
import com.ernesto.chn_examen_api.dto.LoanApplicationResolutionResultDTO;
import com.ernesto.chn_examen_api.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/loan-applications")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping
    public ResponseEntity<LoanApplicationDTO> createLoanApplication(@Valid @RequestBody LoanApplicationDTO dto) {
        return new ResponseEntity<>(loanApplicationService.createLoanApplication(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LoanApplicationDTO>> getAllLoanApplications() {
        return ResponseEntity.ok(loanApplicationService.getAllLoanApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanApplicationDTO> getLoanApplicationById(@PathVariable Integer id) {
        return ResponseEntity.ok(loanApplicationService.getLoanApplicationById(id));
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanApplicationDTO>> getLoanApplicationsByCustomerId(@PathVariable Integer customerId) {
        return ResponseEntity.ok(loanApplicationService.getLoanApplicationsByCustomerId(customerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LoanApplicationDTO>> getLoanApplicationsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(loanApplicationService.getLoanApplicationsByStatus(status));
    }

    @GetMapping("/customer/{customerId}/status/{status}")
    public ResponseEntity<List<LoanApplicationDTO>> getLoanApplicationsByCustomerAndStatus(
            @PathVariable Integer customerId, @PathVariable String status) {
        return ResponseEntity.ok(loanApplicationService.getLoanApplicationsByCustomerAndStatus(customerId, status));
    }
    
    @GetMapping("/evaluator/{evaluatorId}")
    public ResponseEntity<List<LoanApplicationDTO>> getLoanApplicationsByEvaluatorId(@PathVariable Integer evaluatorId) {
        return ResponseEntity.ok(loanApplicationService.getLoanApplicationsByEvaluatedBy(evaluatorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanApplicationDTO> updateLoanApplication(@PathVariable Integer id, @Valid @RequestBody LoanApplicationDTO dto) {
        return ResponseEntity.ok(loanApplicationService.updateLoanApplication(id, dto));
    }

    @PutMapping("/{id}/assign-evaluator/{evaluatorId}")
    public ResponseEntity<LoanApplicationDTO> assignEvaluator(@PathVariable Integer id, @PathVariable Integer evaluatorId) {
        return ResponseEntity.ok(loanApplicationService.assignEvaluator(id, evaluatorId));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<LoanApplicationDTO> updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(loanApplicationService.updateStatus(id, payload.get("status"), payload.get("resolutionDetails")));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<LoanApplicationResolutionResultDTO> resolveLoanApplication(
            @PathVariable Integer id,
            @Valid @RequestBody LoanApplicationResolutionDTO dto) {
        return ResponseEntity.ok(loanApplicationService.resolveLoanApplication(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoanApplication(@PathVariable Integer id) {
        loanApplicationService.deleteLoanApplication(id);
        return ResponseEntity.noContent().build();
    }
}
