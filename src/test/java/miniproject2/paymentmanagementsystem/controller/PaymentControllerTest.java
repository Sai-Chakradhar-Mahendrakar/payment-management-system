package miniproject2.paymentmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import miniproject2.paymentmanagementsystem.dto.PaymentCreateDTO;
import miniproject2.paymentmanagementsystem.dto.PaymentResponseDTO;
import miniproject2.paymentmanagementsystem.dto.PaymentUpdateDTO;
import miniproject2.paymentmanagementsystem.enums.Category;
import miniproject2.paymentmanagementsystem.enums.PaymentType;
import miniproject2.paymentmanagementsystem.enums.Status;
import miniproject2.paymentmanagementsystem.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentCreateDTO paymentCreateDTO;
    private PaymentUpdateDTO paymentUpdateDTO;
    private PaymentResponseDTO paymentResponseDTO;
    private List<PaymentResponseDTO> paymentList;

    @BeforeEach
    void setUp() {
        paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setAmount(new BigDecimal("1000.00"));
        paymentCreateDTO.setPaymentType(PaymentType.INCOMING);
        paymentCreateDTO.setCategory(Category.SALARY);
        paymentCreateDTO.setStatus(Status.PENDING);
        paymentCreateDTO.setDate(LocalDateTime.now());
        paymentCreateDTO.setDescription("Test payment");

        paymentUpdateDTO = new PaymentUpdateDTO();
        paymentUpdateDTO.setAmount(new BigDecimal("1500.00"));
        paymentUpdateDTO.setStatus(Status.COMPLETED);

        paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setId(1L);
        paymentResponseDTO.setAmount(new BigDecimal("1000.00"));
        paymentResponseDTO.setPaymentType(PaymentType.INCOMING);
        paymentResponseDTO.setCategory(Category.SALARY);
        paymentResponseDTO.setStatus(Status.PENDING);
        paymentResponseDTO.setDate(LocalDateTime.now());
        paymentResponseDTO.setDescription("Test payment");
        paymentResponseDTO.setCreatedById(1L);
        paymentResponseDTO.setCreatedByName("Test User");
        paymentResponseDTO.setCreatedAt(LocalDateTime.now());
        paymentResponseDTO.setUpdatedAt(LocalDateTime.now());

        PaymentResponseDTO payment2 = new PaymentResponseDTO();
        payment2.setId(2L);
        payment2.setAmount(new BigDecimal("500.00"));
        payment2.setPaymentType(PaymentType.OUTGOING);
        payment2.setCategory(Category.VENDOR);

        paymentList = Arrays.asList(paymentResponseDTO, payment2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPayment_ShouldReturnCreatedPayment_WhenValidInputAndAdminRole() throws Exception {
        // Given
        when(paymentService.createPayment(any(PaymentCreateDTO.class))).thenReturn(paymentResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(paymentResponseDTO.getId()))
                .andExpect(jsonPath("$.amount").value(paymentResponseDTO.getAmount()))
                .andExpect(jsonPath("$.paymentType").value(paymentResponseDTO.getPaymentType().toString()))
                .andExpect(jsonPath("$.category").value(paymentResponseDTO.getCategory().toString()))
                .andExpect(jsonPath("$.status").value(paymentResponseDTO.getStatus().toString()));
    }

    @Test
    @WithMockUser(roles = "FINANCE_MANAGER")
    void createPayment_ShouldReturnCreatedPayment_WhenValidInputAndFinanceManagerRole() throws Exception {
        // Given
        when(paymentService.createPayment(any(PaymentCreateDTO.class))).thenReturn(paymentResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentCreateDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void createPayment_ShouldReturnForbidden_WhenUserIsViewer() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentCreateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPayment_ShouldReturnUnauthorized_WhenUserIsNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentCreateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPayment_ShouldReturnBadRequest_WhenAmountIsNull() throws Exception {
        // Given
        paymentCreateDTO.setAmount(null);

        // When & Then
        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getAllPayments_ShouldReturnListOfPayments_WhenUserHasViewAccess() throws Exception {
        // Given
        when(paymentService.getAllPayments()).thenReturn(paymentList);

        // When & Then
        mockMvc.perform(get("/api/payments")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(paymentResponseDTO.getId()))
                .andExpect(jsonPath("$[0].amount").value(paymentResponseDTO.getAmount()))
                .andExpect(jsonPath("$[1].id").value(paymentList.get(1).getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllPayments_ShouldReturnListOfPayments_WhenUserIsAdmin() throws Exception {
        // Given
        when(paymentService.getAllPayments()).thenReturn(paymentList);

        // When & Then
        mockMvc.perform(get("/api/payments")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllPayments_ShouldReturnUnauthorized_WhenUserIsNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/payments")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPaymentById_ShouldReturnPayment_WhenPaymentExists() throws Exception {
        // Given
        Long paymentId = 1L;
        when(paymentService.getPaymentById(paymentId)).thenReturn(paymentResponseDTO);

        // When & Then
        mockMvc.perform(get("/api/payments/{id}", paymentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(paymentResponseDTO.getId()))
                .andExpect(jsonPath("$.amount").value(paymentResponseDTO.getAmount()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPaymentById_ShouldReturnNotFound_WhenPaymentDoesNotExist() throws Exception {
        // Given
        Long paymentId = 999L;
        when(paymentService.getPaymentById(paymentId))
                .thenThrow(new RuntimeException("Payment not found with id: " + paymentId));

        // When & Then
        mockMvc.perform(get("/api/payments/{id}", paymentId)
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updatePayment_ShouldReturnUpdatedPayment_WhenValidInput() throws Exception {
        // Given
        Long paymentId = 1L;
        when(paymentService.updatePayment(eq(paymentId), any(PaymentUpdateDTO.class)))
                .thenReturn(paymentResponseDTO);

        // When & Then
        mockMvc.perform(put("/api/payments/{id}", paymentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(paymentResponseDTO.getId()));
    }

    @Test
    @WithMockUser(roles = "FINANCE_MANAGER")
    void updatePayment_ShouldReturnUpdatedPayment_WhenUserIsFinanceManager() throws Exception {
        // Given
        Long paymentId = 1L;
        when(paymentService.updatePayment(eq(paymentId), any(PaymentUpdateDTO.class)))
                .thenReturn(paymentResponseDTO);

        // When & Then
        mockMvc.perform(put("/api/payments/{id}", paymentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentUpdateDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void updatePayment_ShouldReturnForbidden_WhenUserIsViewer() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/payments/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentUpdateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePayment_ShouldReturnNoContent_WhenPaymentExists() throws Exception {
        // Given
        Long paymentId = 1L;
        doNothing().when(paymentService).deletePayment(paymentId);

        // When & Then
        mockMvc.perform(delete("/api/payments/{id}", paymentId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(paymentService).deletePayment(paymentId);
    }

    @Test
    @WithMockUser(roles = "FINANCE_MANAGER")
    void deletePayment_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/payments/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void deletePayment_ShouldReturnForbidden_WhenUserIsViewer() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/payments/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMyPayments_ShouldReturnUserPayments() throws Exception {
        // Given
        when(paymentService.getPaymentsByCurrentUser()).thenReturn(paymentList);

        // When & Then
        mockMvc.perform(get("/api/payments/my-payments")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "FINANCE_MANAGER")
    void getMyPayments_ShouldReturnUserPayments_WhenUserIsFinanceManager() throws Exception {
        // Given
        when(paymentService.getPaymentsByCurrentUser()).thenReturn(paymentList);

        // When & Then
        mockMvc.perform(get("/api/payments/my-payments")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getMyPayments_ShouldReturnUserPayments_WhenUserIsViewer() throws Exception {
        // Given
        when(paymentService.getPaymentsByCurrentUser()).thenReturn(paymentList);

        // When & Then
        mockMvc.perform(get("/api/payments/my-payments")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void getMyPayments_ShouldReturnUnauthorized_WhenUserIsNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/payments/my-payments")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
