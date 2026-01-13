package com.moleep.toeic_master.dto.request;

import com.moleep.toeic_master.entity.ScoreType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScoreRequest {

    @NotNull(message = "type은 필수입니다")
    private ScoreType type;

    @NotNull(message = "refId는 필수입니다")
    private Long refId;
}
