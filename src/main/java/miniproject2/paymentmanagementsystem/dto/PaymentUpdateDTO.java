package miniproject2.paymentmanagementsystem.dto;

import lombok.Data;
import miniproject2.paymentmanagementsystem.enums.Category;
import miniproject2.paymentmanagementsystem.enums.PaymentType;
import miniproject2.paymentmanagementsystem.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentUpdateDTO {
    private BigDecimal amount;
    private PaymentType paymentType;
    private Category category;
    private Status status;
    private LocalDateTime date;
    private String description;
}
