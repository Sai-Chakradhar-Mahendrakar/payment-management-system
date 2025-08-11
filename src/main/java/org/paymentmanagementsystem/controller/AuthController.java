package org.paymentmanagementsystem.controller;

import org.paymentmanagementsystem.dto.LoginRequestDTO;
import org.paymentmanagementsystem.dto.UserDTO;
import org.paymentmanagementsystem.exception.AuthenticationException;
import org.paymentmanagementsystem.model.User;
import org.paymentmanagementsystem.service.AuthService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class AuthController {
    private final AuthService authService;
    private final Scanner scanner;

    public AuthController() throws SQLException, IOException {
        this.authService = new AuthService();
        this.scanner = new Scanner(System.in);
    }

    public User handleLogin() {
        System.out.println("\n=== LOGIN ===");
        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            LoginRequestDTO loginRequest = new LoginRequestDTO(email, password);
            User user = authService.authenticate(loginRequest);
            System.out.println("Login successful! Welcome, " + user.getName());
            System.out.println("Role: " + user.getRole().getRoleName());
            return user;
        } catch (AuthenticationException e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    public void handleRegistration() {
        System.out.println("\n=== USER REGISTRATION ===");
        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.println("Available roles: admin, finance_manager, viewer");
        System.out.print("Role: ");
        String role = scanner.nextLine();

        try {
            UserDTO userDTO = new UserDTO(name, email, password, role);
            User user = authService.registerUser(userDTO);
            System.out.println("User registered successfully! ID: " + user.getUserId());
        } catch (AuthenticationException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    public void handleLogout(User currentUser) {
        if (currentUser != null) {
            authService.logout(currentUser.getEmail());
            System.out.println("Logged out successfully!");
        }
    }
}