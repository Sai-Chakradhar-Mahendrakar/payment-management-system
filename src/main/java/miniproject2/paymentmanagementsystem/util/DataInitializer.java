package miniproject2.paymentmanagementsystem.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniproject2.paymentmanagementsystem.entity.User;
import miniproject2.paymentmanagementsystem.enums.Role;
import miniproject2.paymentmanagementsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createDefaultUsers();
    }

    private void createDefaultUsers() {
        // Create Admin user if not exists
        if (!userRepository.existsByEmail("admin@payment.com")) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@payment.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            log.info("Created default admin user: admin@payment.com");
        }

        // Create Finance Manager user if not exists
        if (!userRepository.existsByEmail("finance@payment.com")) {
            User financeManager = new User();
            financeManager.setName("Finance Manager");
            financeManager.setEmail("finance@payment.com");
            financeManager.setPassword(passwordEncoder.encode("finance123"));
            financeManager.setRole(Role.FINANCE_MANAGER);
            userRepository.save(financeManager);
            log.info("Created default finance manager user: finance@payment.com");
        }

        // Create Viewer user if not exists
        if (!userRepository.existsByEmail("viewer@payment.com")) {
            User viewer = new User();
            viewer.setName("Payment Viewer");
            viewer.setEmail("viewer@payment.com");
            viewer.setPassword(passwordEncoder.encode("viewer123"));
            viewer.setRole(Role.VIEWER);
            userRepository.save(viewer);
            log.info("Created default viewer user: viewer@payment.com");
        }
    }
}
