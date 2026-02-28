package com.krunish.common.exception;

import com.krunish.common.dto.ApiError;
import com.krunish.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ApiResponse<Void> handleAppException(AppException ex) {
        return ApiResponse.error(
                ex.getMessage(),
                List.of(new ApiError(ex.getCode(), ex.getMessage()))
        );
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleAll(Exception ex) {
        return ApiResponse.error(
                "Internal Server Error",
                List.of(new ApiError("INTERNAL_ERROR", ex.getMessage()))
        );
    }
}
