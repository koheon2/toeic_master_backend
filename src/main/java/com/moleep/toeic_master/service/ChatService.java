package com.moleep.toeic_master.service;

import com.moleep.toeic_master.dto.response.ChatMessageResponse;
import com.moleep.toeic_master.entity.ChatMessage;
import com.moleep.toeic_master.entity.Study;
import com.moleep.toeic_master.entity.User;
import com.moleep.toeic_master.exception.CustomException;
import com.moleep.toeic_master.repository.ChatMessageRepository;
import com.moleep.toeic_master.repository.StudyRepository;
import com.moleep.toeic_master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final StudyRepository studyRepository;
    private final UserRepository userRepository;
    private final StudyMemberService memberService;

    @Transactional
    public ChatMessageResponse saveMessage(Long studyId, Long userId, String content) {
        if (!memberService.isMember(studyId, userId)) {
            throw new CustomException("스터디 멤버만 채팅할 수 있습니다", HttpStatus.FORBIDDEN);
        }

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException("스터디를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        ChatMessage message = ChatMessage.builder()
                .study(study)
                .user(user)
                .content(content)
                .build();

        chatMessageRepository.save(message);
        return ChatMessageResponse.from(message);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(Long studyId, Long userId, Pageable pageable) {
        if (!memberService.isMember(studyId, userId)) {
            throw new CustomException("스터디 멤버만 채팅 이력을 조회할 수 있습니다", HttpStatus.FORBIDDEN);
        }

        return chatMessageRepository.findByStudyIdOrderByCreatedAtDesc(studyId, pageable)
                .map(ChatMessageResponse::from);
    }
}
