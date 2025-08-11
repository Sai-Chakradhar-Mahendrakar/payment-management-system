package miniproject1.paymentmanagementsystem.service;

import miniproject1.paymentmanagementsystem.model.AuditTrail;
import miniproject1.paymentmanagementsystem.model.Payment;
import miniproject1.paymentmanagementsystem.model.User;
import miniproject1.paymentmanagementsystem.repository.AuditTrailRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuditService {
    private final AuditTrailRepository auditTrailRepository;
    private final ExecutorService executorService;

    public AuditService() throws SQLException, IOException {
        this.auditTrailRepository = new AuditTrailRepository();
        this.executorService = Executors.newFixedThreadPool(3);
    }

    public CompletableFuture<Void> logPaymentCreation(Payment payment, User user) {
        return CompletableFuture.runAsync(() -> {
            try {
                AuditTrail auditTrail = new AuditTrail(
                        payment.getPaymentId(),
                        user.getUserId(),
                        "CREATE",
                        null,
                        "Payment created with amount: " + payment.getAmount()
                );
                auditTrailRepository.save(auditTrail);
            } catch (SQLException e) {
                // Log error (you might want to use a logging framework)
                System.err.println("Failed to log payment creation: " + e.getMessage());
            }
        }, executorService);
    }

    public CompletableFuture<Void> logStatusChange(Payment payment, User user, String newStatus) {
        return CompletableFuture.runAsync(() -> {
            try {
                AuditTrail auditTrail = new AuditTrail(
                        payment.getPaymentId(),
                        user.getUserId(),
                        "STATUS_CHANGE",
                        payment.getStatus().getStatusName(),
                        newStatus
                );
                auditTrailRepository.save(auditTrail);
            } catch (SQLException e) {
                // Log error
                System.err.println("Failed to log status change: " + e.getMessage());
            }
        }, executorService);
    }

    public CompletableFuture<Void> logPaymentDeletion(Payment payment, User user) {
        return CompletableFuture.runAsync(() -> {
            try {
                AuditTrail auditTrail = new AuditTrail(
                        payment.getPaymentId(),
                        user.getUserId(),
                        "DELETE",
                        "Payment with amount: " + payment.getAmount(),
                        null
                );
                auditTrailRepository.save(auditTrail);
            } catch (SQLException e) {
                // Log error
                System.err.println("Failed to log payment deletion: " + e.getMessage());
            }
        }, executorService);
    }

    public List<AuditTrail> getPaymentAuditHistory(int paymentId) throws SQLException {
        return auditTrailRepository.findByPaymentId(paymentId);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}

