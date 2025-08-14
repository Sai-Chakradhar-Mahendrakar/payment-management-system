package miniproject2.paymentmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniproject2.paymentmanagementsystem.security.JwtUtil;
import miniproject2.paymentmanagementsystem.dto.AuthResponseDTO;
import miniproject2.paymentmanagementsystem.dto.LoginRequestDTO;
import miniproject2.paymentmanagementsystem.entity.User;
import miniproject2.paymentmanagementsystem.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthResponseDTO authenticate(LoginRequestDTO loginRequest) {
        log.info("Attempting authentication for user: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> {
                        log.error("User not found: {}", loginRequest.getEmail());
                        return new UsernameNotFoundException("User not found");
                    });

            String token = jwtUtil.generateToken(user);
            log.info("Successfully generated token for user: {}", user.getEmail());

            return new AuthResponseDTO(
                    token,
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole()
            );
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getEmail(), e);
            throw e;
        }
    }

    public void logout(String token) {
        log.info("Processing logout request");

        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // Add token to blacklist
            tokenBlacklistService.blacklistToken(token);
            log.info("Token successfully blacklisted");
        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
            throw e;
        }
    }
}
