package com.moleep.toeic_master.controller;

import com.moleep.toeic_master.dto.response.ApiResponse;
import com.moleep.toeic_master.dto.response.SchoolResponse;
import com.moleep.toeic_master.service.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
@Tag(name = "School", description = "고사장(학교) API")
public class SchoolController {

    private final SchoolService schoolService;

    @GetMapping
    @Operation(summary = "학교 목록 조회", description = "모든 학교 목록을 조회합니다")
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> getAllSchools() {
        List<SchoolResponse> schools = schoolService.getAllSchools();
        return ResponseEntity.ok(ApiResponse.success(schools));
    }

    @GetMapping("/{id}")
    @Operation(summary = "학교 상세 조회", description = "학교 ID로 상세 정보를 조회합니다")
    public ResponseEntity<ApiResponse<SchoolResponse>> getSchool(@PathVariable Long id) {
        SchoolResponse school = schoolService.getSchool(id);
        return ResponseEntity.ok(ApiResponse.success(school));
    }

    @GetMapping("/nearby")
    @Operation(summary = "주변 학교 조회", description = "현재 위치 기준으로 주변 학교를 조회합니다")
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> getNearbySchools(
            @Parameter(description = "위도") @RequestParam BigDecimal lat,
            @Parameter(description = "경도") @RequestParam BigDecimal lng,
            @Parameter(description = "반경 (도 단위, 기본값 0.05 ≈ 5.5km)") @RequestParam(defaultValue = "0.05") BigDecimal radius) {

        List<SchoolResponse> schools = schoolService.getNearbySchools(lat, lng, radius);
        return ResponseEntity.ok(ApiResponse.success(schools));
    }

    @GetMapping("/search")
    @Operation(summary = "학교 검색", description = "학교명으로 검색합니다")
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> searchSchools(
            @Parameter(description = "학교명") @RequestParam String name) {

        List<SchoolResponse> schools = schoolService.searchSchools(name);
        return ResponseEntity.ok(ApiResponse.success(schools));
    }
}
