package com.ernesto.chn_examen_api.controller;

import com.ernesto.chn_examen_api.dto.PaymentDTO;
import com.ernesto.chn_examen_api.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentDTO> registerPayment(@Valid @RequestBody PaymentDTO dto) {
        return new ResponseEntity<>(paymentService.registerPayment(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Integer id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByLoanId(@PathVariable Integer loanId) {
        return ResponseEntity.ok(paymentService.getPaymentsByLoanId(loanId));
    }

    @GetMapping("/registered-by/{userId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByRegisteredById(@PathVariable Integer userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByRegisteredById(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Integer id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
