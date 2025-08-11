package miniproject1.paymentmanagementsystem.config;

import miniproject1.paymentmanagementsystem.util.DatabaseInitializer;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class DatabaseConfig {
    private static final String PROPERTIES_FILE = "/database.properties";
    private static DatabaseConfig instance;
    private final Properties properties;
    private final BlockingQueue<Connection> connectionPool;

    private DatabaseConfig() throws SQLException, IOException {
        properties = loadProperties();

        // Use DatabaseInitializer for complete database setup
        try {
            DatabaseInitializer.initializeDatabase(properties);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }

        int poolSize = Integer.parseInt(properties.getProperty("db.pool.size", "10"));
        connectionPool = new ArrayBlockingQueue<>(poolSize);
        initializePool(poolSize);
    }

    public static synchronized DatabaseConfig getInstance() throws SQLException, IOException {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new IOException("Unable to find " + PROPERTIES_FILE);
            }
            props.load(input);
        }
        return props;
    }

    private void initializePool(int poolSize) throws SQLException {
        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        for (int i = 0; i < poolSize; i++) {
            Connection connection = DriverManager.getConnection(url, username, password);
            connectionPool.offer(connection);
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            return connectionPool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Connection pool interrupted", e);
        }
    }

    public void returnConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connectionPool.offer(connection);
                }
            } catch (SQLException e) {
                System.err.println("Error returning connection to pool: " + e.getMessage());
            }
        }
    }

    public void closeAllConnections() {
        while (!connectionPool.isEmpty()) {
            try {
                Connection conn = connectionPool.poll();
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
