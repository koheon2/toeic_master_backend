package com.moleep.toeic_master.dto.response;

import com.moleep.toeic_master.entity.ApplicationStatus;
import com.moleep.toeic_master.entity.StudyApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private Long studyId;
    private String studyTitle;
    private Long applicantId;
    private String applicantNickname;
    private String applicantBio;
    private String message;
    private ApplicationStatus status;
    private LocalDateTime createdAt;

    public static ApplicationResponse from(StudyApplication application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .studyId(application.getStudy().getId())
                .studyTitle(application.getStudy().getTitle())
                .applicantId(application.getUser().getId())
                .applicantNickname(application.getUser().getNickname())
                .applicantBio(application.getUser().getBio())
                .message(application.getMessage())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .build();
    }
}
