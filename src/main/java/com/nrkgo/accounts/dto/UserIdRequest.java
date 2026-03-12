package com.nrkgo.accounts.dto;

import lombok.Data;

@Data
public class UserIdRequest {
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
