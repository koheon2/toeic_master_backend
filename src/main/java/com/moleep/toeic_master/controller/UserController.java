package com.moleep.toeic_master.controller;

import com.moleep.toeic_master.dto.request.ProfileUpdateRequest;
import com.moleep.toeic_master.dto.response.ApiResponse;
import com.moleep.toeic_master.dto.response.UserProfileResponse;
import com.moleep.toeic_master.security.CustomUserDetails;
import com.moleep.toeic_master.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 프로필 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필을 조회합니다")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileResponse profile = userService.getMyProfile(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/me")
    @Operation(summary = "프로필 수정", description = "닉네임과 자기소개를 수정합니다")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        UserProfileResponse profile = userService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("프로필이 수정되었습니다", profile));
    }

    @GetMapping("/{id}")
    @Operation(summary = "유저 프로필 조회", description = "다른 사용자의 프로필을 조회합니다")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable Long id) {
        UserProfileResponse profile = userService.getUserProfile(id);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
