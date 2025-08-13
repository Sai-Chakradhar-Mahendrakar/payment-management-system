package miniproject2.paymentmanagementsystem.service;

import miniproject2.paymentmanagementsystem.dto.AuthResponseDTO;
import miniproject2.paymentmanagementsystem.dto.LoginRequestDTO;
import miniproject2.paymentmanagementsystem.entity.User;
import miniproject2.paymentmanagementsystem.enums.Role;
import miniproject2.paymentmanagementsystem.repository.UserRepository;
import miniproject2.paymentmanagementsystem.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.ADMIN);

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");
    }

    @Test
    void authenticate_ShouldReturnAuthResponseDTO_WhenCredentialsAreValid() {
        // Given
        String expectedToken = "jwt.token.here";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(testUser)).thenReturn(expectedToken);

        // When
        AuthResponseDTO result = authService.authenticate(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getRole(), result.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(jwtUtil).generateToken(testUser);
    }

    @Test
    void authenticate_ShouldThrowUsernameNotFoundException_WhenUserNotFound() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            authService.authenticate(loginRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void logout_ShouldBlacklistToken_WhenTokenHasBearerPrefix() {
        // Given
        String tokenWithPrefix = "Bearer jwt.token.here";
        String expectedToken = "jwt.token.here";

        // When
        authService.logout(tokenWithPrefix);

        // Then
        verify(tokenBlacklistService).blacklistToken(expectedToken);
    }

    @Test
    void logout_ShouldBlacklistToken_WhenTokenHasNoBearerPrefix() {
        // Given
        String token = "jwt.token.here";

        // When
        authService.logout(token);

        // Then
        verify(tokenBlacklistService).blacklistToken(token);
    }

    @Test
    void authenticate_ShouldUseCorrectEmailAndPassword() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(testUser)).thenReturn("token");

        // When
        authService.authenticate(loginRequest);

        // Then
        verify(authenticationManager).authenticate(
                argThat(auth -> auth.getPrincipal().equals(loginRequest.getEmail()) &&
                               auth.getCredentials().equals(loginRequest.getPassword()))
        );
    }
}
