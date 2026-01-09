package com.moleep.toeic_master.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private Long userId;
    private String email;
    private String nickname;

    public static AuthResponse of(String token, Long userId, String email, String nickname) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .nickname(nickname)
                .build();
    }
}
