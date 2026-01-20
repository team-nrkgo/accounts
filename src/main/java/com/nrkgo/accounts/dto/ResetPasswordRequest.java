package com.nrkgo.accounts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class ResetPasswordRequest {
    @NotBlank(message = "Token is required")
    @JsonProperty("token")
    private String token;

    @NotBlank(message = "New password is required")
    @JsonProperty("newPassword")
    private String newPassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
