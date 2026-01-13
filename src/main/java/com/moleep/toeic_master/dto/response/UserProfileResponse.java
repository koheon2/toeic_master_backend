package com.moleep.toeic_master.dto.response;

import com.moleep.toeic_master.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String email;
    private String nickname;
    private String bio;
    private String tendency;
    private String profileImageUrl;
    private LocalDateTime createdAt;

    public static UserProfileResponse from(User user, String profileImageUrl) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .bio(user.getBio())
                .tendency(user.getTendency())
                .profileImageUrl(profileImageUrl)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
