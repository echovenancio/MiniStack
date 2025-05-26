package com.echovenancio.ministack.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class RegisterRequest {
    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @Size(min = 6, message = "Confirm Password must be at least 6 characters long")
    private String confirmPassword;

    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters long")
    private String username;
}
