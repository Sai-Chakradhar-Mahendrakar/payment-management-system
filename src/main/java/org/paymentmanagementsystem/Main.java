package org.paymentmanagementsystem;

import org.paymentmanagementsystem.config.DatabaseConfig;
import org.paymentmanagementsystem.controller.UserController;
import org.paymentmanagementsystem.controller.AuthController;
import org.paymentmanagementsystem.controller.PaymentController;
import org.paymentmanagementsystem.service.AuditService;
import org.paymentmanagementsystem.service.PaymentService;
import org.paymentmanagementsystem.model.User;

import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static AuthController authController;
    private static PaymentController paymentController;
    private static UserController userController;
    private static AuditService auditService;
    private static PaymentService paymentService;

    public static void main(String[] args) {
        try {
            System.out.println("=== Payment Management System ===");
            System.out.println("Initializing...");

            DatabaseConfig.getInstance();
            System.out.println("✓ Database initialized");

            initializeControllers();
            runApplication();
        } catch (Exception e) {
            System.err.println("Failed to start: " + e.getMessage());
            performShutdown();
            System.exit(1);
        }
    }

    private static void initializeControllers() throws Exception {
        auditService = new AuditService();
        paymentService = new PaymentService();
        authController = new AuthController();
        paymentController = new PaymentController();
        userController = new UserController();
        System.out.println("✓ Application ready");
        System.out.println();
    }

    private static void runApplication() {
        User currentUser = null;

        while (true) {
            try {
                if (currentUser == null) {
                    currentUser = handleAuthenticationMenu();
                } else {
                    boolean shouldLogout = handleMainMenu(currentUser);
                    if (shouldLogout) {
                        authController.handleLogout(currentUser);
                        currentUser = null;
                        System.out.println("Logged out successfully!");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                System.out.println("Please try again.");
            }
        }
    }

    private static User handleAuthenticationMenu() {
        System.out.println("\n=== LOGIN ===");
        System.out.println("1. Login");
        System.out.println("2. Exit");
        System.out.print("Choose option: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    return authController.handleLogin();
                case 2:
                    System.out.println("Goodbye!");
                    performShutdown();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }

        return null;
    }

    private static boolean handleMainMenu(User currentUser) {
        String roleName = currentUser.getRole().getRoleName().toLowerCase();

        System.out.println("\n=== MAIN MENU ===");
        System.out.println("User: " + currentUser.getName() + " (" + roleName + ")");
        System.out.println();

        return switch (roleName) {
            case "admin" -> showAdminMenu(currentUser);
            case "finance_manager" -> showFinanceManagerMenu(currentUser);
            case "viewer" -> showViewerMenu(currentUser);
            default -> {
                System.out.println("Unknown role. Limited access.");
                yield showViewerMenu(currentUser);
            }
        };
    }

    private static boolean showAdminMenu(User currentUser) {
        System.out.println("ADMIN PANEL");
        System.out.println("----------");

        System.out.println("\n[PAYMENT MANAGEMENT]");
        System.out.println("1. Create Payment");
        System.out.println("2. View Payments");
        System.out.println("3. Filter Payments");
        System.out.println("4. Approve Payment");
        System.out.println("5. Reject Payment");
        System.out.println("6. Delete Payment");

        System.out.println("\n[USER MANAGEMENT]");
        System.out.println("7. Register User");

        System.out.println("\n[TEAM MANAGEMENT]");
        System.out.println("8. View Teams");
        System.out.println("9. View Team Members");

        System.out.println("\n[SALARY MANAGEMENT]");
        System.out.println("10. Manage Salaries");
        System.out.println("11. Generate Salaries");

        System.out.println("\n[REPORTS]");
        System.out.println("12. Generate Reports");

        System.out.println("\n[SYSTEM]");
        System.out.println("13. Logout");

        System.out.print("\nChoice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> paymentController.handleCreatePayment(currentUser);
                case 2 -> paymentController.handleViewPayments(currentUser);
                case 3 -> paymentController.handleFilterPaymentsByStatus(currentUser);
                case 4 -> userController.handleApprovePayment(currentUser);
                case 5 -> userController.handleRejectPayment(currentUser);
                case 6 -> userController.handleDeletePayment(currentUser);
                case 7 -> authController.handleRegistration();
                case 8 -> userController.handleViewAllTeams(currentUser);
                case 9 -> userController.handleViewTeamMembers(currentUser);
                case 10 -> userController.handleManageSalaries(currentUser);
                case 11 -> userController.handleGenerateMonthlySalaries(currentUser);
                case 12 -> userController.handleGenerateReports(currentUser);
                case 13 -> { return true; }
                default -> System.out.println("Invalid option.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }

        return false;
    }

    private static boolean showFinanceManagerMenu(User currentUser) {
        System.out.println("FINANCE MANAGER PANEL");
        System.out.println("--------------------");

        System.out.println("\n[PAYMENT MANAGEMENT]");
        System.out.println("1. Create Payment");
        System.out.println("2. View Payments");
        System.out.println("3. Filter Payments");
        System.out.println("4. Approve Payment");
        System.out.println("5. Reject Payment");

        System.out.println("\n[TEAM MANAGEMENT]");
        System.out.println("6. Create Team");
        System.out.println("7. Manage Team Members");
        System.out.println("8. View My Teams");

        System.out.println("\n[REPORTS]");
        System.out.println("9. Generate Reports");

        System.out.println("\n[SYSTEM]");
        System.out.println("10. Logout");

        System.out.print("\nChoice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> paymentController.handleCreatePayment(currentUser);
                case 2 -> paymentController.handleViewPayments(currentUser);
                case 3 -> paymentController.handleFilterPaymentsByStatus(currentUser);
                case 4 -> userController.handleApprovePayment(currentUser);
                case 5 -> userController.handleRejectPayment(currentUser);
                case 6 -> userController.handleCreateTeam(currentUser);
                case 7 -> userController.handleManageTeamMembers(currentUser);
                case 8 -> userController.handleViewMyTeams(currentUser);
                case 9 -> userController.handleGenerateReports(currentUser);
                case 10 -> { return true; }
                default -> System.out.println("Invalid option.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }

        return false;
    }

    private static boolean showViewerMenu(User currentUser) {
        System.out.println("VIEWER PANEL");
        System.out.println("-----------");

        System.out.println("\n[PAYMENT MANAGEMENT]");
        System.out.println("1. Create Payment");
        System.out.println("2. View My Payments");
        System.out.println("3. Filter My Payments");

        System.out.println("\n[SYSTEM]");
        System.out.println("4. Logout");

        System.out.print("\nChoice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> paymentController.handleCreatePayment(currentUser);
                case 2 -> paymentController.handleViewPayments(currentUser);
                case 3 -> paymentController.handleFilterPaymentsByStatus(currentUser);
                case 4 -> { return true; }
                default -> System.out.println("Invalid option.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }

        return false;
    }

    private static void performShutdown() {
        try {
            System.out.println("\nShutting down...");

            // Shutdown services
            if (auditService != null) {
                auditService.shutdown();
                System.out.println("✓ Audit service shut down");
            }

            if (paymentService != null) {
                paymentService.shutdown();
                System.out.println("✓ Payment service shut down");
            }

            // Close all database connections
            DatabaseConfig.getInstance().closeAllConnections();
            System.out.println("✓ Database connections closed");

        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }
}