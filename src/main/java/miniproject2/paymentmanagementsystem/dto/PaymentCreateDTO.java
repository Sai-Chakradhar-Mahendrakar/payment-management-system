package miniproject2.paymentmanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import miniproject2.paymentmanagementsystem.enums.Category;
import miniproject2.paymentmanagementsystem.enums.PaymentType;
import miniproject2.paymentmanagementsystem.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentCreateDTO {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Status is required")
    private Status status;

    @NotNull(message = "Date is required")
    private LocalDateTime date;

    private String description;
}
