package miniproject2.paymentmanagementsystem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@Slf4j
public class PaymentManagementSystem {

    public static void main(String[] args) {
        log.info("Starting Payment Management System application...");
        try {
            SpringApplication.run(PaymentManagementSystem.class, args);
            log.info("Payment Management System application started successfully");
        } catch (Exception e) {
            log.error("Failed to start Payment Management System application", e);
            throw e;
        }
    }
}
