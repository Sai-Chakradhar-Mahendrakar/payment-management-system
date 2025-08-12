package miniproject2.paymentmanagementsystem.repository;

import miniproject2.paymentmanagementsystem.entity.Payment;
import miniproject2.paymentmanagementsystem.entity.User;
import miniproject2.paymentmanagementsystem.enums.Category;
import miniproject2.paymentmanagementsystem.enums.PaymentType;
import miniproject2.paymentmanagementsystem.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCreatedBy(User createdBy);
    List<Payment> findByStatus(Status status);
    List<Payment> findByCategory(Category category);
    List<Payment> findByPaymentType(PaymentType paymentType);

    @Query("SELECT p FROM Payment p WHERE p.date BETWEEN :startDate AND :endDate")
    List<Payment> findByDateBetween(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.createdBy.id = :userId AND p.status = :status")
    List<Payment> findByCreatedByIdAndStatus(@Param("userId") Long userId, @Param("status") Status status);
}
