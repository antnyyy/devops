package com.napier.sem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private Connection connection;
    private static final int MAX_RETRIES = 5;

    public void connect(String url, String username, String password) throws SQLException {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                System.out.println("Connecting to database...");
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Successfully connected");
                return;
            } catch (SQLException e) {
                System.err.println("Failed to connect attempt " + attempt + ": " + e.getMessage());
                if (attempt == MAX_RETRIES) {
                    throw new SQLException("Failed to connect after " + MAX_RETRIES + " attempts", e);
                }
                try {
                    Thread.sleep(1000); // Wait 1 second before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("No valid database connection available");
        }
        return connection;
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Disconnected.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}