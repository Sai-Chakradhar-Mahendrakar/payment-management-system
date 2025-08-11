package miniproject1.paymentmanagementsystem.util;

import java.sql.*;
import java.util.Properties;

public class DatabaseInitializer {

    public static void initializeDatabase(Properties dbProperties) throws SQLException, ClassNotFoundException {
        String fullUrl = dbProperties.getProperty("db.url");
        String username = dbProperties.getProperty("db.username");
        String password = dbProperties.getProperty("db.password");
        String driver = dbProperties.getProperty("db.driver");

        // Load PostgreSQL driver
        Class.forName(driver);

        // Extract database name
        String[] urlParts = fullUrl.split("/");
        String databaseName = urlParts[urlParts.length - 1];
        String postgresUrl = fullUrl.substring(0, fullUrl.lastIndexOf('/')) + "/postgres";

        System.out.println("Checking database existence...");

        // Connect to postgres database to create target database if needed
        try (Connection conn = DriverManager.getConnection(postgresUrl, username, password);
             Statement stmt = conn.createStatement()) {

            // Check if database exists
            String checkDbQuery = "SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'";
            boolean dbExists = stmt.executeQuery(checkDbQuery).next();

            if (!dbExists) {
                System.out.println("Creating database: " + databaseName);
                stmt.executeUpdate("CREATE DATABASE " + databaseName);
                System.out.println("Database created successfully!");
            } else {
                System.out.println("Database already exists: " + databaseName);
            }
        }

        // Now connect to the target database and create schema
        try (Connection conn = DriverManager.getConnection(fullUrl, username, password)) {
            boolean isFirstTimeSetup = !schemaExists(conn);

            System.out.println("First time setup: " + isFirstTimeSetup);

            if (isFirstTimeSetup) {
                System.out.println("Creating database schema...");
                createSchema(conn);
                System.out.println("Database schema created successfully!");

                // Create default admin user on first setup
                createDefaultAdminUser(conn);
            } else {
                System.out.println("Database schema already exists.");
            }
        }
    }

