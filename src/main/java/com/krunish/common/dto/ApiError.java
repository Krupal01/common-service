package com.krunish.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter  // ✅ Add this — without it Jackson returns {}
@AllArgsConstructor
public class ApiError {
    private String code;
    private String detail;
}
