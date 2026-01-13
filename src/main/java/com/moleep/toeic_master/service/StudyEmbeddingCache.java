package com.moleep.toeic_master.service;

import com.moleep.toeic_master.entity.Study;
import com.moleep.toeic_master.repository.StudyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class StudyEmbeddingCache {

    private final StudyRepository studyRepository;
    private final EmbeddingService embeddingService;

    private final Map<Long, float[]> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadAllEmbeddings();
    }

    private void loadAllEmbeddings() {
        log.info("Loading study embeddings into cache...");
        List<Study> studies = studyRepository.findAll();
        int count = 0;
        for (Study study : studies) {
            if (study.getEmbedding() != null) {
                float[] embedding = embeddingService.bytesToFloatArray(study.getEmbedding());
                if (embedding != null) {
                    cache.put(study.getId(), embedding);
                    count++;
                }
            }
        }
        log.info("Loaded {} study embeddings into cache", count);
    }

    public void put(Long studyId, float[] embedding) {
        if (embedding != null) {
            cache.put(studyId, embedding);
        }
    }

    public float[] get(Long studyId) {
        return cache.get(studyId);
    }

    public void remove(Long studyId) {
        cache.remove(studyId);
    }

    public Map<Long, float[]> getAll() {
        return cache;
    }
}
