package com.example.healthtrackr.service;

import com.example.healthtrackr.config.JwtUtil;
import com.example.healthtrackr.dto.AuthRequest;
import com.example.healthtrackr.dto.LoginResponse;
import com.example.healthtrackr.entity.User;
import com.example.healthtrackr.exception.AppException;
import com.example.healthtrackr.repository.UserRepository;
import com.example.healthtrackr.utils.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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

    public LoginResponse login(AuthRequest request) {
        // Kiểm tra nếu request hoặc các trường trong request bị null
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new AppException(ErrorCode.EMAIL_NOT_NULL);  // Lỗi khi email bị thiếu
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new AppException(ErrorCode.PASSWORD_NOT_NULL);  // Lỗi khi password bị thiếu
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED)); // 1000: User authentication failed

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED); // 1000: User authentication failed
        }

        // Tạo JWT Token
        String token = jwtUtil.generateToken(user.getEmail());

        // Trả về LoginResponse chứa cả token, email và username
        return new LoginResponse(token, user.getEmail(), user.getUsername());
    }






}
