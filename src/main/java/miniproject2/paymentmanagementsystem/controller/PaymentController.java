package miniproject2.paymentmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER')")
    public ResponseEntity<PaymentResponseDTO> createPayment(@Valid @RequestBody PaymentCreateDTO paymentCreateDTO) {
        PaymentResponseDTO response = paymentService.createPayment(paymentCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER') or hasRole('VIEWER')")
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        List<PaymentResponseDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER') or hasRole('VIEWER')")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
        PaymentResponseDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER')")
    public ResponseEntity<PaymentResponseDTO> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentUpdateDTO paymentUpdateDTO) {
        PaymentResponseDTO response = paymentService.updatePayment(id, paymentUpdateDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-payments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER') or hasRole('VIEWER')")
    public ResponseEntity<List<PaymentResponseDTO>> getMyPayments() {
        List<PaymentResponseDTO> payments = paymentService.getPaymentsByCurrentUser();
        return ResponseEntity.ok(payments);
    }
}
