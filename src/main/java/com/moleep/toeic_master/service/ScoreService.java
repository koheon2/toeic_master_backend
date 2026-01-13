package com.moleep.toeic_master.service;

import com.moleep.toeic_master.dto.response.ScoreResponse;
import com.moleep.toeic_master.entity.PointTransaction;
import com.moleep.toeic_master.entity.ScoreType;
import com.moleep.toeic_master.entity.User;
import com.moleep.toeic_master.exception.CustomException;
import com.moleep.toeic_master.repository.PointTransactionRepository;
import com.moleep.toeic_master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Transactional
    public ScoreResponse addScore(Long userId, ScoreType type, Long refId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        // 중복 지급 확인
        if (pointTransactionRepository.existsByUserIdAndTypeAndRefId(userId, type, refId)) {
            return ScoreResponse.builder()
                    .score(user.getScore())
                    .delta(0)
                    .build();
        }

        int delta = type.getPoints();

        // 점수 추가
        user.setScore(user.getScore() + delta);

        // 트랜잭션 기록
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .type(type)
                .refId(refId)
                .delta(delta)
                .build();
        pointTransactionRepository.save(transaction);

        return ScoreResponse.builder()
                .score(user.getScore())
                .delta(delta)
                .build();
    }
}
