package com.echovenancio.ministack.models;

import jakarta.validation.constraints.Email;
import lombok.ToString;

@ToString
public class LoginCredentials {

    @Email(message = "Email should be valid")
    private String email;
    private String password;

    public LoginCredentials() {
    }

    public LoginCredentials(String email, String password) {
        this.email = email;
        this.password = password;
    }

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

}
