package com.moleep.toeic_master.service;

import com.moleep.toeic_master.dto.response.StudyMemberResponse;
import com.moleep.toeic_master.entity.MemberRole;
import com.moleep.toeic_master.entity.Study;
import com.moleep.toeic_master.entity.StudyMember;
import com.moleep.toeic_master.entity.User;
import com.moleep.toeic_master.exception.CustomException;
import com.moleep.toeic_master.repository.StudyMemberRepository;
import com.moleep.toeic_master.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyMemberService {

    private final StudyMemberRepository studyMemberRepository;
    private final StudyRepository studyRepository;

    @Transactional(readOnly = true)
    public List<StudyMemberResponse> getMembers(Long studyId) {
        return studyMemberRepository.findByStudyId(studyId).stream()
                .map(StudyMemberResponse::from)
                .toList();
    }

    @Transactional
    public void addMember(Study study, User user, MemberRole role) {
        if (studyMemberRepository.existsByStudyIdAndUserId(study.getId(), user.getId())) {
            throw new CustomException("이미 스터디 멤버입니다", HttpStatus.BAD_REQUEST);
        }

        StudyMember member = StudyMember.builder()
                .study(study)
                .user(user)
                .role(role)
                .build();
        studyMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long studyId, Long targetUserId, Long requesterId) {
        StudyMember requester = studyMemberRepository.findByStudyIdAndUserId(studyId, requesterId)
                .orElseThrow(() -> new CustomException("스터디 멤버가 아닙니다", HttpStatus.FORBIDDEN));

        if (requester.getRole() != MemberRole.LEADER) {
            throw new CustomException("방장만 멤버를 강퇴할 수 있습니다", HttpStatus.FORBIDDEN);
        }

        if (requesterId.equals(targetUserId)) {
            throw new CustomException("방장은 자신을 강퇴할 수 없습니다", HttpStatus.BAD_REQUEST);
        }

        StudyMember target = studyMemberRepository.findByStudyIdAndUserId(studyId, targetUserId)
                .orElseThrow(() -> new CustomException("해당 멤버를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        studyMemberRepository.delete(target);
    }

    @Transactional
    public void leaveStudy(Long studyId, Long userId) {
        StudyMember member = studyMemberRepository.findByStudyIdAndUserId(studyId, userId)
                .orElseThrow(() -> new CustomException("스터디 멤버가 아닙니다", HttpStatus.BAD_REQUEST));

        if (member.getRole() == MemberRole.LEADER) {
            throw new CustomException("방장은 스터디를 나갈 수 없습니다. 스터디를 삭제하거나 방장을 위임하세요.", HttpStatus.BAD_REQUEST);
        }

        studyMemberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public boolean isMember(Long studyId, Long userId) {
        return studyMemberRepository.existsByStudyIdAndUserId(studyId, userId);
    }

    @Transactional(readOnly = true)
    public boolean isLeader(Long studyId, Long userId) {
        return studyMemberRepository.findByStudyIdAndUserId(studyId, userId)
                .map(m -> m.getRole() == MemberRole.LEADER)
                .orElse(false);
    }
}
