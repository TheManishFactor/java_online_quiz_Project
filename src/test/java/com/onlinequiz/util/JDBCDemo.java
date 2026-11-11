package com.onlinequiz.util;

import com.onlinequiz.database.DatabaseConnection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Step-by-step JDBC demonstration on a dedicated table, so the real quiz data is
 * never touched.
 *
 * Run with:
 * java -cp "target/classes;target/test-classes;<dependency-classpath>" com.onlinequiz.util.JDBCDemo
 */
public final class JDBCDemo {

    private static final String DEMO_DATABASE = "target/jdbc_demo.db";
    private static final String INSERT_SQL =
            "INSERT INTO jdbc_demo_items(name, quantity, price) VALUES (?, ?, ?)";
    private static final String SELECT_SQL =
            "SELECT id, name, quantity, price FROM jdbc_demo_items ORDER BY id";

    private JDBCDemo() {
    }

    public static void main(String[] args) {
        try {
            prepareDemoDatabase();
            loadDriver();

            Connection connection = DatabaseConnection.getConnection(DEMO_DATABASE);
            try (connection) {
                createDemoTable(connection);
                insertDemoRows(connection);
                selectRows(connection, "after INSERT");
                updateDemoRow(connection);
                selectRows(connection, "after UPDATE");
                deleteDemoRow(connection);
                selectRows(connection, "after DELETE");
                demonstrateCommittedTransaction(connection);
                demonstrateRollback(connection);
                handleDuplicateKey(connection);
                selectRows(connection, "final contents");
                System.out.println();
                System.out.println("10. Every statement and result set above was closed"
                        + " by try-with-resources.");
            }
            System.out.println("10. Connection closed: " + connection.isClosed());
        } catch (IOException | ClassNotFoundException | SQLException error) {
            System.err.println("JDBC DEMO FAILED: " + error.getMessage());
            error.printStackTrace();
            System.exit(1);
        }
    }

    /** 1. JDBC 4 loads drivers automatically, but explicit loading is educational. */
    private static void loadDriver() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        System.out.println("1. JDBC driver loaded: org.sqlite.JDBC");
    }

    /** 3. DDL is executed with a Statement because no user values are involved. */
    private static void createDemoTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS jdbc_demo_items");
            statement.executeUpdate(
                    "CREATE TABLE jdbc_demo_items ("
                            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "name TEXT NOT NULL UNIQUE,"
                            + "quantity INTEGER NOT NULL,"
                            + "price REAL NOT NULL)");
        }
        System.out.println("2. Connection established and table jdbc_demo_items created.");
    }

    /** 4. INSERT uses placeholders so values cannot inject SQL. */
    private static void insertDemoRows(Connection connection) throws SQLException {
        insertItem(connection, "Notebook", 20, 3.50);
        insertItem(connection, "Pen", 50, 1.20);
        insertItem(connection, "Marker", 15, 2.00);
        System.out.println("3. PreparedStatement inserted three demo items.");
    }

    /** 5. SELECT reads rows through a ResultSet cursor. */
    private static void selectRows(Connection connection, String stage) throws SQLException {
        System.out.println("4. Rows " + stage + ":");
        try (PreparedStatement statement = connection.prepareStatement(SELECT_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                System.out.println("   #" + resultSet.getInt("id")
                        + " " + resultSet.getString("name")
                        + " qty=" + resultSet.getInt("quantity")
                        + " price=" + resultSet.getDouble("price"));
            }
        }
    }

    /** 6. UPDATE changes existing rows and reports how many were affected. */
    private static void updateDemoRow(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE jdbc_demo_items SET quantity = ?, price = ? WHERE name = ?")) {
            statement.setInt(1, 40);
            statement.setDouble(2, 1.10);
            statement.setString(3, "Pen");
            int rows = statement.executeUpdate();
            System.out.println("5. UPDATE affected " + rows + " row(s).");
        }
    }

    /** 7. DELETE removes a row by a parameterized condition. */
    private static void deleteDemoRow(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM jdbc_demo_items WHERE name = ?")) {
            statement.setString(1, "Marker");
            int rows = statement.executeUpdate();
            System.out.println("6. DELETE affected " + rows + " row(s).");
        }
    }

    /** 8a. Two inserts that belong together are committed as one transaction. */
    private static void demonstrateCommittedTransaction(Connection connection)
            throws SQLException {
        connection.setAutoCommit(false);
        try {
            insertItem(connection, "Eraser", 25, 0.80);
            insertItem(connection, "Ruler", 10, 1.50);
            connection.commit();
            System.out.println("7. commit(): Eraser and Ruler were saved together.");
        } catch (SQLException error) {
            connection.rollback();
            throw error;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /** 8b. A failure inside a transaction rolls back every statement in it. */
    private static void demonstrateRollback(Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        try {
            insertItem(connection, "Glue", 5, 2.40);
            insertItem(connection, "Glue", 6, 2.40);
            connection.commit();
            System.out.println("8. Unexpected success: duplicate Glue was accepted.");
        } catch (SQLException error) {
            connection.rollback();
            System.out.println("8. rollback(): duplicate rejected, Glue was not saved.");
            System.out.println("   Reason: " + error.getMessage());
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /** 9. SQLException is caught and reported instead of terminating the program. */
    private static void handleDuplicateKey(Connection connection) {
        try {
            insertItem(connection, "Ruler", 1, 1.00);
            System.out.println("9. Unexpected success: duplicate Ruler was accepted.");
        } catch (SQLException error) {
            System.out.println("9. SQLException handled: " + error.getMessage());
        }
    }

    private static void insertItem(Connection connection, String name, int quantity,
                                   double price) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, name);
            statement.setInt(2, quantity);
            statement.setDouble(3, price);
            statement.executeUpdate();
        }
    }

    private static void prepareDemoDatabase() throws IOException {
        Files.createDirectories(Paths.get("target"));
        deleteIfPresent(DEMO_DATABASE);
        deleteIfPresent(DEMO_DATABASE + "-journal");
        deleteIfPresent(DEMO_DATABASE + "-wal");
        deleteIfPresent(DEMO_DATABASE + "-shm");
    }

    private static void deleteIfPresent(String file) throws IOException {
        Path path = Paths.get(file);
        Files.deleteIfExists(path);
    }
}
