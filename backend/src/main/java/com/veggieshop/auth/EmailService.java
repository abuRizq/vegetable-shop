package com.veggieshop.auth;

public interface EmailService {
    /**
     * Sends a password reset email with the given reset link.
     *
     * @param toEmail   The recipient's email address.
     * @param name      The recipient's display name.
     * @param resetLink The password reset URL to include in the email.
     */
    void sendPasswordReset(String toEmail, String name, String resetLink);
}