    private static boolean schemaExists(Connection conn) throws SQLException {
        String checkQuery = """
            SELECT COUNT(*) FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_name IN ('roles', 'teams', 'users', 'categories', 'status', 'payments', 'audit_trail')
            """;

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(checkQuery);
            if (rs.next()) {
                return rs.getInt(1) == 7; // All 7 tables must exist (including teams)
            }
        }
        return false;
    }

    private static void createSchema(Connection conn) throws SQLException {
        String[] schemaStatements = {
                // Create roles table
                """
            CREATE TABLE roles (
                role_id SERIAL PRIMARY KEY,
                role_name VARCHAR(50) NOT NULL UNIQUE
            )
            """,

                // Create teams table (without foreign key initially)
                """
            CREATE TABLE teams (
                team_id SERIAL PRIMARY KEY,
                team_name VARCHAR(100) NOT NULL UNIQUE,
                created_by_user_id INTEGER NOT NULL,
                created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,

                // Create users table
                """
            CREATE TABLE users (
                user_id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                email VARCHAR(100) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                role_id INTEGER NOT NULL,
                team_id INTEGER,
                monthly_salary DECIMAL(15,2) DEFAULT 0.00,
                salary_effective_date DATE,
                FOREIGN KEY (role_id) REFERENCES roles(role_id),
                FOREIGN KEY (team_id) REFERENCES teams(team_id)
            )
            """,

                // Create categories table
                """
            CREATE TABLE categories (
                category_id SERIAL PRIMARY KEY,
                category_name VARCHAR(100) NOT NULL UNIQUE
            )
            """,

                // Create status table
                """
            CREATE TABLE status (
                status_id SERIAL PRIMARY KEY,
                status_name VARCHAR(50) NOT NULL UNIQUE
            )
            """,

                // Create payments table
                """
            CREATE TABLE payments (
                payment_id SERIAL PRIMARY KEY,
                amount DECIMAL(15,2) NOT NULL,
                type VARCHAR(50) NOT NULL,
                payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                description TEXT,
                category_id INTEGER NOT NULL,
                status_id INTEGER NOT NULL,
                created_by_user_id INTEGER NOT NULL,
                team_id INTEGER,
                FOREIGN KEY (category_id) REFERENCES categories(category_id),
                FOREIGN KEY (status_id) REFERENCES status(status_id),
                FOREIGN KEY (created_by_user_id) REFERENCES users(user_id),
                FOREIGN KEY (team_id) REFERENCES teams(team_id)
            )
            """,

                // Create audit_trail table
                """
            CREATE TABLE audit_trail (
                audit_id SERIAL PRIMARY KEY,
                payment_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                action VARCHAR(50) NOT NULL,
                change_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                old_value TEXT,
                new_value TEXT,
                FOREIGN KEY (payment_id) REFERENCES payments(payment_id),
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            )
            """,

                // Add foreign key constraint to teams table (after users table is created)
                """
            ALTER TABLE teams 
            ADD CONSTRAINT fk_teams_created_by 
            FOREIGN KEY (created_by_user_id) REFERENCES users(user_id)
            """,

                // Insert default roles
                """
            INSERT INTO roles (role_name) VALUES 
            ('admin'),
            ('finance_manager'),
            ('viewer')
            """,

                // Insert default categories
                """
            INSERT INTO categories (category_name) VALUES 
            ('Office Supplies'),
            ('Travel'),
            ('Equipment'),
            ('Software'),
            ('Utilities'),
            ('Vendor Payment'),
            ('Client Invoice')
            """,

                // Insert default status values
                """
            INSERT INTO status (status_name) VALUES 
            ('PENDING'),
            ('APPROVED'),
            ('REJECTED')
            """,

                // Create indexes for better performance
                "CREATE INDEX idx_payments_created_by ON payments(created_by_user_id)",
                "CREATE INDEX idx_payments_status ON payments(status_id)",
                "CREATE INDEX idx_payments_date ON payments(payment_date)",
                "CREATE INDEX idx_payments_team ON payments(team_id)",
                "CREATE INDEX idx_users_team ON users(team_id)",
                "CREATE INDEX idx_audit_trail_payment ON audit_trail(payment_id)"
        };

        try (Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);

            for (String statement : schemaStatements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.executeUpdate(trimmed);
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            conn.rollback();
            conn.setAutoCommit(true);
            throw e;
        }
    }

    private static void createDefaultAdminUser(Connection conn) throws SQLException {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("FIRST TIME SETUP - CREATING DEFAULT ADMIN USER");
        System.out.println("=".repeat(50));

        try {
            String hashedPassword = PasswordUtil.hashPassword("admin@123");

            String insertAdminQuery = """
                INSERT INTO users (name, email, password_hash, role_id) 
                VALUES (?, ?, ?, (SELECT role_id FROM roles WHERE role_name = 'admin'))
                """;

            try (PreparedStatement stmt = conn.prepareStatement(insertAdminQuery)) {
                stmt.setString(1, "admin");
                stmt.setString(2, "admin@admin.tech");
                stmt.setString(3, hashedPassword);

                int rowsInserted = stmt.executeUpdate();

                if (rowsInserted > 0) {
                    System.out.println("✓ Default admin user created successfully!");
                    System.out.println();
                    System.out.println("DEFAULT ADMIN CREDENTIALS:");
                    System.out.println("Name:     admin");
                    System.out.println("Email:    admin@admin.tech");
                    System.out.println("Password: admin@123");
                    System.out.println("Role:     admin");
                    System.out.println();
                    System.out.println("⚠️  IMPORTANT: Please change the default password after first login!");
                    System.out.println("=".repeat(50));
                    System.out.println();
                } else {
                    throw new SQLException("No rows inserted for default admin user");
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to create default admin user: " + e.getMessage());
            throw new SQLException("Error creating default admin user", e);
        }
    }
}