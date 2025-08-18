package miniproject2.paymentmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniproject2.paymentmanagementsystem.dto.request.UserCreateDTO;
import miniproject2.paymentmanagementsystem.dto.response.UserResponseDTO;
import miniproject2.paymentmanagementsystem.entity.User;
import miniproject2.paymentmanagementsystem.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
    }

    public UserResponseDTO createUser(UserCreateDTO userCreateDTO) {
        log.info("Creating new user with email: {}", userCreateDTO.getEmail());

        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            log.error("User already exists with email: {}", userCreateDTO.getEmail());
            throw new RuntimeException("User already exists with email: " + userCreateDTO.getEmail());
        }

        try {
            User user = new User();
            user.setName(userCreateDTO.getName());
            user.setEmail(userCreateDTO.getEmail());
            user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
            user.setRole(userCreateDTO.getRole());

            User savedUser = userRepository.save(user);
            log.info("User created successfully with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());
            return convertToResponseDTO(savedUser);
        } catch (Exception e) {
            log.error("Failed to create user with email: {}", userCreateDTO.getEmail(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.info("Fetching all users");
        try {
            List<UserResponseDTO> users = userRepository.findAll()
                    .stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            log.info("Successfully retrieved {} users", users.size());
            return users;
        } catch (Exception e) {
            log.error("Failed to fetch all users", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> {
                        log.error("User not found with ID: {}", id);
                        return new RuntimeException("User not found with id: " + id);
                    });
            log.info("Successfully retrieved user with ID: {}", id);
            return convertToResponseDTO(user);
        } catch (Exception e) {
            log.error("Failed to fetch user with ID: {}", id, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("User not found with email: {}", email);
                        return new RuntimeException("User not found with email: " + email);
                    });
            log.info("Successfully retrieved user with email: {}", email);
            return convertToResponseDTO(user);
        } catch (Exception e) {
            log.error("Failed to fetch user with email: {}", email, e);
            throw e;
        }
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}
