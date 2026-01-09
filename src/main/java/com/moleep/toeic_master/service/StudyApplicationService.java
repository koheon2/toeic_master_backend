package com.moleep.toeic_master.service;

import com.moleep.toeic_master.dto.request.ApplicationRequest;
import com.moleep.toeic_master.dto.response.ApplicationResponse;
import com.moleep.toeic_master.entity.*;
import com.moleep.toeic_master.exception.CustomException;
import com.moleep.toeic_master.repository.StudyApplicationRepository;
import com.moleep.toeic_master.repository.StudyRepository;
import com.moleep.toeic_master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyApplicationService {

    private final StudyApplicationRepository applicationRepository;
    private final StudyRepository studyRepository;
    private final UserRepository userRepository;
    private final StudyMemberService memberService;

    @Transactional
    public ApplicationResponse apply(Long userId, Long studyId, ApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException("스터디를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (study.getStatus() == StudyStatus.CLOSED) {
            throw new CustomException("마감된 스터디입니다", HttpStatus.BAD_REQUEST);
        }

        if (memberService.isMember(studyId, userId)) {
            throw new CustomException("이미 스터디 멤버입니다", HttpStatus.BAD_REQUEST);
        }

        if (applicationRepository.existsByStudyIdAndUserIdAndStatus(studyId, userId, ApplicationStatus.PENDING)) {
            throw new CustomException("이미 참가 신청 중입니다", HttpStatus.BAD_REQUEST);
        }

        StudyApplication application = StudyApplication.builder()
                .study(study)
                .user(user)
                .message(request.getMessage())
                .build();

        applicationRepository.save(application);
        return ApplicationResponse.from(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplications(Long studyId, Long requesterId) {
        if (!memberService.isLeader(studyId, requesterId)) {
            throw new CustomException("방장만 신청 목록을 조회할 수 있습니다", HttpStatus.FORBIDDEN);
        }

        return applicationRepository.findByStudyIdAndStatus(studyId, ApplicationStatus.PENDING).stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    @Transactional
    public ApplicationResponse accept(Long applicationId, Long requesterId) {
        StudyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException("신청을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (!memberService.isLeader(application.getStudy().getId(), requesterId)) {
            throw new CustomException("방장만 신청을 수락할 수 있습니다", HttpStatus.FORBIDDEN);
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new CustomException("이미 처리된 신청입니다", HttpStatus.BAD_REQUEST);
        }

        application.setStatus(ApplicationStatus.ACCEPTED);
        memberService.addMember(application.getStudy(), application.getUser(), MemberRole.MEMBER);

        return ApplicationResponse.from(application);
    }

    @Transactional
    public ApplicationResponse reject(Long applicationId, Long requesterId) {
        StudyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException("신청을 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if (!memberService.isLeader(application.getStudy().getId(), requesterId)) {
            throw new CustomException("방장만 신청을 거절할 수 있습니다", HttpStatus.FORBIDDEN);
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new CustomException("이미 처리된 신청입니다", HttpStatus.BAD_REQUEST);
        }

        application.setStatus(ApplicationStatus.REJECTED);
        return ApplicationResponse.from(application);
    }
}
