package miniproject2.paymentmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import miniproject2.paymentmanagementsystem.dto.PaymentCreateDTO;
import miniproject2.paymentmanagementsystem.dto.PaymentResponseDTO;
import miniproject2.paymentmanagementsystem.dto.PaymentUpdateDTO;
import miniproject2.paymentmanagementsystem.entity.Payment;
import miniproject2.paymentmanagementsystem.entity.User;
import miniproject2.paymentmanagementsystem.enums.Role;
import miniproject2.paymentmanagementsystem.repository.PaymentRepository;
import miniproject2.paymentmanagementsystem.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER')")
    public PaymentResponseDTO createPayment(PaymentCreateDTO paymentCreateDTO) {
        User currentUser = getCurrentUser();

        Payment payment = new Payment();
        payment.setAmount(paymentCreateDTO.getAmount());
        payment.setPaymentType(paymentCreateDTO.getPaymentType());
        payment.setCategory(paymentCreateDTO.getCategory());
        payment.setStatus(paymentCreateDTO.getStatus());
        payment.setDate(paymentCreateDTO.getDate());
        payment.setDescription(paymentCreateDTO.getDescription());
        payment.setCreatedBy(currentUser);

        Payment savedPayment = paymentRepository.save(payment);
        return convertToResponseDTO(savedPayment);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER') or hasRole('VIEWER')")
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER') or hasRole('VIEWER')")
    public PaymentResponseDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        return convertToResponseDTO(payment);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER')")
    public PaymentResponseDTO updatePayment(Long id, PaymentUpdateDTO paymentUpdateDTO) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));

        User currentUser = getCurrentUser();

        // Only allow updates if user is ADMIN or the creator of the payment
        if (currentUser.getRole() != Role.ADMIN && !payment.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update payments you created");
        }

        if (paymentUpdateDTO.getAmount() != null) {
            payment.setAmount(paymentUpdateDTO.getAmount());
        }
        if (paymentUpdateDTO.getPaymentType() != null) {
            payment.setPaymentType(paymentUpdateDTO.getPaymentType());
        }
        if (paymentUpdateDTO.getCategory() != null) {
            payment.setCategory(paymentUpdateDTO.getCategory());
        }
        if (paymentUpdateDTO.getStatus() != null) {
            payment.setStatus(paymentUpdateDTO.getStatus());
        }
        if (paymentUpdateDTO.getDate() != null) {
            payment.setDate(paymentUpdateDTO.getDate());
        }
        if (paymentUpdateDTO.getDescription() != null) {
            payment.setDescription(paymentUpdateDTO.getDescription());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return convertToResponseDTO(updatedPayment);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));

        paymentRepository.delete(payment);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER') or hasRole('VIEWER')")
    public List<PaymentResponseDTO> getPaymentsByCurrentUser() {
        User currentUser = getCurrentUser();
        return paymentRepository.findByCreatedBy(currentUser)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    private PaymentResponseDTO convertToResponseDTO(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentType(payment.getPaymentType());
        dto.setCategory(payment.getCategory());
        dto.setStatus(payment.getStatus());
        dto.setDate(payment.getDate());
        dto.setDescription(payment.getDescription());
        dto.setCreatedById(payment.getCreatedBy().getId());
        dto.setCreatedByName(payment.getCreatedBy().getName());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        return dto;
    }
}
