package com.onlinequiz.model;

import com.onlinequiz.exception.InvalidLoginException;
import com.onlinequiz.exception.ValidationException;
import com.onlinequiz.interfaces.Authenticatable;
import com.onlinequiz.util.Validation;

import java.util.Locale;

/**
 * Abstract account type shared by students and administrators. It owns all account
 * state and exposes a dashboard contract that concrete roles implement.
 *
 * Abstract class: User cannot be instantiated directly; a concrete role is required.
 */
public abstract class User extends Person implements Authenticatable {

    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_ADMIN = "ADMIN";

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private String role;

    protected User(int id, String name, String email, String passwordHash, String role) {
        this.id = Validation.requireNonNegative(id, "User id");
        this.name = Validation.requireText(name, "Name");
        this.email = Validation.requireEmail(email);
        this.passwordHash = Validation.requireText(passwordHash, "Password hash");
        this.role = normalizeRole(role);
    }

    /**
     * Constructor overloading: new accounts do not have a database id yet.
     * The this() call delegates to the full constructor.
     */
    protected User(String name, String email, String passwordHash, String role) {
        this(0, name, email, passwordHash, role);
    }

    @Override
    public final int getId() {
        return id;
    }

    public final void setId(int id) {
        this.id = Validation.requireNonNegative(id, "User id");
    }

    @Override
    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = Validation.requireText(name, "Name");
    }

    @Override
    public final String getEmail() {
        return email;
    }

    public final void setEmail(String email) {
        this.email = Validation.requireEmail(email);
    }

    @Override
    public final String getPasswordHash() {
        return passwordHash;
    }

    public final void setPasswordHash(String passwordHash) {
        this.passwordHash = Validation.requireText(passwordHash, "Password hash");
    }

    @Override
    public final String getRole() {
        return role;
    }

    /**
     * Keeps the descriptive role field aligned with the concrete account type.
     */
    public final void setRole(String role) {
        String normalized = normalizeRole(role);
        if (isAdmin() != ROLE_ADMIN.equals(normalized)) {
            throw new ValidationException("Role does not match the account type");
        }
        this.role = normalized;
    }

    /**
     * Verifies the supplied credentials against this account.
     *
     * Checked exception: a failed login is an expected outcome that the login screen
     * must handle, so the compiler forces every caller to deal with it. The message
     * is deliberately generic so it does not reveal whether the email exists.
     *
     * @return this account when the credentials match
     * @throws InvalidLoginException when the email or password does not match
     */
    public final User authenticate(String email, String providedPasswordHash)
            throws InvalidLoginException {
        if (email == null || providedPasswordHash == null
                || !getEmail().equalsIgnoreCase(email.trim())
                || !passwordHash.equals(providedPasswordHash)) {
            throw new InvalidLoginException("Invalid email or password");
        }
        return this;
    }

    public abstract boolean isAdmin();

    public final boolean canManageQuizzes() {
        return isAdmin();
    }

    /**
     * Each role renders its own dashboard. Subclasses override this method, which
     * demonstrates runtime polymorphism when called through a User reference.
     */
    public abstract String displayDashboard();

    public void displayBasicInfo() {
        System.out.println("User ID : " + id);
        System.out.println("Name    : " + name);
        System.out.println("Email   : " + email);
        System.out.println("Role    : " + role);
    }

    private static String normalizeRole(String role) {
        String normalized = Validation.requireText(role, "Role").toUpperCase(Locale.ROOT);
        if (!ROLE_STUDENT.equals(normalized) && !ROLE_ADMIN.equals(normalized)) {
            throw new ValidationException("Role must be STUDENT or ADMIN");
        }
        return normalized;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", name='" + name
                + "', email='" + email + "', role='" + role + "'}";
    }
}
