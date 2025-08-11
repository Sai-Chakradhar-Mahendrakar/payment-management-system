package miniproject1.paymentmanagementsystem.repository;

import miniproject1.paymentmanagementsystem.config.DatabaseConfig;
import miniproject1.paymentmanagementsystem.model.Role;
import miniproject1.paymentmanagementsystem.model.Team;
import miniproject1.paymentmanagementsystem.model.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final DatabaseConfig dbConfig;

    public UserRepository() throws SQLException, IOException {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = """
            SELECT u.*, r.role_name, t.team_name 
            FROM users u 
            LEFT JOIN roles r ON u.role_id = r.role_id 
            LEFT JOIN teams t ON u.team_id = t.team_id 
            WHERE u.email = ?
            """;

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return Optional.empty();
    }

    public User save(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, role_id) VALUES (?, ?, ?, ?) RETURNING user_id";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setInt(4, user.getRoleId());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user.setUserId(rs.getInt("user_id"));
            }
            return user;
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public User createUser(User user) throws SQLException {
        String sql = """
            INSERT INTO users (name, email, password_hash, role_id, team_id, monthly_salary, salary_effective_date)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING user_id
            """;

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getPasswordHash());
                stmt.setInt(4, user.getRoleId());

                if (user.getTeamId() != null) {
                    stmt.setInt(5, user.getTeamId());
                } else {
                    stmt.setNull(5, Types.INTEGER);
                }

                if (user.getMonthlySalary() != null) {
                    stmt.setBigDecimal(6, user.getMonthlySalary());
                } else {
                    stmt.setBigDecimal(6, BigDecimal.ZERO);
                }

                if (user.getSalaryEffectiveDate() != null) {
                    stmt.setDate(7, Date.valueOf(user.getSalaryEffectiveDate()));
                } else {
                    stmt.setNull(7, Types.DATE);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt("user_id"));
                        return user;
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return null;
    }

    public List<User> findAll() throws SQLException {
        String sql = """
            SELECT u.user_id, u.name, u.email, u.password_hash, u.role_id,
                   r.role_name
            FROM users u
            JOIN roles r ON u.role_id = r.role_id
            """;

        Connection conn = null;
        List<User> users = new ArrayList<>();

        try {
            conn = dbConfig.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public List<User> findByTeamId(int teamId) throws SQLException {
        String sql = """
            SELECT u.*, r.role_name, t.team_name 
            FROM users u 
            LEFT JOIN roles r ON u.role_id = r.role_id 
            LEFT JOIN teams t ON u.team_id = t.team_id 
            WHERE u.team_id = ?
            ORDER BY u.name
            """;

        Connection conn = null;
        List<User> users = new ArrayList<>();
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, teamId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToUser(rs));
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return users;
    }

    public void deleteById(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public List<User> findAvailableViewers() throws SQLException {
        String sql = """
            SELECT u.*, r.role_name, t.team_name 
            FROM users u 
            LEFT JOIN roles r ON u.role_id = r.role_id 
            LEFT JOIN teams t ON u.team_id = t.team_id 
            WHERE r.role_name = 'viewer' AND u.team_id IS NULL
            ORDER BY u.name
            """;

        Connection conn = null;
        List<User> users = new ArrayList<>();
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToUser(rs));
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return users;
    }

    public boolean assignUserToTeam(int userId, Integer teamId) throws SQLException {
        String sql = "UPDATE users SET team_id = ? WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (teamId != null) {
                    stmt.setInt(1, teamId);
                } else {
                    stmt.setNull(1, Types.INTEGER);
                }
                stmt.setInt(2, userId);

                return stmt.executeUpdate() > 0;
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public boolean updateUserSalary(int userId, BigDecimal monthlySalary, LocalDate effectiveDate) throws SQLException {
        String sql = "UPDATE users SET monthly_salary = ?, salary_effective_date = ? WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBigDecimal(1, monthlySalary);
                stmt.setDate(2, Date.valueOf(effectiveDate));
                stmt.setInt(3, userId);

                return stmt.executeUpdate() > 0;
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public List<User> findUsersWithSalary() throws SQLException {
        String sql = """
            SELECT u.*, r.role_name, t.team_name 
            FROM users u 
            LEFT JOIN roles r ON u.role_id = r.role_id 
            LEFT JOIN teams t ON u.team_id = t.team_id 
            WHERE u.monthly_salary > 0 AND u.salary_effective_date IS NOT NULL
            ORDER BY u.name
            """;

        Connection conn = null;
        List<User> users = new ArrayList<>();
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToUser(rs));
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return users;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRoleId(rs.getInt("role_id"));

        // Handle team_id which can be null
        int teamId = rs.getInt("team_id");
        if (!rs.wasNull()) {
            user.setTeamId(teamId);
        }

        // Handle salary fields
        BigDecimal monthlySalary = rs.getBigDecimal("monthly_salary");
        if (monthlySalary != null) {
            user.setMonthlySalary(monthlySalary);
        }

        Date salaryEffectiveDate = rs.getDate("salary_effective_date");
        if (salaryEffectiveDate != null) {
            user.setSalaryEffectiveDate(salaryEffectiveDate.toLocalDate());
        }

        // Set role information
        Role role = new Role();
        role.setRoleId(user.getRoleId());
        role.setRoleName(rs.getString("role_name"));
        user.setRole(role);

        // Set team information if available
        String teamName = rs.getString("team_name");
        if (teamName != null) {
            Team team = new Team();
            team.setTeamId(user.getTeamId());
            team.setTeamName(teamName);
            user.setTeam(team);
        }

        return user;
    }
}
