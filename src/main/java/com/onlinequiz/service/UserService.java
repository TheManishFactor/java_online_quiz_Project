package com.onlinequiz.service;

import com.onlinequiz.exception.InvalidLoginException;
import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.interfaces.UserOperations;
import com.onlinequiz.model.Admin;
import com.onlinequiz.model.User;
import com.onlinequiz.repository.UserRepository;
import com.onlinequiz.util.PasswordHasher;
import com.onlinequiz.util.RegistrationValidator;
import com.onlinequiz.util.Validation;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Registration, authentication and account management. Passwords supplied in the
 * user model are hashed before they reach the repository; hashes are never stored
 * in plain text.
 */
public class UserService implements UserOperations {

    private final UserRepository userRepository;
    private final Set<Integer> activeSessions = new HashSet<>();

    public UserService() {
        this(new UserRepository());
    }

    public UserService(String databaseFile) {
        this(new UserRepository(databaseFile));
    }

    public UserService(UserRepository userRepository) {
        if (userRepository == null) {
            throw new ValidationException("User repository is required");
        }
        this.userRepository = userRepository;
    }

    /**
     * Persists a new account. The password carried by the model is treated as a
     * plain-text password unless it is already an encoded PBKDF2 hash.
     */
    @Override
    public boolean registerUser(User user) {
        if (user == null) {
            throw new ValidationException("User is required");
        }
        String email = Validation.requireEmail(user.getEmail());
        try {
            if (userRepository.findUserByEmail(email) != null) {
                throw new ValidationException("Email is already registered: " + email);
            }
            user.setEmail(email);
            if (!PasswordHasher.isHashed(user.getPasswordHash())) {
                RegistrationValidator.requirePassword(user.getPasswordHash());
                user.setPasswordHash(PasswordHasher.hash(user.getPasswordHash()));
            }
            userRepository.registerUser(user);
            return true;
        } catch (SQLException error) {
            throw new IllegalStateException("Could not register user", error);
        }
    }

    /**
     * True when at least one administrator exists. Used to decide whether the
     * first-run setup screen may be offered.
     */
    public boolean hasAdmin() {
        try {
            return userRepository.countAdmins() > 0;
        } catch (SQLException error) {
            throw new IllegalStateException("Could not check administrator accounts", error);
        }
    }

    /**
     * Safe first-run bootstrap. There is no hardcoded administrator: the person
     * installing the application chooses the name, email and password here. The
     * operation is refused as soon as one administrator already exists, so it
     * cannot be used to add extra administrators behind the application's back.
     */
    public User createFirstAdmin(String name, String email, String password) {
        String cleanName = RegistrationValidator.requireName(name);
        String cleanEmail = RegistrationValidator.requireEmail(email);
        RegistrationValidator.requirePassword(password);
        if (hasAdmin()) {
            throw new ValidationException("An administrator account already exists");
        }
        Admin admin = new Admin(cleanName, cleanEmail, password);
        registerUser(admin);
        return admin;
    }

    @Override
    public User loginUser(String email, String password) throws InvalidLoginException {
        if (email == null || password == null
                || email.trim().isEmpty() || password.isEmpty()) {
            throw new InvalidLoginException("Invalid email or password");
        }
        try {
            String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
            User user = userRepository.findUserByEmail(normalizedEmail);
            if (user == null || !PasswordHasher.verify(password, user.getPasswordHash())) {
                throw new InvalidLoginException("Invalid email or password");
            }
            activeSessions.add(user.getId());
            return user;
        } catch (SQLException error) {
            throw new IllegalStateException("Could not log in", error);
        }
    }

    @Override
    public boolean logoutUser(int userId) {
        Validation.requirePositive(userId, "User id");
        return activeSessions.remove(userId);
    }

    public User findUserById(int userId) {
        Validation.requirePositive(userId, "User id");
        try {
            return userRepository.findUserById(userId);
        } catch (SQLException error) {
            throw new IllegalStateException("Could not load user " + userId, error);
        }
    }

    public User findUserByEmail(String email) {
        String normalizedEmail = Validation.requireEmail(email);
        try {
            return userRepository.findUserByEmail(normalizedEmail);
        } catch (SQLException error) {
            throw new IllegalStateException("Could not load user", error);
        }
    }

    public List<User> getAllUsers() {
        try {
            return userRepository.getAllUsers();
        } catch (SQLException error) {
            throw new IllegalStateException("Could not load users", error);
        }
    }

    public boolean updateUser(User user) {
        if (user == null) {
            throw new ValidationException("User is required");
        }
        Validation.requirePositive(user.getId(), "User id");
        String email = Validation.requireEmail(user.getEmail());
        try {
            User existing = userRepository.findUserByEmail(email);
            if (existing != null && existing.getId() != user.getId()) {
                throw new ValidationException("Email is already registered: " + email);
            }
            user.setEmail(email);
            if (!PasswordHasher.isHashed(user.getPasswordHash())) {
                RegistrationValidator.requirePassword(user.getPasswordHash());
                user.setPasswordHash(PasswordHasher.hash(user.getPasswordHash()));
            }
            return userRepository.updateUser(user);
        } catch (SQLException error) {
            throw new IllegalStateException("Could not update user " + user.getId(), error);
        }
    }

    public boolean deleteUser(int userId) {
        Validation.requirePositive(userId, "User id");
        try {
            activeSessions.remove(userId);
            return userRepository.deleteUser(userId);
        } catch (SQLException error) {
            throw new IllegalStateException("Could not delete user " + userId, error);
        }
    }

    /**
     * Exposes the in-memory session set for diagnostics without allowing callers
     * to alter it.
     */
    public Set<Integer> getActiveSessionIds() {
        return Set.copyOf(activeSessions);
    }
}
