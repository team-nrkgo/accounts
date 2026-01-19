package com.nrkgo.accounts.dto;

import lombok.Data;

@Data
public class TokenRequest {
    private String token;

    // Manual Getter/Setter
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
