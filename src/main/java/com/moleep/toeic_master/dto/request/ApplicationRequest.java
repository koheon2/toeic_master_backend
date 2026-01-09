package com.moleep.toeic_master.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequest {

    @Size(max = 500, message = "신청 메시지는 500자 이하여야 합니다")
    private String message;
}
