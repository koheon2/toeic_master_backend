package com.moleep.toeic_master.controller;

import com.moleep.toeic_master.dto.request.ReviewRequest;
import com.moleep.toeic_master.dto.response.ApiResponse;
import com.moleep.toeic_master.dto.response.ReviewResponse;
import com.moleep.toeic_master.security.CustomUserDetails;
import com.moleep.toeic_master.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/api/schools/{schoolId}/reviews")
    @Operation(summary = "학교 리뷰 목록", description = "특정 학교의 리뷰 목록을 조회합니다")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviews(
            @PathVariable Long schoolId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReviewResponse> reviews = reviewService.getReviewsBySchool(schoolId, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @PostMapping("/api/schools/{schoolId}/reviews")
    @Operation(summary = "리뷰 작성", description = "학교에 새로운 리뷰를 작성합니다")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long schoolId,
            @Valid @RequestBody ReviewRequest request) {

        ReviewResponse review = reviewService.createReview(userDetails.getId(), schoolId, request);
        return ResponseEntity.ok(ApiResponse.success("리뷰가 작성되었습니다", review));
    }

    @PutMapping("/api/reviews/{id}")
    @Operation(summary = "리뷰 수정", description = "리뷰를 수정합니다")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {

        ReviewResponse review = reviewService.updateReview(userDetails.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("리뷰가 수정되었습니다", review));
    }

    @DeleteMapping("/api/reviews/{id}")
    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {

        reviewService.deleteReview(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("리뷰가 삭제되었습니다", null));
    }
}
