package miniproject2.paymentmanagementsystem.service;

import miniproject2.paymentmanagementsystem.dto.PaymentCreateDTO;
import miniproject2.paymentmanagementsystem.dto.PaymentResponseDTO;
import miniproject2.paymentmanagementsystem.dto.PaymentUpdateDTO;
import miniproject2.paymentmanagementsystem.entity.Payment;
import miniproject2.paymentmanagementsystem.entity.User;
import miniproject2.paymentmanagementsystem.enums.Category;
import miniproject2.paymentmanagementsystem.enums.PaymentType;
import miniproject2.paymentmanagementsystem.enums.Role;
import miniproject2.paymentmanagementsystem.enums.Status;
import miniproject2.paymentmanagementsystem.repository.PaymentRepository;
import miniproject2.paymentmanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PaymentService paymentService;

    private User testUser;
    private User adminUser;
    private Payment testPayment;
    private PaymentCreateDTO paymentCreateDTO;
    private PaymentUpdateDTO paymentUpdateDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.FINANCE_MANAGER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setAmount(new BigDecimal("1000.00"));
        testPayment.setPaymentType(PaymentType.INCOMING);
        testPayment.setCategory(Category.SALARY);
        testPayment.setStatus(Status.PENDING);
        testPayment.setDate(LocalDateTime.now());
        testPayment.setDescription("Test payment");
        testPayment.setCreatedBy(testUser);
        testPayment.setCreatedAt(LocalDateTime.now());
        testPayment.setUpdatedAt(LocalDateTime.now());

        paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setAmount(new BigDecimal("1500.00"));
        paymentCreateDTO.setPaymentType(PaymentType.OUTGOING);
        paymentCreateDTO.setCategory(Category.VENDOR);
        paymentCreateDTO.setStatus(Status.PROCESSING);
        paymentCreateDTO.setDate(LocalDateTime.now());
        paymentCreateDTO.setDescription("New payment");

        paymentUpdateDTO = new PaymentUpdateDTO();
        paymentUpdateDTO.setAmount(new BigDecimal("2000.00"));
        paymentUpdateDTO.setStatus(Status.COMPLETED);
    }

    @Test
    void createPayment_ShouldReturnPaymentResponseDTO_WhenValidInput() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(testUser.getEmail());
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

            // When
            PaymentResponseDTO result = paymentService.createPayment(paymentCreateDTO);

            // Then
            assertNotNull(result);
            assertEquals(testPayment.getId(), result.getId());
            assertEquals(testPayment.getAmount(), result.getAmount());
            assertEquals(testPayment.getPaymentType(), result.getPaymentType());
            assertEquals(testPayment.getCategory(), result.getCategory());
            assertEquals(testPayment.getStatus(), result.getStatus());

            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Test
    void getAllPayments_ShouldReturnListOfPaymentResponseDTO() {
        // Given
        Payment payment2 = new Payment();
        payment2.setId(2L);
        payment2.setAmount(new BigDecimal("500.00"));
        payment2.setCreatedBy(testUser);
        payment2.setCreatedAt(LocalDateTime.now());
        payment2.setUpdatedAt(LocalDateTime.now());

        List<Payment> payments = Arrays.asList(testPayment, payment2);
        when(paymentRepository.findAll()).thenReturn(payments);

        // When
        List<PaymentResponseDTO> result = paymentService.getAllPayments();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testPayment.getId(), result.get(0).getId());
        assertEquals(payment2.getId(), result.get(1).getId());

        verify(paymentRepository).findAll();
    }

    @Test
    void getPaymentById_ShouldReturnPaymentResponseDTO_WhenPaymentExists() {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        // When
        PaymentResponseDTO result = paymentService.getPaymentById(paymentId);

        // Then
        assertNotNull(result);
        assertEquals(testPayment.getId(), result.getId());
        assertEquals(testPayment.getAmount(), result.getAmount());
        assertEquals(testPayment.getCreatedBy().getId(), result.getCreatedById());

        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void getPaymentById_ShouldThrowRuntimeException_WhenPaymentNotExists() {
        // Given
        Long paymentId = 999L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.getPaymentById(paymentId);
        });

        assertEquals("Payment not found with id: " + paymentId, exception.getMessage());
        verify(paymentRepository).findById(paymentId);
    }

    @Test
    void updatePayment_ShouldReturnUpdatedPaymentResponseDTO_WhenUserIsCreator() {
        // Given
        Long paymentId = 1L;
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(testUser.getEmail());
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

            // When
            PaymentResponseDTO result = paymentService.updatePayment(paymentId, paymentUpdateDTO);

            // Then
            assertNotNull(result);
            verify(paymentRepository).findById(paymentId);
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Test
    void updatePayment_ShouldAllowUpdate_WhenUserIsAdmin() {
        // Given
        Long paymentId = 1L;
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(adminUser.getEmail());
            when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));
            when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

            // When
            PaymentResponseDTO result = paymentService.updatePayment(paymentId, paymentUpdateDTO);

            // Then
            assertNotNull(result);
            verify(paymentRepository).findById(paymentId);
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Test
    void updatePayment_ShouldThrowRuntimeException_WhenUserIsNotCreatorOrAdmin() {
        // Given
        Long paymentId = 1L;
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setEmail("other@example.com");
        otherUser.setRole(Role.FINANCE_MANAGER);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(otherUser.getEmail());
            when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                paymentService.updatePayment(paymentId, paymentUpdateDTO);
            });

            assertEquals("You can only update payments you created", exception.getMessage());
            verify(paymentRepository).findById(paymentId);
            verify(paymentRepository, never()).save(any(Payment.class));
        }
    }

    @Test
    void deletePayment_ShouldDeletePayment_WhenPaymentExists() {
        // Given
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        // When
        paymentService.deletePayment(paymentId);

        // Then
        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository).delete(testPayment);
    }

    @Test
    void deletePayment_ShouldThrowRuntimeException_WhenPaymentNotExists() {
        // Given
        Long paymentId = 999L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.deletePayment(paymentId);
        });

        assertEquals("Payment not found with id: " + paymentId, exception.getMessage());
        verify(paymentRepository).findById(paymentId);
        verify(paymentRepository, never()).delete(any(Payment.class));
    }

    @Test
    void getPaymentsByCurrentUser_ShouldReturnUserPayments() {
        // Given
        List<Payment> userPayments = Arrays.asList(testPayment);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(testUser.getEmail());
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
            when(paymentRepository.findByCreatedBy(testUser)).thenReturn(userPayments);

            // When
            List<PaymentResponseDTO> result = paymentService.getPaymentsByCurrentUser();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testPayment.getId(), result.get(0).getId());

            verify(paymentRepository).findByCreatedBy(testUser);
        }
    }
}
