package org.paymentmanagementsystem.repository;

import org.paymentmanagementsystem.config.DatabaseConfig;
import org.paymentmanagementsystem.model.AuditTrail;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditTrailRepository {
    private final DatabaseConfig dbConfig;

    public AuditTrailRepository() throws SQLException, IOException {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    public AuditTrail save(AuditTrail auditTrail) throws SQLException {
        String sql = """
            INSERT INTO audit_trail (payment_id, user_id, action, change_timestamp, old_value, new_value)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING audit_id
            """;

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, auditTrail.getPaymentId());
            stmt.setInt(2, auditTrail.getUserId());
            stmt.setString(3, auditTrail.getAction());
            stmt.setTimestamp(4, Timestamp.valueOf(auditTrail.getChangeTimestamp()));
            stmt.setString(5, auditTrail.getOldValue());
            stmt.setString(6, auditTrail.getNewValue());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                auditTrail.setAuditId(rs.getInt("audit_id"));
            }
            return auditTrail;
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public List<AuditTrail> findByPaymentId(int paymentId) throws SQLException {
        String sql = """
            SELECT audit_id, payment_id, user_id, action, change_timestamp, old_value, new_value
            FROM audit_trail
            WHERE payment_id = ?
            ORDER BY change_timestamp DESC
            """;

        Connection conn = null;
        List<AuditTrail> auditTrails = new ArrayList<>();

        try {
            conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, paymentId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AuditTrail auditTrail = new AuditTrail();
                auditTrail.setAuditId(rs.getInt("audit_id"));
                auditTrail.setPaymentId(rs.getInt("payment_id"));
                auditTrail.setUserId(rs.getInt("user_id"));
                auditTrail.setAction(rs.getString("action"));
                auditTrail.setChangeTimestamp(rs.getTimestamp("change_timestamp").toLocalDateTime());
                auditTrail.setOldValue(rs.getString("old_value"));
                auditTrail.setNewValue(rs.getString("new_value"));
                auditTrails.add(auditTrail);
            }
            return auditTrails;
        } finally {
            dbConfig.returnConnection(conn);
        }
    }
}
