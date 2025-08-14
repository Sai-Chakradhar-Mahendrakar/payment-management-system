package miniproject2.paymentmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniproject2.paymentmanagementsystem.dto.PaymentCreateDTO;
import miniproject2.paymentmanagementsystem.dto.PaymentResponseDTO;
import miniproject2.paymentmanagementsystem.dto.PaymentUpdateDTO;
import miniproject2.paymentmanagementsystem.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER')")
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentCreateDTO paymentCreateDTO) {
        log.info("Creating payment for amount: {} with type: {}", paymentCreateDTO.getAmount(), paymentCreateDTO.getPaymentType());
        try {
            PaymentResponseDTO response = paymentService.createPayment(paymentCreateDTO);
            log.info("Payment created successfully with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Failed to create payment for amount: {}", paymentCreateDTO.getAmount(), e);
            throw e;
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER') or hasRole('VIEWER')")
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        log.info("Fetching all payments");
        try {
            List<PaymentResponseDTO> payments = paymentService.getAllPayments();
            log.info("Retrieved {} payments", payments.size());
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Failed to fetch all payments", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER') or hasRole('VIEWER')")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
        log.info("Fetching payment with ID: {}", id);
        try {
            PaymentResponseDTO payment = paymentService.getPaymentById(id);
            log.info("Payment found with ID: {}", id);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            log.error("Failed to fetch payment with ID: {}", id, e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER')")
    public ResponseEntity<PaymentResponseDTO> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentUpdateDTO paymentUpdateDTO) {
        log.info("Updating payment with ID: {}", id);
        try {
            PaymentResponseDTO response = paymentService.updatePayment(id, paymentUpdateDTO);
            log.info("Payment updated successfully with ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update payment with ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        log.info("Deleting payment with ID: {}", id);
        try {
            paymentService.deletePayment(id);
            log.info("Payment deleted successfully with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete payment with ID: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/my-payments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER') or hasRole('VIEWER')")
    public ResponseEntity<List<PaymentResponseDTO>> getMyPayments() {
        log.info("Fetching payments for current user");
        try {
            List<PaymentResponseDTO> payments = paymentService.getPaymentsByCurrentUser();
            log.info("Retrieved {} payments for current user", payments.size());
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            log.error("Failed to fetch payments for current user", e);
            throw e;
        }
    }
}
