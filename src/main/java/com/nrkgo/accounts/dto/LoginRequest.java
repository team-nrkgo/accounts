package com.nrkgo.accounts.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
    
    // Optional timezone update on login? Legacy did this.
    private String timeZone;

    // Manual Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
}
