package org.paymentmanagementsystem.model;

import java.time.LocalDateTime;

public class AuditTrail {
    private int auditId;
    private int paymentId;
    private int userId;
    private String action;
    private LocalDateTime changeTimestamp;
    private String oldValue;
    private String newValue;

    public AuditTrail() {}

    public AuditTrail(int paymentId, int userId, String action, String oldValue, String newValue) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeTimestamp = LocalDateTime.now();
    }

    public int getAuditId() { return auditId; }
    public void setAuditId(int auditId) { this.auditId = auditId; }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getChangeTimestamp() { return changeTimestamp; }
    public void setChangeTimestamp(LocalDateTime changeTimestamp) { this.changeTimestamp = changeTimestamp; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
}

