package com.echovenancio.ministack.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @Size(min = 6, message = "Confirm Password must be at least 6 characters long")
    private String confirmPassword;

    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters long")
    private String username;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
