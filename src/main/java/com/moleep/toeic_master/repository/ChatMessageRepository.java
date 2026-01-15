package com.moleep.toeic_master.repository;

import com.moleep.toeic_master.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByStudyIdOrderByCreatedAtDesc(Long studyId, Pageable pageable);
    void deleteByStudyId(Long studyId);
}
