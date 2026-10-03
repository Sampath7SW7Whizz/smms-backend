package com.smms.backend.dto;

public class LoginRequest {
    /**
     * Accepts either { email, password } or { emailOrPhone, password } from the
     * frontend
     */
    private String email;
    private String emailOrPhone;
    private String password;

    /**
     * Returns whichever identifier field was supplied (emailOrPhone takes priority)
     */
    public String resolveIdentifier() {
        if (emailOrPhone != null && !emailOrPhone.isBlank())
            return emailOrPhone.trim();
        if (email != null && !email.isBlank())
            return email.trim();
        return "";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        this.email = v;
    }

    public String getEmailOrPhone() {
        return emailOrPhone;
    }

    public void setEmailOrPhone(String v) {
        this.emailOrPhone = v;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String v) {
        this.password = v;
    }
}
