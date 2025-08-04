package org.paymentmanagementsystem.dto;

import java.math.BigDecimal;

public class PaymentDTO {
    private BigDecimal amount;
    private String type;
    private String description;
    private int categoryId;

    public PaymentDTO() {}

    public PaymentDTO(BigDecimal amount, String type, String description, int categoryId) {
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.categoryId = categoryId;
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
}
