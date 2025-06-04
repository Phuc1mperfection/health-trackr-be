package com.example.healthtrackr.service;

import com.example.healthtrackr.config.JwtUtil;
import com.example.healthtrackr.dto.request.AuthRequest;
import com.example.healthtrackr.dto.response.LoginResponse;
import com.example.healthtrackr.entity.Token;
import com.example.healthtrackr.entity.User;
import com.example.healthtrackr.exception.AppException;
import com.example.healthtrackr.repository.TokenRepository;
import com.example.healthtrackr.repository.UserRepository;
import com.example.healthtrackr.utils.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Set<String> tokenBlocklist = new HashSet<>();

    public AuthService(UserRepository userRepository, TokenRepository tokenRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(AuthRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new AppException(ErrorCode.EMAIL_NOT_NULL);
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new AppException(ErrorCode.PASSWORD_NOT_NULL);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Tạo JWT Token
        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // Lưu token vào DB
        Token tokenEntity = new Token();
        tokenEntity.setUser(user);
        tokenEntity.setToken(accessToken);
        tokenEntity.setRefreshToken(refreshToken);
        tokenEntity.setExpired(false);
        tokenEntity.setRevoked(false);
        tokenEntity.setExpirationDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)); // 7 ngày
        tokenEntity.setRefreshExpirationDate(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30)); // 30 ngày

        tokenRepository.save(tokenEntity);

        return new LoginResponse(accessToken, refreshToken, user.getEmail(), user.getUsername());
    }


    public LoginResponse refreshToken(String refreshToken) {
        Token tokenEntity = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (tokenEntity.isExpired() || tokenEntity.isRevoked()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String newAccessToken = jwtUtil.generateToken(tokenEntity.getUser().getEmail());

        return new LoginResponse(newAccessToken, refreshToken,
                tokenEntity.getUser().getEmail(), tokenEntity.getUser().getUsername());
    }
    public ResponseEntity<String> register(AuthRequest request) {
        // Kiểm tra nếu request hoặc các trường trong request bị null
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new AppException(ErrorCode.EMAIL_NOT_NULL);  // Lỗi khi email bị thiếu
        }
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw new AppException(ErrorCode.USERNAME_NOT_NULL);  // Lỗi khi username bị thiếu
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new AppException(ErrorCode.PASSWORD_NOT_NULL);  // Lỗi khi password bị thiếu
        }

        String email = request.getEmail();

        // Kiểm tra nếu email đã tồn tại
        if (userRepository.findByEmail(email).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_EXITED);  // Ném lỗi khi email đã tồn tại
        }

        // Tạo và lưu người dùng mới
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        user.setRoles(Collections.singleton("USER"));

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully!");
    }
    public void logout(String token) {
        tokenBlocklist.add(token);
    }

}
