package com.example.healthtrackr.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9998, "Invalid constraint key", HttpStatus.INTERNAL_SERVER_ERROR), PYTHON_SERVER_NOT_CONNECT(9997, "Cannot connect to Python server", HttpStatus.INTERNAL_SERVER_ERROR),

    UNAUTHENTICATED(1000, "User authentication failed", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1001, "You do not have permission to access", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(1002, "Token has expired", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_EXPIRED(1003, "Refresh token has expired", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_NON_EXISTED(1004, "Refresh token does not exist", HttpStatus.NOT_FOUND),
    TOKEN_NOT_VALID(1005, "Token is not valid", HttpStatus.FORBIDDEN),

    // Validate user
    USER_EXISTED(1010, "User already exists", HttpStatus.BAD_REQUEST),
    EMAIL_EXITED(1011, "Email already exists", HttpStatus.BAD_REQUEST),
    USER_NON_EXISTED(1012, "User does not exist", HttpStatus.NOT_FOUND),
    PASSWORD_NOT_MATCH(1018, "Password and retype password do not match", HttpStatus.BAD_REQUEST),
    USERNAME_NOT_NULL(1029, "Username cannot be empty", HttpStatus.BAD_REQUEST),
    USERNAME_SIZE(1030, "Username must be between 3 and 50 characters", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_NULL(1031, "Email cannot be empty", HttpStatus.BAD_REQUEST),
    WRONG_EMAIL_FORMAT(1032, "Incorrect email format", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_NULL(1033, "Password cannot be empty", HttpStatus.BAD_REQUEST),
    USERNAME_CHAR(1034, "Username only letters, numbers, underlines, and horizontal lines are allowed", HttpStatus.BAD_REQUEST),
    PASSWORD_SIZE(1035, "Password must be at least 8 characters long", HttpStatus.BAD_REQUEST),
    PASSWORD_CHAR(1036, "Password must contain uppercase letters, lowercase letters, numbers, and special characters", HttpStatus.BAD_REQUEST),
    LOCATION_SIZE(1037, "Location is maximum 100 characters long", HttpStatus.BAD_REQUEST),
    ABOUTME_SIZE(1038, "About me is maximum 500 characters long", HttpStatus.BAD_REQUEST),
    SLOGAN_SIZE(1039, "Slogan is maximum 50 characters long", HttpStatus.BAD_REQUEST),


    ;

    private int code;
    private String message;
    private HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
