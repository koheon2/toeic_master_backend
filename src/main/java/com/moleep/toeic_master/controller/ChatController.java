package com.moleep.toeic_master.controller;

import com.moleep.toeic_master.dto.request.ChatMessageRequest;
import com.moleep.toeic_master.dto.response.ApiResponse;
import com.moleep.toeic_master.dto.response.ChatMessageResponse;
import com.moleep.toeic_master.security.CustomUserDetails;
import com.moleep.toeic_master.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 API")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/api/studies/{studyId}/messages")
    @Operation(summary = "채팅 이력 조회", description = "스터디 채팅방의 메시지 이력을 조회합니다")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long studyId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ChatMessageResponse> messages = chatService.getMessages(studyId, userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @MessageMapping("/chat/{studyId}")
    public void sendMessage(
            @DestinationVariable Long studyId,
            @Payload ChatMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        // WebSocket 세션에서 userId 가져오기 (인터셉터에서 설정)
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");

        if (userId != null) {
            ChatMessageResponse response = chatService.saveMessage(studyId, userId, request.getContent());
            messagingTemplate.convertAndSend("/topic/study/" + studyId, response);
        }
    }
}
