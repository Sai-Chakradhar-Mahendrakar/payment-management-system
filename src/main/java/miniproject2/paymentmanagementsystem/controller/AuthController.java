package miniproject2.paymentmanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniproject2.paymentmanagementsystem.dto.response.AuthResponseDTO;
import miniproject2.paymentmanagementsystem.dto.request.LoginRequestDTO;
import miniproject2.paymentmanagementsystem.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getEmail());
        try {
            AuthResponseDTO response = authService.authenticate(loginRequest);
            log.info("Successful login for user: {}", loginRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getEmail(), e);
            throw e;
        }
    }
}
