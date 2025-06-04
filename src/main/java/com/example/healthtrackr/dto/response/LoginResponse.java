package com.example.healthtrackr.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
@Getter
@Setter
public class LoginResponse {
    private String token;
    private String refreshToken; // Thêm trường này
    private String email;
    private String username;

    public LoginResponse(String token, String refreshToken, String email, String username) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
        this.username = username;
    }
}
