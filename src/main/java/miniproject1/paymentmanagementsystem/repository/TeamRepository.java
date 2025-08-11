package miniproject1.paymentmanagementsystem.repository;

import miniproject1.paymentmanagementsystem.config.DatabaseConfig;
import miniproject1.paymentmanagementsystem.model.Team;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamRepository {
    private final DatabaseConfig dbConfig;

    public TeamRepository() throws Exception {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    public Team createTeam(Team team) throws SQLException {
        String sql = """
            INSERT INTO teams (team_name, created_by_user_id)
            VALUES (?, ?)
            RETURNING team_id, created_date
            """;

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, team.getTeamName());
                stmt.setInt(2, team.getCreatedByUserId());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        team.setTeamId(rs.getInt("team_id"));
                        team.setCreatedDate(rs.getTimestamp("created_date").toLocalDateTime());
                        return team;
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return null;
    }

    public Team findById(int teamId) throws SQLException {
        String sql = "SELECT * FROM teams WHERE team_id = ?";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, teamId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToTeam(rs);
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return null;
    }

    public Team findByName(String teamName) throws SQLException {
        String sql = "SELECT * FROM teams WHERE team_name = ?";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, teamName);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToTeam(rs);
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return null;
    }

    public List<Team> findByCreatedBy(int userId) throws SQLException {
        String sql = "SELECT * FROM teams WHERE created_by_user_id = ? ORDER BY created_date DESC";

        Connection conn = null;
        List<Team> teams = new ArrayList<>();
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        teams.add(mapResultSetToTeam(rs));
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return teams;
    }

    public List<Team> findAll() throws SQLException {
        String sql = "SELECT * FROM teams ORDER BY team_name";

        Connection conn = null;
        List<Team> teams = new ArrayList<>();
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        teams.add(mapResultSetToTeam(rs));
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return teams;
    }

    public boolean updateTeam(Team team) throws SQLException {
        String sql = "UPDATE teams SET team_name = ? WHERE team_id = ?";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, team.getTeamName());
                stmt.setInt(2, team.getTeamId());

                return stmt.executeUpdate() > 0;
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public boolean deleteTeam(int teamId) throws SQLException {
        String sql = "DELETE FROM teams WHERE team_id = ?";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, teamId);
                return stmt.executeUpdate() > 0;
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    private Team mapResultSetToTeam(ResultSet rs) throws SQLException {
        Team team = new Team();
        team.setTeamId(rs.getInt("team_id"));
        team.setTeamName(rs.getString("team_name"));
        team.setCreatedByUserId(rs.getInt("created_by_user_id"));

        Timestamp timestamp = rs.getTimestamp("created_date");
        if (timestamp != null) {
            team.setCreatedDate(timestamp.toLocalDateTime());
        }

        return team;
    }
}
