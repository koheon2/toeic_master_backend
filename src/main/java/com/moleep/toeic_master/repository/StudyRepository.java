package com.moleep.toeic_master.repository;

import com.moleep.toeic_master.entity.Study;
import com.moleep.toeic_master.entity.StudyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyRepository extends JpaRepository<Study, Long> {

    @Query("SELECT s.id FROM Study s WHERE " +
            "(:examType IS NULL OR s.examType = :examType) AND " +
            "(:region IS NULL OR s.region = :region) AND " +
            "s.status = :status AND " +
            "(:minScore IS NULL OR s.targetScore >= :minScore) AND " +
            "(:maxScore IS NULL OR s.targetScore <= :maxScore) AND " +
            "s.embedding IS NOT NULL")
    List<Long> findStudyIdsWithFilters(
            @Param("examType") String examType,
            @Param("region") String region,
            @Param("status") StudyStatus status,
            @Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore
    );

    @Query("SELECT s FROM Study s WHERE " +
            "(:keyword IS NULL OR s.title LIKE %:keyword% OR s.region LIKE %:keyword%) AND " +
            "(:examType IS NULL OR s.examType = :examType) AND " +
            "(:region IS NULL OR s.region = :region) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:minScore IS NULL OR s.targetScore >= :minScore) AND " +
            "(:maxScore IS NULL OR s.targetScore <= :maxScore) " +
            "ORDER BY s.createdAt DESC")
    Page<Study> findWithFilters(
            @Param("keyword") String keyword,
            @Param("examType") String examType,
            @Param("region") String region,
            @Param("status") StudyStatus status,
            @Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore,
            Pageable pageable
    );
}
