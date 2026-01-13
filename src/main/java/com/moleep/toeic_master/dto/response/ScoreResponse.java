package com.moleep.toeic_master.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ScoreResponse {
    private Integer score;
    private Integer delta;
}
