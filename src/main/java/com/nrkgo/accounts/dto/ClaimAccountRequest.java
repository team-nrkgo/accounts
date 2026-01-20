package com.nrkgo.accounts.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ClaimAccountRequest {
    @NotBlank(message = "Token is required")
    private String token;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    private String lastName;

    // Manual Accessors
    public String getToken() { return token; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}
