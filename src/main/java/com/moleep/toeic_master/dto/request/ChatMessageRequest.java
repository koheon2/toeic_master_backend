package com.moleep.toeic_master.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {

    @NotBlank(message = "메시지 내용은 필수입니다")
    private String content;
}
