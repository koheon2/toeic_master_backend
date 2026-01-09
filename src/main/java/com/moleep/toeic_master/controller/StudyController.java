package com.moleep.toeic_master.controller;

import com.moleep.toeic_master.dto.request.StudyRequest;
import com.moleep.toeic_master.dto.response.ApiResponse;
import com.moleep.toeic_master.dto.response.StudyResponse;
import com.moleep.toeic_master.entity.ExamType;
import com.moleep.toeic_master.security.CustomUserDetails;
import com.moleep.toeic_master.service.StudyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
@Tag(name = "Study", description = "스터디 모집 API")
public class StudyController {

    private final StudyService studyService;

    @GetMapping
    @Operation(summary = "스터디 목록 조회", description = "검색 및 필터링 조건으로 스터디 목록을 조회합니다")
    public ResponseEntity<ApiResponse<Page<StudyResponse>>> getStudies(
            @Parameter(description = "검색어 (제목/지역)") @RequestParam(required = false) String keyword,
            @Parameter(description = "시험 종류") @RequestParam(required = false) ExamType examType,
            @Parameter(description = "지역") @RequestParam(required = false) String region,
            @Parameter(description = "최소 목표 점수") @RequestParam(required = false) Integer minScore,
            @Parameter(description = "최대 목표 점수") @RequestParam(required = false) Integer maxScore,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<StudyResponse> studies = studyService.getStudies(keyword, examType, region, minScore, maxScore, pageable);
        return ResponseEntity.ok(ApiResponse.success(studies));
    }

    @GetMapping("/{id}")
    @Operation(summary = "스터디 상세 조회", description = "스터디 ID로 상세 정보를 조회합니다")
    public ResponseEntity<ApiResponse<StudyResponse>> getStudy(@PathVariable Long id) {
        StudyResponse study = studyService.getStudy(id);
        return ResponseEntity.ok(ApiResponse.success(study));
    }

    @PostMapping
    @Operation(summary = "스터디 생성", description = "새로운 스터디 모집글을 작성합니다")
    public ResponseEntity<ApiResponse<StudyResponse>> createStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody StudyRequest request) {

        StudyResponse study = studyService.createStudy(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("스터디가 생성되었습니다", study));
    }

    @PutMapping("/{id}")
    @Operation(summary = "스터디 수정", description = "스터디 모집글을 수정합니다")
    public ResponseEntity<ApiResponse<StudyResponse>> updateStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody StudyRequest request) {

        StudyResponse study = studyService.updateStudy(userDetails.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("스터디가 수정되었습니다", study));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "스터디 삭제", description = "스터디 모집글을 삭제합니다")
    public ResponseEntity<ApiResponse<Void>> deleteStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        studyService.deleteStudy(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("스터디가 삭제되었습니다", null));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "스터디 마감", description = "스터디 모집을 마감합니다")
    public ResponseEntity<ApiResponse<StudyResponse>> closeStudy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        StudyResponse study = studyService.closeStudy(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("스터디 모집이 마감되었습니다", study));
    }
}
