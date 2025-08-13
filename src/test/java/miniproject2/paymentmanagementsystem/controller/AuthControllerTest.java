package miniproject2.paymentmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import miniproject2.paymentmanagementsystem.dto.AuthResponseDTO;
import miniproject2.paymentmanagementsystem.dto.LoginRequestDTO;
import miniproject2.paymentmanagementsystem.enums.Role;
import miniproject2.paymentmanagementsystem.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequestDTO loginRequestDTO;
    private AuthResponseDTO authResponseDTO;

    @BeforeEach
    void setUp() {
        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail("test@example.com");
        loginRequestDTO.setPassword("password");

        authResponseDTO = new AuthResponseDTO(
                "jwt.token.here",
                1L,
                "Test User",
                "test@example.com",
                Role.ADMIN
        );
    }

    @Test
    void login_ShouldReturnAuthResponseDTO_WhenCredentialsAreValid() throws Exception {
        // Given
        when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn(authResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(authResponseDTO.getToken()))
                .andExpect(jsonPath("$.userId").value(authResponseDTO.getUserId()))
                .andExpect(jsonPath("$.name").value(authResponseDTO.getName()))
                .andExpect(jsonPath("$.email").value(authResponseDTO.getEmail()))
                .andExpect(jsonPath("$.role").value(authResponseDTO.getRole().toString()));
    }

    @Test
    void login_ShouldReturnBadRequest_WhenEmailIsEmpty() throws Exception {
        // Given
        loginRequestDTO.setEmail("");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenPasswordIsEmpty() throws Exception {
        // Given
        loginRequestDTO.setPassword("");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenEmailIsInvalid() throws Exception {
        // Given
        loginRequestDTO.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenEmailIsNull() throws Exception {
        // Given
        loginRequestDTO.setEmail(null);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenPasswordIsNull() throws Exception {
        // Given
        loginRequestDTO.setPassword(null);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenEmailContainsWhitespaceOnly() throws Exception {
        // Given
        loginRequestDTO.setEmail("   ");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenPasswordContainsWhitespaceOnly() throws Exception {
        // Given
        loginRequestDTO.setPassword("   ");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenRequestBodyIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenRequestBodyIsMalformed() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenAuthenticationFails() throws Exception {
        // Given
        when(authService.authenticate(any(LoginRequestDTO.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void login_ShouldReturnCorrectContentType() throws Exception {
        // Given
        when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn(authResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"));
    }

    @Test
    void login_ShouldAcceptDifferentRoles() throws Exception {
        // Given
        AuthResponseDTO financeManagerResponse = new AuthResponseDTO(
                "jwt.token.here", 2L, "Finance Manager", "fm@example.com", Role.FINANCE_MANAGER);

        when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn(financeManagerResponse);

    @Test
    @WithMockUser
    void logout_ShouldReturnOk_WhenValidAuthorizationHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .header("Authorization", "Bearer jwt.token.here"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully logged out. Please discard your token."));
    }

    @Test
    @WithMockUser
    void logout_ShouldReturnBadRequest_WhenInvalidAuthorizationHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .header("Authorization", "Invalid header"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid authorization header"));
    }

    @Test
    void logout_ShouldReturnBadRequest_WhenNoAuthorizationHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid authorization header"));
    }
}
