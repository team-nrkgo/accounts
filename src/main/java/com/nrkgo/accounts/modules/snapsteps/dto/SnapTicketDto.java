package com.nrkgo.accounts.modules.snapsteps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapTicketDto {
    private String email;
    private String subject;
    private String message;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("browser_info")
    private String browserInfo;
}
