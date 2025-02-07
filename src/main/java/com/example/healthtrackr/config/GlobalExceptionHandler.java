package com.example.healthtrackr.config;

import com.example.healthtrackr.exception.AppException;
import com.example.healthtrackr.utils.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        Map<String, Object> response = new HashMap<>();
        ErrorCode errorCode = ex.getErrorCode();

        response.put("Error code", errorCode.getCode());
        response.put("Message", errorCode.getMessage());

        return new ResponseEntity<>(response, errorCode.getStatusCode());
    }
}
