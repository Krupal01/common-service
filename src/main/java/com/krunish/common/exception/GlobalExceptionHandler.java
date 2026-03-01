package com.krunish.common.exception;

import com.krunish.common.dto.ApiError;
import com.krunish.common.dto.ApiResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @PostConstruct
    public void init() {
        System.out.println(">>> [GlobalExceptionHandler] ✅ Loaded and active");
    }

    // ✅ Add this — handles BaseException with correct HTTP status
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
        System.out.println(">>> [GlobalExceptionHandler] ❌ BaseException: " + ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(
                        ex.getMessage(),
                        List.of(new ApiError(ex.getErrorCode(), ex.getMessage()))
                ));
    }

    // ✅ Fix this — wrap in ResponseEntity with 400
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(
                        ex.getMessage(),
                        List.of(new ApiError(ex.getCode(), ex.getMessage()))
                ));
    }

    // ✅ Fix this — wrap in ResponseEntity with 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {
        System.out.println(">>> [GlobalExceptionHandler] ❌ Generic: " + ex.getMessage());
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error(
                        "Internal Server Error",
                        List.of(new ApiError("INTERNAL_ERROR", ex.getMessage()))
                ));
    }
}
