package org.paymentmanagementsystem.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private BigDecimal amount;
    private String type;
    private LocalDateTime paymentDate;
    private String description;
    private int categoryId;
    private int statusId;
    private int createdByUserId;
    private Integer teamId; // Can be null for admin-created payments

    private Category category;
    private Status status;
    private User createdBy;
    private Team team;

    public Payment() {}

    public Payment(BigDecimal amount, String type, String description,
                   int categoryId, int statusId, int createdByUserId) {
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.categoryId = categoryId;
        this.statusId = statusId;
        this.createdByUserId = createdByUserId;
        this.paymentDate = LocalDateTime.now();
    }

    public Payment(BigDecimal amount, String type, String description,
                   int categoryId, int statusId, int createdByUserId, Integer teamId) {
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.categoryId = categoryId;
        this.statusId = statusId;
        this.createdByUserId = createdByUserId;
        this.teamId = teamId;
        this.paymentDate = LocalDateTime.now();
    }

    // Getters and Setters
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }

    public int getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(int createdByUserId) { this.createdByUserId = createdByUserId; }

    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", paymentDate=" + paymentDate +
                ", description='" + description + '\'' +
                ", categoryId=" + categoryId +
                ", statusId=" + statusId +
                ", createdByUserId=" + createdByUserId +
                ", teamId=" + teamId +
                '}';
    }
}
