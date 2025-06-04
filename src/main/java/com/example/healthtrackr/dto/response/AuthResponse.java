package com.example.healthtrackr.dto.response;

import com.example.healthtrackr.entity.Token;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthResponse {
    String accessToken;
    String refreshToken;
    boolean authenticated;

    public static AuthResponse toAuthenticationResponse(Token token) {
        return AuthResponse.builder()
                .accessToken(token.getToken())
                .refreshToken(token.getRefreshToken())
                .authenticated(true)
                .build();
    }
}
