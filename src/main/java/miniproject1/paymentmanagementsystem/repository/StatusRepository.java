package miniproject1.paymentmanagementsystem.repository;

import miniproject1.paymentmanagementsystem.config.DatabaseConfig;
import miniproject1.paymentmanagementsystem.model.Status;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StatusRepository {
    private final DatabaseConfig dbConfig;

    public StatusRepository() throws SQLException, IOException {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    public List<Status> findAll() throws SQLException {
        String sql = "SELECT status_id, status_name FROM status";

        Connection conn = null;
        List<Status> statusList = new ArrayList<>();

        try {
            conn = dbConfig.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Status status = new Status();
                status.setStatusId(rs.getInt("status_id"));
                status.setStatusName(rs.getString("status_name"));
                statusList.add(status);
            }
            return statusList;
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public Optional<Status> findById(int statusId) throws SQLException {
        String sql = "SELECT status_id, status_name FROM status WHERE status_id = ?";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, statusId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Status status = new Status();
                status.setStatusId(rs.getInt("status_id"));
                status.setStatusName(rs.getString("status_name"));
                return Optional.of(status);
            }
            return Optional.empty();
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public Status findByName(String statusName) throws SQLException {
        String sql = "SELECT * FROM status WHERE status_name = ?";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, statusName);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Status status = new Status();
                        status.setStatusId(rs.getInt("status_id"));
                        status.setStatusName(rs.getString("status_name"));
                        return status;
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return null;
    }
}
