package com.moleep.toeic_master.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StudyRecommendationResponse {
    private Long studyId;
    private double similarity;
}
