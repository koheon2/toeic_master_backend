package com.moleep.toeic_master.service;

import com.moleep.toeic_master.dto.response.StudyRecommendationResponse;
import com.moleep.toeic_master.dto.response.StudyResponse;
import com.moleep.toeic_master.entity.Study;
import com.moleep.toeic_master.entity.StudyStatus;
import com.moleep.toeic_master.entity.User;
import com.moleep.toeic_master.exception.CustomException;
import com.moleep.toeic_master.repository.StudyMemberRepository;
import com.moleep.toeic_master.repository.StudyRepository;
import com.moleep.toeic_master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StudyRecommendationService {

    private final UserRepository userRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final EmbeddingService embeddingService;
    private final StudyEmbeddingCache studyEmbeddingCache;

    @Transactional(readOnly = true)
    public List<StudyResponse> getRecommendedStudies(
            Long userId,
            String examType,
            String region,
            Integer minScore,
            Integer maxScore,
            int topK
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (user.getEmbedding() == null) {
            throw new CustomException("성향 정보가 없습니다. 프로필에서 성향을 설정해주세요.", HttpStatus.BAD_REQUEST);
        }

        float[] userEmbedding = embeddingService.bytesToFloatArray(user.getEmbedding());

        // SQL 하드 필터로 study_id 목록 조회
        List<Long> candidateIds = studyRepository.findStudyIdsWithFilters(
                examType, region, StudyStatus.RECRUITING, minScore, maxScore
        );

        if (candidateIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 캐시에서 임베딩 조회 및 코사인 유사도 계산
        List<StudyRecommendationResponse> similarities = new ArrayList<>();
        for (Long studyId : candidateIds) {
            float[] studyEmbedding = studyEmbeddingCache.get(studyId);
            if (studyEmbedding != null) {
                double similarity = embeddingService.cosineSimilarity(userEmbedding, studyEmbedding);
                similarities.add(StudyRecommendationResponse.builder()
                        .studyId(studyId)
                        .similarity(similarity)
                        .build());
            }
        }

        // 유사도 기준 정렬 및 Top-K 추출
        similarities.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));
        List<Long> topKIds = similarities.stream()
                .limit(topK)
                .map(StudyRecommendationResponse::getStudyId)
                .toList();

        if (topKIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Study 엔티티 조회 및 응답 생성
        List<Study> studies = studyRepository.findAllById(topKIds);
        Map<Long, Study> studyMap = new HashMap<>();
        for (Study study : studies) {
            studyMap.put(study.getId(), study);
        }

        // 정렬 순서 유지하면서 응답 생성
        List<StudyResponse> result = new ArrayList<>();
        for (Long id : topKIds) {
            Study study = studyMap.get(id);
            if (study != null) {
                result.add(StudyResponse.from(study, studyMemberRepository.countByStudyId(id)));
            }
        }

        return result;
    }
}
