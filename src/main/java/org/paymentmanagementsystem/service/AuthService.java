package org.paymentmanagementsystem.service;


import org.paymentmanagementsystem.dto.LoginRequestDTO;
import org.paymentmanagementsystem.dto.UserDTO;
import org.paymentmanagementsystem.exception.AuthenticationException;
import org.paymentmanagementsystem.model.User;
import org.paymentmanagementsystem.repository.UserRepository;
import org.paymentmanagementsystem.util.PasswordUtil;
import org.paymentmanagementsystem.util.ValidationUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, User> loggedInUsers;

    public AuthService() throws SQLException, IOException {
        this.userRepository = new UserRepository();
        this.loggedInUsers = new ConcurrentHashMap<>();
    }

    public User authenticate(LoginRequestDTO loginRequest) throws AuthenticationException {
        try {
            if (!ValidationUtil.isValidEmail(loginRequest.getEmail())) {
                throw new AuthenticationException("Invalid email format");
            }

            Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
            if (userOpt.isEmpty()) {
                throw new AuthenticationException("User not found");
            }

            User user = userOpt.get();
            if (!PasswordUtil.verifyPassword(loginRequest.getPassword(), user.getPasswordHash())) {
                throw new AuthenticationException("Invalid password");
            }

            // Store logged in user
            loggedInUsers.put(user.getEmail(), user);
            return user;

        } catch (SQLException e) {
            throw new AuthenticationException("Database error during authentication", e);
        }
    }

    public User registerUser(UserDTO userDTO) throws AuthenticationException {
        try {
            // Validate input
            if (!ValidationUtil.isValidEmail(userDTO.getEmail())) {
                throw new AuthenticationException("Invalid email format");
            }

            if (!ValidationUtil.isValidPassword(userDTO.getPassword())) {
                throw new AuthenticationException("Password must be at least 6 characters");
            }

            if (!ValidationUtil.isNotEmpty(userDTO.getName())) {
                throw new AuthenticationException("Name cannot be empty");
            }

            // Check if user already exists
            Optional<User> existingUser = userRepository.findByEmail(userDTO.getEmail());
            if (existingUser.isPresent()) {
                throw new AuthenticationException("User already exists with this email");
            }

            // Create new user
            String hashedPassword = PasswordUtil.hashPassword(userDTO.getPassword());
            int roleId = getRoleIdByName(userDTO.getRoleName());

            User user = new User(userDTO.getName(), userDTO.getEmail(), hashedPassword, roleId);
            return userRepository.save(user);

        } catch (SQLException e) {
            throw new AuthenticationException("Database error during registration", e);
        }
    }

    public void logout(String email) {
        loggedInUsers.remove(email);
    }

    public boolean isLoggedIn(String email) {
        return loggedInUsers.containsKey(email);
    }

    public User getCurrentUser(String email) {
        return loggedInUsers.get(email);
    }

    private int getRoleIdByName(String roleName) {
        switch (roleName.toLowerCase()) {
            case "admin":
                return 1;
            case "finance_manager":
                return 2;
            case "viewer":
                return 3;
            default:
                return 3;
        }
    }
}

