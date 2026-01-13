package com.moleep.toeic_master.service;

import com.moleep.toeic_master.entity.Review;
import com.moleep.toeic_master.entity.School;
import com.moleep.toeic_master.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolEmbeddingAsyncService {

    private final SchoolRepository schoolRepository;
    private final EmbeddingService embeddingService;
    private final SchoolEmbeddingCache schoolEmbeddingCache;

    @Async
    @Transactional
    public void updateSchoolEmbeddingAsync(Long schoolId) {
        try {
            School school = schoolRepository.findById(schoolId).orElse(null);
            if (school == null) {
                log.warn("School not found for embedding update: {}", schoolId);
                return;
            }

            List<String> reviewContents = school.getReviews().stream()
                    .map(Review::getContent)
                    .filter(content -> content != null && !content.isBlank())
                    .toList();

            if (reviewContents.isEmpty()) {
                school.setEvaluation(null);
                school.setEmbedding(null);
                schoolEmbeddingCache.remove(schoolId);
                log.info("Cleared embedding for school {} (no reviews)", schoolId);
                return;
            }

            EmbeddingService.VenueEvalResult result = embeddingService.getVenueEvaluation(reviewContents);
            if (result != null) {
                school.setEvaluation(result.evaluation());
                school.setEmbedding(embeddingService.floatArrayToBytes(result.embedding()));
                schoolEmbeddingCache.put(schoolId, result.embedding());
                log.info("Updated embedding for school {} with {} reviews", schoolId, reviewContents.size());
            } else {
                log.warn("Failed to get embedding for school {}", schoolId);
            }
        } catch (Exception e) {
            log.error("Error updating school embedding for school {}", schoolId, e);
        }
    }
}
