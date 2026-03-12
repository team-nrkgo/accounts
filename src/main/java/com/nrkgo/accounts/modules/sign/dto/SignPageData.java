package com.nrkgo.accounts.modules.sign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignPageData {
    private int pageNumber;
    private float width;
    private float height;
    private int rotation;
}
