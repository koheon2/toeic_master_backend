package com.moleep.toeic_master.controller;

import com.moleep.toeic_master.dto.request.ProfileUpdateRequest;
import com.moleep.toeic_master.dto.request.ScoreRequest;
import com.moleep.toeic_master.dto.response.ApiResponse;
import com.moleep.toeic_master.dto.response.GalleryImageResponse;
import com.moleep.toeic_master.dto.response.ScoreResponse;
import com.moleep.toeic_master.dto.response.StudyResponse;
import com.moleep.toeic_master.dto.response.UserProfileResponse;
import com.moleep.toeic_master.security.CustomUserDetails;
import com.moleep.toeic_master.service.ScoreService;
import com.moleep.toeic_master.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 프로필 API")
public class UserController {

    private final UserService userService;
    private final ScoreService scoreService;

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필을 조회합니다")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileResponse profile = userService.getMyProfile(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "프로필 수정", description = "닉네임과 자기소개를 수정합니다")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        UserProfileResponse profile = userService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("프로필이 수정되었습니다", profile));
    }

    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "프로필 이미지 업로드", description = "프로필 이미지를 업로드합니다")
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("image") MultipartFile image) {
        UserProfileResponse profile = userService.updateProfileImage(userDetails.getId(), image);
        return ResponseEntity.ok(ApiResponse.success("프로필 이미지가 업로드되었습니다", profile));
    }

    @DeleteMapping(value = "/me/profile-image", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "프로필 이미지 삭제", description = "프로필 이미지를 삭제합니다")
    public ResponseEntity<ApiResponse<UserProfileResponse>> deleteProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileResponse profile = userService.deleteProfileImage(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("프로필 이미지가 삭제되었습니다", profile));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "유저 프로필 조회", description = "다른 사용자의 프로필을 조회합니다")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable Long id) {
        UserProfileResponse profile = userService.getUserProfile(id);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping(value = "/me/gallery", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "내 리뷰 사진 갤러리", description = "내가 작성한 리뷰의 사진들을 갤러리 형식으로 조회합니다")
    public ResponseEntity<ApiResponse<Page<GalleryImageResponse>>> getMyGallery(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<GalleryImageResponse> gallery = userService.getMyGallery(userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(gallery));
    }

    @GetMapping(value = "/me/studies", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "내 스터디 목록", description = "내가 참여 중인 스터디 목록을 조회합니다")
    public ResponseEntity<ApiResponse<java.util.List<StudyResponse>>> getMyStudies(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        java.util.List<StudyResponse> studies = userService.getMyStudies(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(studies));
    }

    @PostMapping(value = "/me/score", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "점수 이벤트 처리", description = "스터디 가입, 리뷰 작성 등의 이벤트에 따른 점수를 지급합니다")
    public ResponseEntity<ApiResponse<ScoreResponse>> addScore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ScoreRequest request) {
        ScoreResponse response = scoreService.addScore(userDetails.getId(), request.getType(), request.getRefId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
