package miniproject2.paymentmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniproject2.paymentmanagementsystem.dto.request.PaymentCreateDTO;
import miniproject2.paymentmanagementsystem.dto.response.PaymentResponseDTO;
import miniproject2.paymentmanagementsystem.dto.request.PaymentUpdateDTO;
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
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER')")
    public PaymentResponseDTO createPayment(PaymentCreateDTO paymentCreateDTO) {
        log.info("Creating payment with amount: {} and type: {}", paymentCreateDTO.getAmount(), paymentCreateDTO.getPaymentType());

        try {
            User currentUser = getCurrentUser();
            log.debug("Payment creation initiated by user: {}", currentUser.getEmail());

            Payment payment = new Payment();
            payment.setAmount(paymentCreateDTO.getAmount());
            payment.setPaymentType(paymentCreateDTO.getPaymentType());
            payment.setCategory(paymentCreateDTO.getCategory());
            payment.setStatus(paymentCreateDTO.getStatus());
            payment.setDate(paymentCreateDTO.getDate());
            payment.setDescription(paymentCreateDTO.getDescription());
            payment.setCreatedBy(currentUser);

            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment created successfully with ID: {}", savedPayment.getId());
            return convertToResponseDTO(savedPayment);
        } catch (Exception e) {
            log.error("Failed to create payment with amount: {}", paymentCreateDTO.getAmount(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER') or hasRole('VIEWER')")
    public List<PaymentResponseDTO> getAllPayments() {
        log.info("Fetching all payments");

        try {
            List<PaymentResponseDTO> payments = paymentRepository.findAll()
                    .stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            log.info("Successfully retrieved {} payments", payments.size());
            return payments;
        } catch (Exception e) {
            log.error("Failed to fetch all payments", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER') or hasRole('VIEWER')")
    public PaymentResponseDTO getPaymentById(Long id) {
        log.info("Fetching payment with ID: {}", id);

        try {
            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Payment not found with ID: {}", id);
                        return new RuntimeException("Payment not found with id: " + id);
                    });
            log.info("Successfully retrieved payment with ID: {}", id);
            return convertToResponseDTO(payment);
        } catch (Exception e) {
            log.error("Failed to fetch payment with ID: {}", id, e);
            throw e;
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER')")
    public PaymentResponseDTO updatePayment(Long id, PaymentUpdateDTO paymentUpdateDTO) {
        log.info("Updating payment with ID: {}", id);

        try {
            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Payment not found for update with ID: {}", id);
                        return new RuntimeException("Payment not found with id: " + id);
                    });

            User currentUser = getCurrentUser();

            // Only allow updates if user is ADMIN or the creator of the payment
            if (currentUser.getRole() != Role.ADMIN && !payment.getCreatedBy().getId().equals(currentUser.getId())) {
                log.warn("User {} attempted to update payment {} they didn't create", currentUser.getEmail(), id);
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
            log.info("Payment updated successfully with ID: {}", id);
            return convertToResponseDTO(updatedPayment);
        } catch (Exception e) {
            log.error("Failed to update payment with ID: {}", id, e);
            throw e;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deletePayment(Long id) {
        log.info("Deleting payment with ID: {}", id);

        try {
            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("Payment not found for deletion with ID: {}", id);
                        return new RuntimeException("Payment not found with id: " + id);
                    });

            paymentRepository.delete(payment);
            log.info("Payment deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete payment with ID: {}", id, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or hasRole('FINANCE_MANAGER') or hasRole('VIEWER')")
    public List<PaymentResponseDTO> getPaymentsByCurrentUser() {
        log.info("Fetching payments for current user");

        try {
            User currentUser = getCurrentUser();
            List<PaymentResponseDTO> payments = paymentRepository.findByCreatedBy(currentUser)
                    .stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            log.info("Successfully retrieved {} payments for user: {}", payments.size(), currentUser.getEmail());
            return payments;
        } catch (Exception e) {
            log.error("Failed to fetch payments for current user", e);
            throw e;
        }
    }

    private User getCurrentUser() {
        log.debug("Getting current authenticated user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Current user not found with email: {}", email);
                    return new RuntimeException("Current user not found");
                });
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
