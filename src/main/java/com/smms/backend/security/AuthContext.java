package com.smms.backend.security;

import com.smms.backend.model.User;

/**
 * Utility to hold the current authenticated user in the current thread.
 * This allows services and controllers to access the user without passing it as
 * a parameter.
 */
public class AuthContext {
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}
