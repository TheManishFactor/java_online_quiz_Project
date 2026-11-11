package com.onlinequiz.interfaces;

/**
 * Contract implemented by account types that can authenticate with an email and
 * a password hash.
 */
public interface Authenticatable {

    String getEmail();

    String getPasswordHash();
}
