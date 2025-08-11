package miniproject1.paymentmanagementsystem.service;

import miniproject1.paymentmanagementsystem.model.Payment;
import miniproject1.paymentmanagementsystem.model.User;
import miniproject1.paymentmanagementsystem.repository.CategoryRepository;
import miniproject1.paymentmanagementsystem.repository.PaymentRepository;
import miniproject1.paymentmanagementsystem.repository.StatusRepository;
import miniproject1.paymentmanagementsystem.repository.UserRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public class SalaryService {
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final CategoryRepository categoryRepository;
    private final StatusRepository statusRepository;
    private final AuditService auditService;

    public SalaryService() throws Exception {
        this.userRepository = new UserRepository();
        this.paymentRepository = new PaymentRepository();
        this.categoryRepository = new CategoryRepository();
        this.statusRepository = new StatusRepository();
        this.auditService = new AuditService();
    }

    public void generateMonthlySalaryPayments() throws SQLException {
        YearMonth currentMonth = YearMonth.now();
        generateSalaryPaymentsForMonth(currentMonth);
    }

    public void generateSalaryPaymentsForMonth(YearMonth yearMonth) throws SQLException {
        List<User> usersWithSalary = userRepository.findUsersWithSalary();

        System.out.println("\n=== GENERATING MONTHLY SALARY PAYMENTS ===");
        System.out.println("Month: " + yearMonth);
        System.out.println("Eligible users: " + usersWithSalary.size());

        int successCount = 0;
        int skipCount = 0;

        Integer salaryCategoryId = getSalaryCategoryId();
        Integer approvedStatusId = getApprovedStatusId();

        for (User user : usersWithSalary) {
            try {
                // Check if user has salary and effective date
                if (user.getMonthlySalary() == null || user.getMonthlySalary().compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("⚠️  Skipping " + user.getName() + " - No salary set");
                    skipCount++;
                    continue;
                }

                if (user.getSalaryEffectiveDate() == null) {
                    System.out.println("⚠️  Skipping " + user.getName() + " - No effective date set");
                    skipCount++;
                    continue;
                }

                // Check if salary is effective for this month
                YearMonth effectiveMonth = YearMonth.from(user.getSalaryEffectiveDate());
                if (yearMonth.isBefore(effectiveMonth)) {
                    System.out.println("⚠️  Skipping " + user.getName() + " - Salary not effective yet");
                    skipCount++;
                    continue;
                }

                // Check if salary payment already exists for this month
                if (salaryPaymentExistsForMonth(user.getUserId(), yearMonth)) {
                    System.out.println("⚠️  Skipping " + user.getName() + " - Salary already paid for " + yearMonth);
                    skipCount++;
                    continue;
                }

                // Create salary payment
                Payment salaryPayment = new Payment();
                salaryPayment.setAmount(user.getMonthlySalary());
                salaryPayment.setType("SALARY");
                salaryPayment.setDescription("Monthly salary for " + yearMonth + " - " + user.getName());
                salaryPayment.setCategoryId(salaryCategoryId);
                salaryPayment.setStatusId(approvedStatusId); // Auto-approve salary payments
                salaryPayment.setCreatedByUserId(user.getUserId()); // Salary is "created by" the user receiving it
                salaryPayment.setTeamId(user.getTeamId());
                salaryPayment.setPaymentDate(LocalDateTime.now());

                // Save the payment
                Payment createdPayment = paymentRepository.save(salaryPayment);

                if (createdPayment != null) {
                    // Create audit trail using the existing logPaymentCreation method
                    User auditUser = new User();
                    auditUser.setUserId(user.getUserId());
                    auditService.logPaymentCreation(createdPayment, auditUser);

                    System.out.println("✓ Generated salary payment for " + user.getName() +
                        " - Amount: Rs." + user.getMonthlySalary() + " (Payment ID: " + createdPayment.getPaymentId() + ")");
                    successCount++;
                } else {
                    System.out.println("❌ Failed to create salary payment for " + user.getName());
                }

            } catch (Exception e) {
                System.out.println("❌ Error processing salary for " + user.getName() + ": " + e.getMessage());
            }
        }

        System.out.println("\n=== SALARY GENERATION COMPLETE ===");
        System.out.println("✓ Successfully generated: " + successCount + " payments");
        System.out.println("⚠️  Skipped: " + skipCount + " users");
        System.out.println("Total processed: " + (successCount + skipCount) + " users");
    }

    /**
     * Check if a salary payment already exists for a user in a specific month
     */
    private boolean salaryPaymentExistsForMonth(int userId, YearMonth yearMonth) throws SQLException {
        // Check if there's already a salary payment for this user this month
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        return paymentRepository.existsSalaryPaymentForUserInPeriod(userId, startOfMonth, endOfMonth);
    }

    /**
     * Get or create the "Salary" category
     */
    private Integer getSalaryCategoryId() throws SQLException {
        var salaryCategory = categoryRepository.findByName("Salary");
        if (salaryCategory != null) {
            return salaryCategory.getCategoryId();
        }

        // Create salary category if it doesn't exist
        var newCategory = categoryRepository.createCategory("Salary");
        return newCategory != null ? newCategory.getCategoryId() : 1; // Fallback to first category
    }

    /**
     * Get the APPROVED status ID
     */
    private Integer getApprovedStatusId() throws SQLException {
        var approvedStatus = statusRepository.findByName("APPROVED");
        return approvedStatus != null ? approvedStatus.getStatusId() : 2; // Fallback to status ID 2
    }

    /**
     * Update a user's monthly salary
     */
    public boolean updateUserSalary(int userId, BigDecimal monthlySalary, LocalDate effectiveDate) throws SQLException {
        return userRepository.updateUserSalary(userId, monthlySalary, effectiveDate);
    }

    /**
     * Get all users with salary information
     */
    public List<User> getUsersWithSalary() throws SQLException {
        return userRepository.findUsersWithSalary();
    }

    /**
     * Generate salary payments for a specific user for the current month
     */
    public boolean generateSalaryForUser(int userId) throws SQLException {
        var userOptional = userRepository.findByEmail(getUserEmailById(userId));
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        YearMonth currentMonth = YearMonth.now();

        // Check eligibility
        if (user.getMonthlySalary() == null || user.getMonthlySalary().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (salaryPaymentExistsForMonth(userId, currentMonth)) {
            return false; // Already paid this month
        }

        // Generate payment for this user only
        List<User> singleUserList = List.of(user);
        generateSalaryPaymentsForMonth(currentMonth);

        return true;
    }

    private String getUserEmailById(int userId) throws SQLException {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(u -> u.getUserId() == userId)
                .map(User::getEmail)
                .findFirst()
                .orElse("unknown@unknown.com");
    }
}
