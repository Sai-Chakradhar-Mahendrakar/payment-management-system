package org.paymentmanagementsystem.controller;

import org.paymentmanagementsystem.dto.PaymentDTO;
import org.paymentmanagementsystem.exception.PaymentException;
import org.paymentmanagementsystem.model.Category;
import org.paymentmanagementsystem.model.Payment;
import org.paymentmanagementsystem.model.User;
import org.paymentmanagementsystem.repository.CategoryRepository;
import org.paymentmanagementsystem.service.PaymentService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class PaymentController {
    private final PaymentService paymentService;
    private final CategoryRepository categoryRepository;
    private final Scanner scanner;

    public PaymentController() throws SQLException, IOException {
        this.paymentService = new PaymentService();
        this.categoryRepository = new CategoryRepository();
        this.scanner = new Scanner(System.in);
    }

    public void handleCreatePayment(User currentUser) {
        System.out.println("\n=== CREATE PAYMENT ===");

        try {
            // Show available categories
            List<Category> categories = categoryRepository.findAll();
            System.out.println("Available categories:");
            categories.forEach(cat -> System.out.println(cat.getCategoryId() + ": " + cat.getCategoryName()));

            System.out.print("Amount: ");
            BigDecimal amount = new BigDecimal(scanner.nextLine());

            // Check if user is viewer and restrict payment types
            String userRole = currentUser.getRole().getRoleName().toLowerCase();
            String type;

            if ("viewer".equals(userRole)) {
                System.out.println("Available payment types for viewers: DEBIT only");
                System.out.print("Type (DEBIT): ");
                type = scanner.nextLine().toUpperCase();

                if (!"DEBIT".equals(type)) {
                    System.out.println("Error: Viewers can only create DEBIT payments.");
                    return;
                }
            } else {
                System.out.print("Type (CREDIT, DEBIT): ");
                type = scanner.nextLine().toUpperCase();
            }

            System.out.print("Description: ");
            String description = scanner.nextLine();

            System.out.print("Category ID: ");
            int categoryId = Integer.parseInt(scanner.nextLine());

            PaymentDTO paymentDTO = new PaymentDTO(amount, type, description, categoryId);

            CompletableFuture<Payment> future = paymentService.createPayment(paymentDTO, currentUser);
            Payment payment = future.get();

            System.out.println("Payment created successfully!");
            System.out.println("Payment ID: " + payment.getPaymentId());

            if ("admin".equals(userRole)) {
                System.out.println("Status: APPROVED (Auto-approved for admin)");
            } else {
                System.out.println("Status: PENDING (Awaiting approval)");
            }

        } catch (Exception e) {
            System.out.println("Error creating payment: " + e.getMessage());
        }
    }

    public void handleViewPayments(User currentUser) {
        System.out.println("\n=== YOUR PAYMENTS ===");

        try {
            List<Payment> payments = paymentService.getPaymentsByUser(currentUser);

            if (payments.isEmpty()) {
                System.out.println("No payments found.");
                return;
            }

            System.out.printf("%-5s %-10s %-8s %-20s %-15s %-10s %-15s%n",
                    "ID", "Amount", "Type", "Date", "Category", "Status", "Created By");
            System.out.println("-".repeat(85));

            payments.forEach(payment -> {
                System.out.printf("%-5d %-10.2f %-8s %-20s %-15s %-10s %-15s%n",
                        payment.getPaymentId(),
                        payment.getAmount(),
                        payment.getType(),
                        payment.getPaymentDate().toString().substring(0, 19),
                        payment.getCategory().getCategoryName(),
                        payment.getStatus().getStatusName(),
                        payment.getCreatedBy().getName()
                );
            });

        } catch (PaymentException e) {
            System.out.println("Error retrieving payments: " + e.getMessage());
        }
    }

    public void handleFilterPaymentsByStatus(User currentUser) {
        System.out.println("\n=== FILTER PAYMENTS BY STATUS ===");
        System.out.println("Available statuses: PENDING, APPROVED, REJECTED");
        System.out.print("Enter status: ");
        String status = scanner.nextLine();

        try {
            List<Payment> payments = paymentService.getPaymentsByStatus(status, currentUser);

            if (payments.isEmpty()) {
                System.out.println("No payments found with status: " + status);
                return;
            }

            System.out.printf("%-5s %-10s %-8s %-20s %-15s %-15s%n",
                    "ID", "Amount", "Type", "Date", "Category", "Created By");
            System.out.println("-".repeat(75));

            payments.forEach(payment -> {
                System.out.printf("%-5d %-10.2f %-8s %-20s %-15s %-15s%n",
                        payment.getPaymentId(),
                        payment.getAmount(),
                        payment.getType(),
                        payment.getPaymentDate().toString().substring(0, 19),
                        payment.getCategory().getCategoryName(),
                        payment.getCreatedBy().getName()
                );
            });

        } catch (PaymentException e) {
            System.out.println("Error filtering payments: " + e.getMessage());
        }
    }
}