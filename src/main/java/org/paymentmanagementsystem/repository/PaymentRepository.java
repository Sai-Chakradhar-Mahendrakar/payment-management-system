package org.paymentmanagementsystem.repository;

import org.paymentmanagementsystem.config.DatabaseConfig;
import org.paymentmanagementsystem.model.Category;
import org.paymentmanagementsystem.model.Payment;
import org.paymentmanagementsystem.model.Status;
import org.paymentmanagementsystem.model.User;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentRepository {
    private final DatabaseConfig dbConfig;

    public PaymentRepository() throws SQLException, IOException {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    public Payment save(Payment payment) throws SQLException {
        String sql = """
            INSERT INTO payments (amount, type, payment_date, description, category_id, status_id, created_by_user_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING payment_id
            """;

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBigDecimal(1, payment.getAmount());
            stmt.setString(2, payment.getType());
            stmt.setTimestamp(3, Timestamp.valueOf(payment.getPaymentDate()));
            stmt.setString(4, payment.getDescription());
            stmt.setInt(5, payment.getCategoryId());
            stmt.setInt(6, payment.getStatusId());
            stmt.setInt(7, payment.getCreatedByUserId());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                payment.setPaymentId(rs.getInt("payment_id"));
            }
            return payment;
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public Optional<Payment> findById(int paymentId) throws SQLException {
        String sql = """
            SELECT p.payment_id, p.amount, p.type, p.payment_date, p.description,
                   p.category_id, p.status_id, p.created_by_user_id,
                   c.category_name, s.status_name, u.name as created_by_name
            FROM payments p
            JOIN categories c ON p.category_id = c.category_id
            JOIN status s ON p.status_id = s.status_id
            JOIN users u ON p.created_by_user_id = u.user_id
            WHERE p.payment_id = ?
            """;

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, paymentId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Payment payment = mapResultSetToPayment(rs);
                return Optional.of(payment);
            }
            return Optional.empty();
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public List<Payment> findAll() throws SQLException {
        String sql = """
            SELECT p.payment_id, p.amount, p.type, p.payment_date, p.description,
                   p.category_id, p.status_id, p.created_by_user_id,
                   c.category_name, s.status_name, u.name as created_by_name
            FROM payments p
            JOIN categories c ON p.category_id = c.category_id
            JOIN status s ON p.status_id = s.status_id
            JOIN users u ON p.created_by_user_id = u.user_id
            ORDER BY p.payment_date DESC
            """;

        Connection conn = null;
        List<Payment> payments = new ArrayList<>();

        try {
            conn = dbConfig.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
            return payments;
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public List<Payment> findByUserId(int userId) throws SQLException {
        String sql = """
            SELECT p.payment_id, p.amount, p.type, p.payment_date, p.description,
                   p.category_id, p.status_id, p.created_by_user_id,
                   c.category_name, s.status_name, u.name as created_by_name
            FROM payments p
            JOIN categories c ON p.category_id = c.category_id
            JOIN status s ON p.status_id = s.status_id
            JOIN users u ON p.created_by_user_id = u.user_id
            WHERE p.created_by_user_id = ?
            ORDER BY p.payment_date DESC
            """;

        Connection conn = null;
        List<Payment> payments = new ArrayList<>();

        try {
            conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
            return payments;
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public void updateStatus(int paymentId, int statusId) throws SQLException {
        String sql = "UPDATE payments SET status_id = ? WHERE payment_id = ?";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, statusId);
            stmt.setInt(2, paymentId);
            stmt.executeUpdate();
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public void deleteById(int paymentId) throws SQLException {
        String sql = "DELETE FROM payments WHERE payment_id = ?";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, paymentId);
            stmt.executeUpdate();
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public boolean existsSalaryPaymentForUserInPeriod(int userId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM payments 
            WHERE created_by_user_id = ? 
            AND type = 'SALARY' 
            AND payment_date >= ? 
            AND payment_date <= ?
            """;

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setTimestamp(2, Timestamp.valueOf(startDate.atStartOfDay()));
                stmt.setTimestamp(3, Timestamp.valueOf(endDate.atTime(23, 59, 59)));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return false;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setType(rs.getString("type"));
        payment.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
        payment.setDescription(rs.getString("description"));
        payment.setCategoryId(rs.getInt("category_id"));
        payment.setStatusId(rs.getInt("status_id"));
        payment.setCreatedByUserId(rs.getInt("created_by_user_id"));

        // Set related objects
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setCategoryName(rs.getString("category_name"));
        payment.setCategory(category);

        Status status = new Status();
        status.setStatusId(rs.getInt("status_id"));
        status.setStatusName(rs.getString("status_name"));
        payment.setStatus(status);

        User createdBy = new User();
        createdBy.setUserId(rs.getInt("created_by_user_id"));
        createdBy.setName(rs.getString("created_by_name"));
        payment.setCreatedBy(createdBy);

        return payment;
    }
}
