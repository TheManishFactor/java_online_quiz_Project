package com.onlinequiz.repository;

import com.onlinequiz.database.DatabaseConnection;
import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.model.Admin;
import com.onlinequiz.model.Student;
import com.onlinequiz.model.User;
import com.onlinequiz.util.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite persistence for accounts. Passwords arrive here already hashed; the
 * repository never logs or prints a hash and returns hashes only as part of the
 * user model required for authentication.
 */
public class UserRepository {

    private final String databaseFile;

    public UserRepository() {
        this(DatabaseConnection.DEFAULT_DATABASE_FILE);
    }

    public UserRepository(String databaseFile) {
        this.databaseFile = databaseFile;
    }

    public User registerUser(User user) throws SQLException {
        requireHashedPassword(user);
        String sql = "INSERT INTO users(name, email, password_hash, role)"
                + " VALUES (?, ?, ?, ?)";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole());

            JdbcSupport.requireSingleRow(statement.executeUpdate(), "User insert");
            user.setId(JdbcSupport.lastInsertId(connection));
            return user;
        }
    }

    public User findUserByEmail(String email) throws SQLException {
        String sql = "SELECT id, name, email, password_hash, role"
                + " FROM users WHERE email = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        }
    }

    public User findUserById(int userId) throws SQLException {
        String sql = "SELECT id, name, email, password_hash, role"
                + " FROM users WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        }
    }

    public boolean updateUser(User user) throws SQLException {
        requireHashedPassword(user);
        String sql = "UPDATE users SET name = ?, email = ?, password_hash = ?, role = ?"
                + " WHERE id = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole());
            statement.setInt(5, user.getId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(int userId) throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM users WHERE id = ?")) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        }
    }

    public int countAdmins() throws SQLException {
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM users WHERE role = ?")) {
            statement.setString(1, User.ROLE_ADMIN);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT id, name, email, password_hash, role FROM users ORDER BY id";
        List<User> users = new ArrayList<>();
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapUser(resultSet));
            }
        }
        return users;
    }

    private Connection openConnection() throws SQLException {
        return DatabaseConnection.getConnection(databaseFile);
    }

    /**
     * Last line of defence before persistence: the service hashes passwords, and
     * this guard prevents a direct repository caller from storing plain text.
     */
    private void requireHashedPassword(User user) {
        if (!PasswordHasher.isHashed(user.getPasswordHash())) {
            throw new ValidationException(
                    "Password must be hashed before it is stored");
        }
    }

    /**
     * The database stores only the role, so an administrator is rebuilt with the
     * default STANDARD level; richer admin metadata belongs to a later phase.
     */
    private User mapUser(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        String email = resultSet.getString("email");
        String passwordHash = resultSet.getString("password_hash");
        String role = resultSet.getString("role");

        if (User.ROLE_ADMIN.equals(role)) {
            return new Admin(id, name, email, passwordHash, Admin.LEVEL_STANDARD);
        }
        return new Student(id, name, email, passwordHash);
    }
}
