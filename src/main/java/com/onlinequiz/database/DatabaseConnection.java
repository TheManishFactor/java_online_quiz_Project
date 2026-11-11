package com.onlinequiz.database;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates SQLite JDBC connections.
 *
 * SQLite has no server: a database is just a file, and the JDBC driver creates
 * that file automatically the first time it is opened. Every connection enables
 * foreign-key enforcement, because SQLite disables it by default.
 */
public final class DatabaseConnection {

    public static final String DEFAULT_DATABASE_FILE = "online_quiz.db";

    private static final String JDBC_PREFIX = "jdbc:sqlite:";
    private static final String DRIVER_CLASS = "org.sqlite.JDBC";

    private static volatile boolean driverLoaded;

    private DatabaseConnection() {
    }

    /**
     * Opens the application database ({@code online_quiz.db} in the working
     * directory).
     */
    public static Connection getConnection() throws SQLException {
        return getConnection(DEFAULT_DATABASE_FILE);
    }

    /**
     * Opens a specific database file. Relative paths are resolved against the
     * working directory; the file is created when it does not exist.
     */
    public static Connection getConnection(String databaseFile) throws SQLException {
        String location = resolveLocation(databaseFile);
        loadDriver();

        Connection connection = DriverManager.getConnection(JDBC_PREFIX + location);
        try {
            configure(connection);
            return connection;
        } catch (SQLException error) {
            try {
                connection.close();
            } catch (SQLException closeError) {
                error.addSuppressed(closeError);
            }
            throw error;
        }
    }

    /**
     * JDBC 4 discovers drivers automatically, but loading it explicitly makes the
     * dependency visible and gives a clear error when the driver is missing.
     */
    private static void loadDriver() throws SQLException {
        if (driverLoaded) {
            return;
        }
        synchronized (DatabaseConnection.class) {
            if (driverLoaded) {
                return;
            }
            try {
                Class.forName(DRIVER_CLASS);
                driverLoaded = true;
            } catch (ClassNotFoundException error) {
                throw new SQLException("SQLite JDBC driver is not on the classpath", error);
            }
        }
    }

    private static void configure(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        }
    }

    private static String resolveLocation(String databaseFile) throws SQLException {
        if (databaseFile == null || databaseFile.trim().isEmpty()) {
            throw new SQLException("Database file must be provided");
        }
        String location = databaseFile.trim();
        if (":memory:".equals(location) || location.startsWith("file:")) {
            return location;
        }
        return Paths.get(location)
                .toAbsolutePath()
                .normalize()
                .toString()
                .replace('\\', '/');
    }
}
