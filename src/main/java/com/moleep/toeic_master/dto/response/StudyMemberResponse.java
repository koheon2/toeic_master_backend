package com.moleep.toeic_master.dto.response;

import com.moleep.toeic_master.entity.MemberRole;
import com.moleep.toeic_master.entity.StudyMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StudyMemberResponse {
    private Long id;
    private Long userId;
    private String nickname;
    private String bio;
    private MemberRole role;
    private LocalDateTime joinedAt;

    public static StudyMemberResponse from(StudyMember member) {
        return StudyMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .nickname(member.getUser().getNickname())
                .bio(member.getUser().getBio())
                .role(member.getRole())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}
