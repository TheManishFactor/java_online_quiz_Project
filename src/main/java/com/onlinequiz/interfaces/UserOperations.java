package com.onlinequiz.interfaces;

import com.onlinequiz.exception.InvalidLoginException;
import com.onlinequiz.model.User;

/**
 * Contract for user registration, authentication and session termination. The
 * JDBC-backed implementation is {@code com.onlinequiz.service.UserService}.
 */
public interface UserOperations {

    boolean registerUser(User user);

    User loginUser(String email, String password) throws InvalidLoginException;

    boolean logoutUser(int userId);
}
