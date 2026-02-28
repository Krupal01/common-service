package com.krunish.common.dto;

public class ApiError {

    private String code;
    private String detail;

    public ApiError(String code, String detail) {
        this.code = code;
        this.detail = detail;
    }
}
