package miniproject1.paymentmanagementsystem.controller;

import miniproject1.paymentmanagementsystem.model.Payment;
import miniproject1.paymentmanagementsystem.model.Team;
import miniproject1.paymentmanagementsystem.model.User;
import miniproject1.paymentmanagementsystem.repository.PaymentRepository;
import miniproject1.paymentmanagementsystem.repository.TeamRepository;
import miniproject1.paymentmanagementsystem.repository.UserRepository;
import miniproject1.paymentmanagementsystem.service.PaymentService;
import miniproject1.paymentmanagementsystem.service.SalaryService;
import miniproject1.paymentmanagementsystem.service.ReportService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class UserController {
    private final PaymentService paymentService;
    private final Scanner scanner;

    public UserController() throws SQLException, IOException {
        this.paymentService = new PaymentService();
        this.scanner = new Scanner(System.in);
    }

    public void handleApprovePayment(User currentUser) {
        System.out.println("\n=== APPROVE PAYMENT ===");

        try {
            List<Payment> allPayments = paymentService.getAllPayments();

            if (allPayments.isEmpty()) {
                System.out.println("No payments found.");
                return;
            }

            System.out.println("Available Payments:");
            System.out.printf("%-5s %-10s %-8s %-20s %-15s %-10s %-15s%n",
                    "ID", "Amount", "Type", "Date", "Category", "Status", "Created By");
            System.out.println("-".repeat(85));

            allPayments.forEach(payment -> {
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

            System.out.print("\nEnter Payment ID to approve: ");
            int paymentId = Integer.parseInt(scanner.nextLine());

            CompletableFuture<Void> future = paymentService.approvePayment(paymentId, currentUser);
            future.get();

            System.out.println("Payment approved successfully!");

        } catch (Exception e) {
            System.out.println("Error approving payment: " + e.getMessage());
        }
    }

    public void handleRejectPayment(User currentUser) {
        System.out.println("\n=== REJECT PAYMENT ===");

        try {
            List<Payment> allPayments = paymentService.getAllPayments();

            if (allPayments.isEmpty()) {
                System.out.println("No payments found.");
                return;
            }

            System.out.println("Available Payments:");
            System.out.printf("%-5s %-10s %-8s %-20s %-15s %-10s %-15s%n",
                    "ID", "Amount", "Type", "Date", "Category", "Status", "Created By");
            System.out.println("-".repeat(85));

            allPayments.forEach(payment -> {
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

            System.out.print("\nEnter Payment ID to reject: ");
            int paymentId = Integer.parseInt(scanner.nextLine());

            CompletableFuture<Void> future = paymentService.rejectPayment(paymentId, currentUser);
            future.get();

            System.out.println("Payment rejected successfully!");

        } catch (Exception e) {
            System.out.println("Error rejecting payment: " + e.getMessage());
        }
    }

    public void handleDeletePayment(User currentUser) {
        System.out.println("\n=== DELETE PAYMENT ===");

        try {
            List<Payment> allPayments = paymentService.getAllPayments();

            if (allPayments.isEmpty()) {
                System.out.println("No payments found.");
                return;
            }

            System.out.println("Available Payments:");
            System.out.printf("%-5s %-10s %-8s %-20s %-15s %-10s %-15s%n",
                    "ID", "Amount", "Type", "Date", "Category", "Status", "Created By");
            System.out.println("-".repeat(85));

            allPayments.forEach(payment -> {
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

            System.out.print("\nEnter Payment ID to delete: ");
            int paymentId = Integer.parseInt(scanner.nextLine());

            System.out.print("Are you sure you want to delete this payment? (y/N): ");
            String confirmation = scanner.nextLine();

            if (confirmation.equalsIgnoreCase("y")) {
                CompletableFuture<Void> future = paymentService.deletePayment(paymentId, currentUser);
                future.get();

                System.out.println("Payment deleted successfully!");
            } else {
                System.out.println("Delete operation cancelled.");
            }

        } catch (Exception e) {
            System.out.println("Error deleting payment: " + e.getMessage());
        }
    }

    // Team Management Methods
    public void handleCreateTeam(User currentUser) {
        try {
            System.out.println("\n=== CREATE TEAM ===");
            System.out.print("Enter team name: ");
            String teamName = scanner.nextLine().trim();

            if (teamName.isEmpty()) {
                System.out.println("Team name cannot be empty.");
                return;
            }

            // Check if team name already exists
            TeamRepository teamRepo = new TeamRepository();
            if (teamRepo.findByName(teamName) != null) {
                System.out.println("Team name already exists. Please choose a different name.");
                return;
            }

            Team team = new Team(teamName, currentUser.getUserId());
            Team createdTeam = teamRepo.createTeam(team);

            if (createdTeam != null) {
                System.out.println("✓ Team '" + teamName + "' created successfully!");
                System.out.println("Team ID: " + createdTeam.getTeamId());
            } else {
                System.out.println("Failed to create team.");
            }

        } catch (Exception e) {
            System.err.println("Error creating team: " + e.getMessage());
        }
    }

    public void handleManageTeamMembers(User currentUser) {
        try {
            TeamRepository teamRepo = new TeamRepository();
            List<Team> myTeams = teamRepo.findByCreatedBy(currentUser.getUserId());

            if (myTeams.isEmpty()) {
                System.out.println("You haven't created any teams yet. Create a team first.");
                return;
            }

            System.out.println("\n=== MANAGE TEAM MEMBERS ===");
            System.out.println("Your teams:");
            for (int i = 0; i < myTeams.size(); i++) {
                System.out.println((i + 1) + ". " + myTeams.get(i).getTeamName());
            }

            System.out.print("Select team (enter number): ");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice < 1 || choice > myTeams.size()) {
                System.out.println("Invalid selection.");
                return;
            }

            Team selectedTeam = myTeams.get(choice - 1);
            manageTeamMembersMenu(selectedTeam);

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (Exception e) {
            System.err.println("Error managing team members: " + e.getMessage());
        }
    }

    private void manageTeamMembersMenu(Team team) throws Exception {
        UserRepository userRepo = new UserRepository();

        while (true) {
            System.out.println("\n=== TEAM: " + team.getTeamName() + " ===");
            System.out.println("1. View Team Members");
            System.out.println("2. Add Viewer to Team");
            System.out.println("3. Remove Member from Team");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        viewTeamMembers(team, userRepo);
                        break;
                    case 2:
                        addViewerToTeam(team, userRepo);
                        break;
                    case 3:
                        removeMemberFromTeam(team, userRepo);
                        break;
                    case 4:
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private void viewTeamMembers(Team team, UserRepository userRepo) throws Exception {
        List<User> members = userRepo.findByTeamId(team.getTeamId());

        System.out.println("\n=== TEAM MEMBERS ===");
        if (members.isEmpty()) {
            System.out.println("No members in this team yet.");
        } else {
            for (User member : members) {
                System.out.println("• " + member.getName() + " (" + member.getEmail() + ") - " +
                        member.getRole().getRoleName());
            }
        }
    }

    private void addViewerToTeam(Team team, UserRepository userRepo) throws Exception {
        // Get all available viewers (not assigned to any team)
        List<User> availableViewers = userRepo.findAvailableViewers();

        if (availableViewers.isEmpty()) {
            System.out.println("No available viewers found. All viewers are already assigned to teams.");
            return;
        }

        System.out.println("\n=== AVAILABLE VIEWERS ===");
        System.out.printf("%-5s %-20s %-30s%n", "No.", "Name", "Email");
        System.out.println("-".repeat(60));

        for (int i = 0; i < availableViewers.size(); i++) {
            User viewer = availableViewers.get(i);
            System.out.printf("%-5d %-20s %-30s%n",
                (i + 1),
                viewer.getName(),
                viewer.getEmail()
            );
        }

        System.out.print("\nSelect viewer to add to team (enter number, 0 to cancel): ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            return;
        }

        if (choice == 0) {
            System.out.println("Operation cancelled.");
            return;
        }

        if (choice < 1 || choice > availableViewers.size()) {
            System.out.println("Invalid selection. Please choose a number from the list.");
            return;
        }

        User selectedViewer = availableViewers.get(choice - 1);

        if (userRepo.assignUserToTeam(selectedViewer.getUserId(), team.getTeamId())) {
            System.out.println("✓ " + selectedViewer.getName() + " added to team successfully!");
        } else {
            System.out.println("Failed to add user to team.");
        }
    }

    private void removeMemberFromTeam(Team team, UserRepository userRepo) throws Exception {
        List<User> members = userRepo.findByTeamId(team.getTeamId());

        if (members.isEmpty()) {
            System.out.println("No members in this team to remove.");
            return;
        }

        System.out.println("\n=== REMOVE TEAM MEMBER ===");
        for (int i = 0; i < members.size(); i++) {
            User member = members.get(i);
            System.out.println((i + 1) + ". " + member.getName() + " (" + member.getEmail() + ")");
        }

        System.out.print("Select member to remove (enter number): ");
        int choice = Integer.parseInt(scanner.nextLine());

        if (choice < 1 || choice > members.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        User memberToRemove = members.get(choice - 1);

        if (userRepo.assignUserToTeam(memberToRemove.getUserId(), null)) {
            System.out.println("✓ " + memberToRemove.getName() + " removed from team successfully!");
        } else {
            System.out.println("Failed to remove member from team.");
        }
    }

    public void handleViewMyTeams(User currentUser) {
        try {
            TeamRepository teamRepo = new TeamRepository();
            List<Team> myTeams = teamRepo.findByCreatedBy(currentUser.getUserId());

            System.out.println("\n=== MY TEAMS ===");
            if (myTeams.isEmpty()) {
                System.out.println("You haven't created any teams yet.");
            } else {
                for (Team team : myTeams) {
                    System.out.println("• " + team.getTeamName() + " (Created: " +
                            team.getCreatedDate().toLocalDate() + ")");

                    // Show team member count
                    UserRepository userRepo = new UserRepository();
                    List<User> members = userRepo.findByTeamId(team.getTeamId());
                    System.out.println("  Members: " + members.size());
                }
            }
        } catch (Exception e) {
            System.err.println("Error viewing teams: " + e.getMessage());
        }
    }

    // Admin-specific team viewing methods
    public void handleViewAllTeams(User currentUser) {
        try {
            TeamRepository teamRepo = new TeamRepository();
            UserRepository userRepo = new UserRepository();
            List<Team> allTeams = teamRepo.findAll();

            System.out.println("\n=== ALL TEAMS (ADMIN VIEW) ===");
            if (allTeams.isEmpty()) {
                System.out.println("No teams have been created yet.");
            } else {
                System.out.println("Total teams: " + allTeams.size());
                System.out.println();

                for (Team team : allTeams) {
                    System.out.println("🏢 Team: " + team.getTeamName());
                    System.out.println("   Team ID: " + team.getTeamId());
                    System.out.println("   Created: " + team.getCreatedDate().toLocalDate());

                    // Get creator information
                    try {
                        var creatorOptional = userRepo.findByEmail(getUserEmailById(team.getCreatedByUserId(), userRepo));
                        if (creatorOptional.isPresent()) {
                            User creator = creatorOptional.get();
                            System.out.println("   Created by: " + creator.getName() + " (" + creator.getRole().getRoleName() + ")");
                        }
                    } catch (Exception e) {
                        System.out.println("   Created by: User ID " + team.getCreatedByUserId());
                    }

                    // Get team member count
                    List<User> members = userRepo.findByTeamId(team.getTeamId());
                    System.out.println("   Members: " + members.size());

                    if (!members.isEmpty()) {
                        System.out.println("   Team Members:");
                        for (User member : members) {
                            System.out.println("     • " + member.getName() + " (" + member.getEmail() + ")");
                        }
                    }
                    System.out.println();
                }
            }
        } catch (Exception e) {
            System.err.println("Error viewing all teams: " + e.getMessage());
        }
    }

    public void handleViewTeamMembers(User currentUser) {
        try {
            TeamRepository teamRepo = new TeamRepository();
            List<Team> allTeams = teamRepo.findAll();

            if (allTeams.isEmpty()) {
                System.out.println("No teams have been created yet.");
                return;
            }

            System.out.println("\n=== SELECT TEAM TO VIEW MEMBERS ===");
            for (int i = 0; i < allTeams.size(); i++) {
                System.out.println((i + 1) + ". " + allTeams.get(i).getTeamName() +
                                 " (ID: " + allTeams.get(i).getTeamId() + ")");
            }
            System.out.println((allTeams.size() + 1) + ". View All Users by Role");
            System.out.println((allTeams.size() + 2) + ". Back to Main Menu");

            System.out.print("Select option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice >= 1 && choice <= allTeams.size()) {
                Team selectedTeam = allTeams.get(choice - 1);
                viewDetailedTeamMembers(selectedTeam);
            } else if (choice == allTeams.size() + 1) {
                viewAllUsersByRole();
            } else if (choice == allTeams.size() + 2) {
                return;
            } else {
                System.out.println("Invalid selection.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (Exception e) {
            System.err.println("Error viewing team members: " + e.getMessage());
        }
    }

    private void viewDetailedTeamMembers(Team team) throws Exception {
        UserRepository userRepo = new UserRepository();
        List<User> members = userRepo.findByTeamId(team.getTeamId());

        System.out.println("\n=== TEAM: " + team.getTeamName() + " - DETAILED VIEW ===");
        System.out.println("Team ID: " + team.getTeamId());
        System.out.println("Created: " + team.getCreatedDate());

        // Get creator information
        try {
            String creatorEmail = getUserEmailById(team.getCreatedByUserId(), userRepo);
            var creatorOptional = userRepo.findByEmail(creatorEmail);
            if (creatorOptional.isPresent()) {
                User creator = creatorOptional.get();
                System.out.println("Created by: " + creator.getName() + " (" + creator.getEmail() + ")");
            }
        } catch (Exception e) {
            System.out.println("Created by: User ID " + team.getCreatedByUserId());
        }

        System.out.println("\nTeam Members (" + members.size() + "):");
        if (members.isEmpty()) {
            System.out.println("No members in this team yet.");
        } else {
            System.out.printf("%-20s %-30s %-15s %-15s%n", "Name", "Email", "Role", "User ID");
            System.out.println("-".repeat(80));
            for (User member : members) {
                System.out.printf("%-20s %-30s %-15s %-15s%n",
                    member.getName(),
                    member.getEmail(),
                    member.getRole().getRoleName(),
                    member.getUserId());
            }
        }
    }

    private void viewAllUsersByRole() throws Exception {
        UserRepository userRepo = new UserRepository();
        List<User> allUsers = userRepo.findAll();

        System.out.println("\n=== ALL USERS BY ROLE ===");

        // Group users by role
        var adminUsers = allUsers.stream().filter(u -> "admin".equals(u.getRole().getRoleName())).toList();
        var financeManagers = allUsers.stream().filter(u -> "finance_manager".equals(u.getRole().getRoleName())).toList();
        var viewers = allUsers.stream().filter(u -> "viewer".equals(u.getRole().getRoleName())).toList();

        System.out.println("\n👑 ADMINISTRATORS (" + adminUsers.size() + "):");
        if (adminUsers.isEmpty()) {
            System.out.println("  No admin users found.");
        } else {
            for (User user : adminUsers) {
                System.out.println("  • " + user.getName() + " (" + user.getEmail() + ")");
            }
        }

        System.out.println("\n💼 FINANCE MANAGERS (" + financeManagers.size() + "):");
        if (financeManagers.isEmpty()) {
            System.out.println("  No finance managers found.");
        } else {
            for (User user : financeManagers) {
                String teamInfo = user.getTeamId() != null ? " [Team: " + user.getTeamId() + "]" : " [No team]";
                System.out.println("  • " + user.getName() + " (" + user.getEmail() + ")" + teamInfo);
            }
        }

        System.out.println("\n👥 VIEWERS (" + viewers.size() + "):");
        if (viewers.isEmpty()) {
            System.out.println("  No viewers found.");
        } else {
            for (User user : viewers) {
                String teamInfo = user.getTeamId() != null ? " [Team: " + user.getTeamId() + "]" : " [No team assigned]";
                System.out.println("  • " + user.getName() + " (" + user.getEmail() + ")" + teamInfo);
            }
        }
    }

    // Salary Management Methods
    public void handleManageSalaries(User currentUser) {
        try {
            while (true) {
                System.out.println("\n=== SALARY MANAGEMENT ===");
                System.out.println("1. View All Users with Salaries");
                System.out.println("2. Set/Update User Salary");
                System.out.println("3. View Salary Payments History");
                System.out.println("4. Back to Main Menu");
                System.out.print("Choose option: ");

                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        viewUsersWithSalaries();
                        break;
                    case 2:
                        setUserSalary();
                        break;
                    case 3:
                        viewSalaryPaymentsHistory();
                        break;
                    case 4:
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (Exception e) {
            System.err.println("Error in salary management: " + e.getMessage());
        }
    }

    private void viewUsersWithSalaries() throws Exception {
        SalaryService salaryService = new SalaryService();
        List<User> usersWithSalary = salaryService.getUsersWithSalary();

        System.out.println("\n=== USERS WITH SALARIES ===");
        if (usersWithSalary.isEmpty()) {
            System.out.println("No users have salary configured yet.");
        } else {
            System.out.printf("%-20s %-30s %-15s %-15s %-15s%n",
                "Name", "Email", "Role", "Monthly Salary", "Effective Date");
            System.out.println("-".repeat(95));

            for (User user : usersWithSalary) {
                System.out.printf("%-20s %-30s %-15s Rs.%-14s %-15s%n",
                    user.getName(),
                    user.getEmail(),
                    user.getRole().getRoleName(),
                    user.getMonthlySalary(),
                    user.getSalaryEffectiveDate() != null ? user.getSalaryEffectiveDate().toString() : "Not set"
                );
            }
        }
    }

    private void setUserSalary() throws Exception {
        UserRepository userRepo = new UserRepository();
        SalaryService salaryService = new SalaryService();

        System.out.println("\n=== SET/UPDATE USER SALARY ===");
        System.out.print("Enter user email: ");
        String email = scanner.nextLine().trim();

        var userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            System.out.println("User not found with email: " + email);
            return;
        }

        User user = userOptional.get();

        // Check if user is a viewer (only viewers should have salary)
        if (!"viewer".equals(user.getRole().getRoleName())) {
            System.out.println("Only viewers can have salary configured.");
            System.out.println("Current user role: " + user.getRole().getRoleName());
            return;
        }

        System.out.println("User: " + user.getName() + " (" + user.getEmail() + ")");
        System.out.println("Current salary: Rs." + (user.getMonthlySalary() != null ? user.getMonthlySalary() : "0.00"));
        System.out.println("Current effective date: " + (user.getSalaryEffectiveDate() != null ? user.getSalaryEffectiveDate() : "Not set"));

        System.out.print("Enter new monthly salary: Rs.");
        String salaryInput = scanner.nextLine().trim();

        try {
            BigDecimal monthlySalary = new BigDecimal(salaryInput);
            if (monthlySalary.compareTo(BigDecimal.ZERO) < 0) {
                System.out.println("Salary cannot be negative.");
                return;
            }

            System.out.print("Enter effective date (YYYY-MM-DD) or press Enter for today: ");
            String dateInput = scanner.nextLine().trim();

            LocalDate effectiveDate;
            if (dateInput.isEmpty()) {
                effectiveDate = LocalDate.now();
            } else {
                effectiveDate = LocalDate.parse(dateInput);
            }

            if (salaryService.updateUserSalary(user.getUserId(), monthlySalary, effectiveDate)) {
                System.out.println("✓ Salary updated successfully!");
                System.out.println("New salary: Rs." + monthlySalary);
                System.out.println("Effective date: " + effectiveDate);
            } else {
                System.out.println("Failed to update salary.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid salary amount. Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD format.");
        }
    }

    private void viewSalaryPaymentsHistory() throws Exception {
        PaymentRepository paymentRepo = new PaymentRepository();
        List<Payment> allPayments = paymentRepo.findAll();

        // Filter salary payments
        List<Payment> salaryPayments = allPayments.stream()
            .filter(p -> "SALARY".equals(p.getType()))
            .toList();

        System.out.println("\n=== SALARY PAYMENTS HISTORY ===");
        if (salaryPayments.isEmpty()) {
            System.out.println("No salary payments found.");
        } else {
            System.out.printf("%-10s %-20s %-15s %-15s %-20s%n",
                "Payment ID", "Employee", "Amount", "Status", "Date");
            System.out.println("-".repeat(80));

            for (Payment payment : salaryPayments) {
                System.out.printf("%-10s %-20s Rs.%-14s %-15s %-20s%n",
                    payment.getPaymentId(),
                    payment.getCreatedBy() != null ? payment.getCreatedBy().getName() : "Unknown",
                    payment.getAmount(),
                    payment.getStatus() != null ? payment.getStatus().getStatusName() : "Unknown",
                    payment.getPaymentDate().toLocalDate()
                );
            }
        }
    }

    public void handleGenerateMonthlySalaries(User currentUser) {
        try {
            System.out.println("\n=== GENERATE MONTHLY SALARY PAYMENTS ===");
            System.out.println("This will generate salary payments for all eligible users for the current month.");
            System.out.print("Do you want to proceed? (y/N): ");

            String confirmation = scanner.nextLine().trim();
            if (!confirmation.equalsIgnoreCase("y")) {
                System.out.println("Operation cancelled.");
                return;
            }

            SalaryService salaryService = new SalaryService();
            salaryService.generateMonthlySalaryPayments();

        } catch (Exception e) {
            System.err.println("Error generating monthly salaries: " + e.getMessage());
        }
    }

    // Report Generation Methods
    public void handleGenerateReports(User currentUser) {
        try {
            while (true) {
                System.out.println("\n=== REPORT GENERATION ===");
                System.out.println("1. Generate Monthly Report");
                System.out.println("2. Generate Quarterly Report");
                System.out.println("3. Generate Current Month Report");
                System.out.println("4. Generate Current Quarter Report");
                System.out.println("5. Generate Team-wise Monthly Report");
                System.out.println("6. Generate Team-wise Quarterly Report");
                System.out.println("7. Generate All Teams Summary Report");
                System.out.println("8. Back to Main Menu");
                System.out.print("Choose option: ");

                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        generateMonthlyReport();
                        break;
                    case 2:
                        generateQuarterlyReport();
                        break;
                    case 3:
                        generateCurrentMonthReport();
                        break;
                    case 4:
                        generateCurrentQuarterReport();
                        break;
                    case 5:
                        generateTeamMonthlyReport();
                        break;
                    case 6:
                        generateTeamQuarterlyReport();
                        break;
                    case 7:
                        generateAllTeamsSummaryReport();
                        break;
                    case 8:
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (Exception e) {
            System.err.println("Error in report generation: " + e.getMessage());
        }
    }

    private void generateMonthlyReport() throws Exception {
        System.out.println("\n=== GENERATE MONTHLY REPORT ===");
        System.out.print("Enter year (e.g., 2025): ");
        int year = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter month (1-12): ");
        int month = Integer.parseInt(scanner.nextLine());

        if (month < 1 || month > 12) {
            System.out.println("Invalid month. Please enter a number between 1 and 12.");
            return;
        }

        ReportService reportService = new ReportService();
        ReportService.MonthlyReport report = reportService.generateMonthlyReport(year, month);

        displayMonthlyReport(report);
    }

    private void generateQuarterlyReport() throws Exception {
        System.out.println("\n=== GENERATE QUARTERLY REPORT ===");
        System.out.print("Enter year (e.g., 2025): ");
        int year = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter quarter (1-4): ");
        int quarter = Integer.parseInt(scanner.nextLine());

        if (quarter < 1 || quarter > 4) {
            System.out.println("Invalid quarter. Please enter a number between 1 and 4.");
            return;
        }

        ReportService reportService = new ReportService();
        ReportService.QuarterlyReport report = reportService.generateQuarterlyReport(year, quarter);

        displayQuarterlyReport(report);
    }

    private void generateCurrentMonthReport() throws Exception {
        System.out.println("\n=== CURRENT MONTH REPORT ===");

        ReportService reportService = new ReportService();
        ReportService.MonthlyReport report = reportService.generateCurrentMonthReport();

        displayMonthlyReport(report);
    }

    private void generateCurrentQuarterReport() throws Exception {
        System.out.println("\n=== CURRENT QUARTER REPORT ===");

        ReportService reportService = new ReportService();
        ReportService.QuarterlyReport report = reportService.generateCurrentQuarterReport();

        displayQuarterlyReport(report);
    }

    private void generateTeamMonthlyReport() throws Exception {
        System.out.println("\n=== GENERATE TEAM-WISE MONTHLY REPORT ===");
        
        // First, let user select a team
        TeamRepository teamRepo = new TeamRepository();
        List<Team> allTeams = teamRepo.findAll();
        
        if (allTeams.isEmpty()) {
            System.out.println("No teams found. Please create teams first.");
            return;
        }
        
        System.out.println("Available Teams:");
        for (int i = 0; i < allTeams.size(); i++) {
            Team team = allTeams.get(i);
            System.out.println((i + 1) + ". " + team.getTeamName() + " (ID: " + team.getTeamId() + ")");
        }
        
        System.out.print("Select team (enter number): ");
        int teamChoice = Integer.parseInt(scanner.nextLine());
        
        if (teamChoice < 1 || teamChoice > allTeams.size()) {
            System.out.println("Invalid team selection.");
            return;
        }
        
        Team selectedTeam = allTeams.get(teamChoice - 1);
        
        System.out.print("Enter year (e.g., 2025): ");
        int year = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter month (1-12): ");
        int month = Integer.parseInt(scanner.nextLine());

        if (month < 1 || month > 12) {
            System.out.println("Invalid month. Please enter a number between 1 and 12.");
            return;
        }

        ReportService reportService = new ReportService();
        ReportService.TeamMonthlyReport report = reportService.generateTeamMonthlyReport(year, month, selectedTeam.getTeamId());

        displayTeamMonthlyReport(report);
    }

    private void generateTeamQuarterlyReport() throws Exception {
        System.out.println("\n=== GENERATE TEAM-WISE QUARTERLY REPORT ===");
        
        // First, let user select a team
        TeamRepository teamRepo = new TeamRepository();
        List<Team> allTeams = teamRepo.findAll();
        
        if (allTeams.isEmpty()) {
            System.out.println("No teams found. Please create teams first.");
            return;
        }
        
        System.out.println("Available Teams:");
        for (int i = 0; i < allTeams.size(); i++) {
            Team team = allTeams.get(i);
            System.out.println((i + 1) + ". " + team.getTeamName() + " (ID: " + team.getTeamId() + ")");
        }
        
        System.out.print("Select team (enter number): ");
        int teamChoice = Integer.parseInt(scanner.nextLine());
        
        if (teamChoice < 1 || teamChoice > allTeams.size()) {
            System.out.println("Invalid team selection.");
            return;
        }
        
        Team selectedTeam = allTeams.get(teamChoice - 1);
        
        System.out.print("Enter year (e.g., 2025): ");
        int year = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter quarter (1-4): ");
        int quarter = Integer.parseInt(scanner.nextLine());

        if (quarter < 1 || quarter > 4) {
            System.out.println("Invalid quarter. Please enter a number between 1 and 4.");
            return;
        }

        ReportService reportService = new ReportService();
        ReportService.TeamQuarterlyReport report = reportService.generateTeamQuarterlyReport(year, quarter, selectedTeam.getTeamId());

        displayTeamQuarterlyReport(report);
    }

    private void generateAllTeamsSummaryReport() throws Exception {
        System.out.println("\n=== GENERATE ALL TEAMS SUMMARY REPORT ===");
        System.out.print("Enter year (e.g., 2025): ");
        int year = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter month (1-12) or 0 for quarterly report: ");
        int month = Integer.parseInt(scanner.nextLine());

        ReportService reportService = new ReportService();
        if (month == 0) {
            // Generate quarterly report
            System.out.print("Enter quarter (1-4): ");
            int quarter = Integer.parseInt(scanner.nextLine());
            reportService.generateAllTeamsQuarterlySummary(year, quarter);
        } else {
            // Generate monthly report
            reportService.generateAllTeamsMonthlySummary(year, month);
        }

        System.out.println("Report generated successfully.");
    }

    private void displayMonthlyReport(ReportService.MonthlyReport report) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                    MONTHLY PAYMENT REPORT");
        System.out.println("=".repeat(80));
        System.out.println("Period: " + report.getMonthName() + " " + report.getYear());
        System.out.println("Generated on: " + LocalDate.now());
        System.out.println("-".repeat(80));

        // Summary
        System.out.println("\n📊 SUMMARY:");
        System.out.println("Total Payments: " + report.getTotalPayments());
        System.out.println("Total Amount: Rs." + report.getTotalAmount());

        // By Status
        System.out.println("\n📈 PAYMENTS BY STATUS:");
        if (report.getPaymentsByStatus().isEmpty()) {
            System.out.println("No payments found for this period.");
        } else {
            System.out.printf("%-15s %-10s %-15s%n", "Status", "Count", "Amount");
            System.out.println("-".repeat(40));
            for (String status : report.getPaymentsByStatus().keySet()) {
                int count = report.getPaymentsByStatus().get(status);
                BigDecimal amount = report.getAmountsByStatus().getOrDefault(status, BigDecimal.ZERO);
                System.out.printf("%-15s %-10d Rs.%-14s%n", status, count, amount);
            }
        }

        // By Type
        System.out.println("\n🏷️ PAYMENTS BY TYPE:");
        if (report.getPaymentsByType().isEmpty()) {
            System.out.println("No payment types found.");
        } else {
            System.out.printf("%-15s %-10s %-15s%n", "Type", "Count", "Amount");
            System.out.println("-".repeat(40));
            for (String type : report.getPaymentsByType().keySet()) {
                int count = report.getPaymentsByType().get(type);
                BigDecimal amount = report.getAmountsByType().getOrDefault(type, BigDecimal.ZERO);
                System.out.printf("%-15s %-10d Rs.%-14s%n", type, count, amount);
            }
        }

        // By Category
        System.out.println("\n📂 PAYMENTS BY CATEGORY:");
        if (report.getPaymentsByCategory().isEmpty()) {
            System.out.println("No categories found.");
        } else {
            System.out.printf("%-20s %-10s %-15s%n", "Category", "Count", "Amount");
            System.out.println("-".repeat(45));
            for (String category : report.getPaymentsByCategory().keySet()) {
                int count = report.getPaymentsByCategory().get(category);
                BigDecimal amount = report.getAmountsByCategory().getOrDefault(category, BigDecimal.ZERO);
                System.out.printf("%-20s %-10d Rs.%-14s%n", category, count, amount);
            }
        }

        System.out.println("\n" + "=".repeat(80));
    }

    private void displayQuarterlyReport(ReportService.QuarterlyReport report) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                   QUARTERLY PAYMENT REPORT");
        System.out.println("=".repeat(80));
        System.out.println("Period: Q" + report.getQuarter() + " " + report.getYear());
        System.out.println("Date Range: " + report.getStartDate() + " to " + report.getEndDate());
        System.out.println("Generated on: " + LocalDate.now());
        System.out.println("-".repeat(80));

        // Summary
        System.out.println("\n📊 QUARTERLY SUMMARY:");
        System.out.println("Total Payments: " + report.getTotalPayments());
        System.out.println("Total Amount: Rs." + report.getTotalAmount());

        // Monthly Breakdown
        System.out.println("\n📅 MONTHLY BREAKDOWN:");
        if (report.getMonthlyBreakdown().isEmpty()) {
            System.out.println("No payments found for this quarter.");
        } else {
            System.out.printf("%-12s %-10s %-15s%n", "Month", "Count", "Amount");
            System.out.println("-".repeat(37));
            for (String monthKey : report.getMonthlyBreakdown().keySet()) {
                ReportService.MonthlyReport monthlyReport = report.getMonthlyBreakdown().get(monthKey);
                System.out.printf("%-12s %-10d Rs.%-14s%n",
                    monthKey,
                    monthlyReport.getTotalPayments(),
                    monthlyReport.getTotalAmount());
            }
        }

        // By Status
        System.out.println("\n📈 QUARTERLY PAYMENTS BY STATUS:");
        if (report.getPaymentsByStatus().isEmpty()) {
            System.out.println("No payments found for this period.");
        } else {
            System.out.printf("%-15s %-10s %-15s%n", "Status", "Count", "Amount");
            System.out.println("-".repeat(40));
            for (String status : report.getPaymentsByStatus().keySet()) {
                int count = report.getPaymentsByStatus().get(status);
                BigDecimal amount = report.getAmountsByStatus().getOrDefault(status, BigDecimal.ZERO);
                System.out.printf("%-15s %-10d Rs.%-14s%n", status, count, amount);
            }
        }

        // By Type
        System.out.println("\n🏷️ QUARTERLY PAYMENTS BY TYPE:");
        if (report.getPaymentsByType().isEmpty()) {
            System.out.println("No payment types found.");
        } else {
            System.out.printf("%-15s %-10s %-15s%n", "Type", "Count", "Amount");
            System.out.println("-".repeat(40));
            for (String type : report.getPaymentsByType().keySet()) {
                int count = report.getPaymentsByType().get(type);
                BigDecimal amount = report.getAmountsByType().getOrDefault(type, BigDecimal.ZERO);
                System.out.printf("%-15s %-10d Rs.%-14s%n", type, count, amount);
            }
        }

        System.out.println("\n" + "=".repeat(80));
    }

    private void displayTeamMonthlyReport(ReportService.TeamMonthlyReport report) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                TEAM-WISE MONTHLY REPORT");
        System.out.println("=".repeat(80));
        System.out.println("Team: " + report.getTeamName() + " (ID: " + report.getTeamId() + ")");
        System.out.println("Period: " + report.getMonthName() + " " + report.getYear());
        System.out.println("Generated on: " + LocalDate.now());
        System.out.println("-".repeat(80));

        // Summary
        System.out.println("\n📊 TEAM SUMMARY:");
        System.out.println("Total Payments: " + report.getTotalPayments());
        System.out.println("Total Amount: Rs." + report.getTotalAmount());

        // By Status
        System.out.println("\n📈 PAYMENTS BY STATUS:");
        if (report.getPaymentsByStatus().isEmpty()) {
            System.out.println("No payments found for this team in this period.");
        } else {
            System.out.printf("%-15s %-10s %-15s%n", "Status", "Count", "Amount");
            System.out.println("-".repeat(40));
            for (String status : report.getPaymentsByStatus().keySet()) {
                int count = report.getPaymentsByStatus().get(status);
                BigDecimal amount = report.getAmountsByStatus().getOrDefault(status, BigDecimal.ZERO);
                System.out.printf("%-15s %-10d Rs.%-14s%n", status, count, amount);
            }
        }

        // By Type
        System.out.println("\n🏷️ PAYMENTS BY TYPE:");
        if (report.getPaymentsByType().isEmpty()) {
            System.out.println("No payment types found for this team.");
        } else {
            System.out.printf("%-15s %-10s %-15s%n", "Type", "Count", "Amount");
            System.out.println("-".repeat(40));
            for (String type : report.getPaymentsByType().keySet()) {
                int count = report.getPaymentsByType().get(type);
                BigDecimal amount = report.getAmountsByType().getOrDefault(type, BigDecimal.ZERO);
                System.out.printf("%-15s %-10d Rs.%-14s%n", type, count, amount);
            }
        }

        // By Category
        System.out.println("\n📂 PAYMENTS BY CATEGORY:");
        if (report.getPaymentsByCategory().isEmpty()) {
            System.out.println("No categories found for this team.");
        } else {
            System.out.printf("%-20s %-10s %-15s%n", "Category", "Count", "Amount");
            System.out.println("-".repeat(45));
            for (String category : report.getPaymentsByCategory().keySet()) {
                int count = report.getPaymentsByCategory().get(category);
                BigDecimal amount = report.getAmountsByCategory().getOrDefault(category, BigDecimal.ZERO);
                System.out.printf("%-20s %-10d Rs.%-14s%n", category, count, amount);
            }
        }

        System.out.println("\n" + "=".repeat(80));
    }

    private void displayTeamQuarterlyReport(ReportService.TeamQuarterlyReport report) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                TEAM-WISE QUARTERLY REPORT");
        System.out.println("=".repeat(80));
        System.out.println("Team: " + report.getTeamName() + " (ID: " + report.getTeamId() + ")");
        System.out.println("Period: Q" + report.getQuarter() + " " + report.getYear());
        System.out.println("Date Range: " + report.getStartDate() + " to " + report.getEndDate());
        System.out.println("Generated on: " + LocalDate.now());
        System.out.println("-".repeat(80));

        // Summary
        System.out.println("\n📊 TEAM QUARTERLY SUMMARY:");
        System.out.println("Total Payments: " + report.getTotalPayments());
        System.out.println("Total Amount: Rs." + report.getTotalAmount());

        // Monthly Breakdown
        System.out.println("\n📅 MONTHLY BREAKDOWN:");
        if (report.getMonthlyBreakdown().isEmpty()) {
            System.out.println("No payments found for this team in this quarter.");
        } else {
            System.out.printf("%-12s %-10s %-15s%n", "Month", "Count", "Amount");
            System.out.println("-".repeat(37));
            for (String monthKey : report.getMonthlyBreakdown().keySet()) {
                ReportService.MonthlyReport monthlyReport = report.getMonthlyBreakdown().get(monthKey);
                System.out.printf("%-12s %-10d Rs.%-14s%n",
                    monthKey,
                    monthlyReport.getTotalPayments(),
                    monthlyReport.getTotalAmount());
            }
        }

        // By Status
        System.out.println("\n📈 QUARTERLY PAYMENTS BY STATUS:");
        if (report.getPaymentsByStatus().isEmpty()) {
            System.out.println("No payments found for this team in this period.");
        } else {
            System.out.printf("%-15s %-10s %-15s%n", "Status", "Count", "Amount");
            System.out.println("-".repeat(40));
            for (String status : report.getPaymentsByStatus().keySet()) {
                int count = report.getPaymentsByStatus().get(status);
                BigDecimal amount = report.getAmountsByStatus().getOrDefault(status, BigDecimal.ZERO);
                System.out.printf("%-15s %-10d Rs.%-14s%n", status, count, amount);
            }
        }

        // By Type
        System.out.println("\n🏷️ QUARTERLY PAYMENTS BY TYPE:");
        if (report.getPaymentsByType().isEmpty()) {
            System.out.println("No payment types found for this team.");
        } else {
            System.out.printf("%-15s %-10s %-15s%n", "Type", "Count", "Amount");
            System.out.println("-".repeat(40));
            for (String type : report.getPaymentsByType().keySet()) {
                int count = report.getPaymentsByType().get(type);
                BigDecimal amount = report.getAmountsByType().getOrDefault(type, BigDecimal.ZERO);
                System.out.printf("%-15s %-10d Rs.%-14s%n", type, count, amount);
            }
        }

        System.out.println("\n" + "=".repeat(80));
    }

    private String getUserEmailById(int userId, UserRepository userRepo) throws Exception {
        // This is a helper method to get user email by ID
        // Since we don't have a direct findById method, we'll use findAll and filter
        List<User> allUsers = userRepo.findAll();
        return allUsers.stream()
                .filter(u -> u.getUserId() == userId)
                .map(User::getEmail)
                .findFirst()
                .orElse("unknown@unknown.com");
    }
}

