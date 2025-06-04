package com.example.healthtrackr.controller;

import com.example.healthtrackr.dto.request.AuthRequest;
import com.example.healthtrackr.dto.response.LoginResponse;
import com.example.healthtrackr.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // Cho phép React truy cập

public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ResponseEntity<String>> register(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> login(@RequestBody AuthRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(loginResponse);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7); // Bỏ "Bearer "
        authService.logout(jwt); // Xử lý token (thêm vào blocklist nếu cần)
        return ResponseEntity.ok("Logged out successfully");
    }
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshAccessToken(@RequestHeader("Authorization") String refreshToken) {
        String jwt = refreshToken.substring(7); // Bỏ "Bearer "
        LoginResponse response = authService.refreshToken(jwt);
        return ResponseEntity.ok(response);
    }

}
