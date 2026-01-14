package com.moleep.toeic_master.controller;

import com.moleep.toeic_master.dto.response.ApiResponse;
import com.moleep.toeic_master.entity.User;
import com.moleep.toeic_master.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "Test", description = "테스트용 더미 데이터 API")
public class TestController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final List<String> BIOS = List.of(
            "이번에는 꼭 합격!",
            "토익 900 목표로 달리는 중",
            "매일 단어 100개씩 외우기",
            "LC 만점이 목표입니다",
            "RC 파트7이 너무 어려워요",
            "3개월 안에 850 달성할 거예요",
            "스터디 메이트 구해요~",
            "같이 공부해요!",
            "취준생입니다 화이팅",
            "대기업 입사 준비 중",
            "이직 준비하면서 토익 공부 중",
            "졸업 요건 충족해야 해요 ㅠㅠ",
            "토익 독학러입니다",
            "학원 다니면서 추가로 공부 중",
            "아침형 스터디 선호합니다",
            "주말 스터디 구합니다",
            "온라인 스터디 원해요",
            "오프라인 스터디 선호",
            "서울 강남권 스터디 찾아요",
            "대전 스터디 구합니다",
            "토익 초보인데 같이 해요",
            "700점대인데 800 넘기고 싶어요",
            "파트5 문법이 약해요",
            "리스닝 집중 훈련 중",
            "실전 모의고사 같이 풀 분",
            "단어장 공유해요",
            "토익 꿀팁 나눠요",
            "시험 전날 벼락치기 중",
            "이번 달 시험 접수했어요",
            "드디어 목표 점수 달성!"
    );

    private static final List<String> ADJECTIVES = List.of(
            "열정적인", "성실한", "꾸준한", "노력하는", "도전하는",
            "밝은", "긍정적인", "활발한", "차분한", "집중하는",
            "빠른", "느긋한", "부지런한", "똑똑한", "귀여운"
    );

    private static final List<String> NOUNS = List.of(
            "토익러", "수험생", "직장인", "대학생", "취준생",
            "공시생", "스터디원", "독학러", "고수", "뉴비",
            "펭귄", "고양이", "강아지", "토끼", "다람쥐"
    );

    @PostMapping(value = "/dummy-users", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "더미 유저 생성", description = "테스트용 더미 유저를 생성합니다")
    public ResponseEntity<ApiResponse<List<Long>>> createDummyUsers(
            @Parameter(description = "생성할 유저 수") @RequestParam(defaultValue = "10") int count) {

        Random random = new Random();
        String encodedPassword = passwordEncoder.encode("password");
        List<Long> createdIds = new ArrayList<>();

        long maxDummyNumber = findMaxDummyNumber();

        for (int i = 1; i <= count; i++) {
            long number = maxDummyNumber + i;
            String email = "dummy" + number + "@gmail.com";

            if (userRepository.existsByEmail(email)) {
                continue;
            }

            String nickname = generateUniqueNickname(random);
            String bio = BIOS.get(random.nextInt(BIOS.size()));

            User user = User.builder()
                    .email(email)
                    .password(encodedPassword)
                    .nickname(nickname)
                    .bio(bio)
                    .build();

            userRepository.save(user);
            createdIds.add(user.getId());
        }

        return ResponseEntity.ok(ApiResponse.success(
                createdIds.size() + "명의 더미 유저가 생성되었습니다",
                createdIds
        ));
    }

    private long findMaxDummyNumber() {
        return userRepository.findAll().stream()
                .map(User::getEmail)
                .filter(email -> email.startsWith("dummy") && email.endsWith("@gmail.com"))
                .map(email -> email.replace("dummy", "").replace("@gmail.com", ""))
                .filter(num -> num.matches("\\d+"))
                .mapToLong(Long::parseLong)
                .max()
                .orElse(0);
    }

    private String generateUniqueNickname(Random random) {
        for (int attempt = 0; attempt < 100; attempt++) {
            String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
            String noun = NOUNS.get(random.nextInt(NOUNS.size()));
            String suffix = String.valueOf(random.nextInt(1000));
            String nickname = adjective + noun + suffix;

            if (!userRepository.existsByNickname(nickname)) {
                return nickname;
            }
        }
        return "유저" + System.currentTimeMillis();
    }
}
