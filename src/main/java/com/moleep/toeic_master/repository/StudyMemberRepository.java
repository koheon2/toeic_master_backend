package com.moleep.toeic_master.repository;

import com.moleep.toeic_master.entity.MemberRole;
import com.moleep.toeic_master.entity.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
    List<StudyMember> findByStudyId(Long studyId);
    List<StudyMember> findByUserId(Long userId);
    Optional<StudyMember> findByStudyIdAndUserId(Long studyId, Long userId);
    Optional<StudyMember> findByStudyIdAndRole(Long studyId, MemberRole role);
    boolean existsByStudyIdAndUserId(Long studyId, Long userId);
    void deleteByStudyIdAndUserId(Long studyId, Long userId);
    void deleteByStudyId(Long studyId);
    int countByStudyId(Long studyId);
}
