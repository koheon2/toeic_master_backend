package com.moleep.toeic_master.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {

    @Size(min = 2, max = 50, message = "닉네임은 2-50자 사이여야 합니다")
    private String nickname;

    @Size(max = 500, message = "자기소개는 500자 이하여야 합니다")
    private String bio;
}
