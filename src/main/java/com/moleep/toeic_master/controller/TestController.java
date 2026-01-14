package com.moleep.toeic_master.controller;

import com.moleep.toeic_master.dto.response.ApiResponse;
import com.moleep.toeic_master.entity.*;
import com.moleep.toeic_master.repository.*;
import com.moleep.toeic_master.service.EmbeddingService;
import com.moleep.toeic_master.service.S3Service;
import com.moleep.toeic_master.service.SchoolEmbeddingCache;
import com.moleep.toeic_master.service.StudyEmbeddingCache;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "Test", description = "테스트용 더미 데이터 API")
public class TestController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final SchoolRepository schoolRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final EmbeddingService embeddingService;
    private final S3Service s3Service;
    private final StudyEmbeddingCache studyEmbeddingCache;
    private final SchoolEmbeddingCache schoolEmbeddingCache;

    // ===== 유저 더미 데이터 =====
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

    // ===== 스터디 더미 데이터 =====
    private static final List<String> EXAM_TYPES = List.of("TOEIC", "TOEFL", "TEPS", "OPIc");
    private static final List<String> REGIONS = List.of(
            "서울", "대전", "부산", "인천", "광주", "대구", "울산", "세종",
            "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
    );
    private static final List<String> MEETING_FREQUENCIES = List.of(
            "주 1회", "주 2회", "주 3회", "주 4회", "주 5회", "주 6회", "매일"
    );
    private static final List<String> STUDY_TITLE_PREFIXES = List.of(
            "함께 성장하는", "열정 가득", "목표 달성", "합격 기원", "꾸준히 하는",
            "실력 향상", "고득점 목표", "즐겁게 공부하는", "매일 성장하는", "화이팅"
    );
    private static final List<String> STUDY_TITLE_SUFFIXES = List.of(
            "스터디", "모임", "그룹", "팀", "스터디 모임"
    );
    private static final List<String> STUDY_DESCRIPTIONS = List.of(
            "매일 꾸준히 공부하실 분들 환영합니다.",
            "목표 점수 달성까지 함께 달려요!",
            "서로 동기부여하며 성장하는 스터디입니다.",
            "출석 체크 철저히 하고 있어요.",
            "주 1회 온라인 미팅으로 진도 체크합니다.",
            "단어 암기 인증 필수입니다.",
            "LC/RC 파트별로 집중 공략합니다.",
            "실전 모의고사 매주 풀이합니다.",
            "오답 노트 공유하며 함께 성장해요.",
            "시험 일정에 맞춰 체계적으로 준비합니다.",
            "초보자도 환영하는 스터디입니다.",
            "고득점자 멘토링도 진행합니다.",
            "자료 공유 활발히 하고 있어요.",
            "분위기 좋은 스터디 찾으시는 분 환영!",
            "책임감 있는 분들만 지원해주세요.",
            "중도 포기 없이 끝까지 함께해요.",
            "매일 학습량 체크하고 있습니다.",
            "주말 집중 스터디입니다.",
            "평일 저녁 시간대에 진행합니다.",
            "아침형 스터디원 구합니다.",
            "직장인 친화적인 스터디예요.",
            "대학생 위주로 운영됩니다.",
            "취준생분들 같이 준비해요.",
            "함께 시험 접수하고 응시합니다.",
            "시험 후기 공유도 활발해요.",
            "스터디 카페에서 오프라인 모임합니다.",
            "줌으로 화상 스터디 진행합니다.",
            "카카오톡 오픈채팅으로 소통해요.",
            "디스코드 서버 운영 중입니다.",
            "노션으로 학습 기록 관리합니다.",
            "성실하게 참여하시는 분 환영합니다.",
            "서로 존중하며 공부하는 분위기입니다.",
            "질문과 토론 활발한 스터디예요.",
            "파트별 전문가가 도와드립니다.",
            "기출문제 분석 중심으로 진행합니다.",
            "약점 파트 집중 공략합니다.",
            "시간 관리 훈련도 함께해요.",
            "실전처럼 타이머 맞춰서 풀이합니다.",
            "오답률 높은 문제 유형 분석합니다.",
            "매달 모의고사 성적 비교합니다.",
            "목표 점수 달성 시 축하 이벤트!",
            "스터디 종료 후에도 인연 이어가요.",
            "편하게 질문할 수 있는 분위기입니다.",
            "늦은 시간도 괜찮으신 분 환영!",
            "주중 낮 시간대 가능하신 분 구해요.",
            "유연한 일정 조율 가능합니다.",
            "개인 사정 고려해드려요.",
            "장기 스터디 목표로 운영합니다.",
            "단기 집중 스터디입니다.",
            "시험 직전 막판 스퍼트용 스터디예요."
    );

    @PostMapping(value = "/dummy-studies", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "더미 스터디 생성", description = "모든 더미 유저에 대해 스터디를 생성합니다")
    public ResponseEntity<ApiResponse<Integer>> createDummyStudies(
            @Parameter(description = "유저당 생성할 스터디 수") @RequestParam(defaultValue = "2") int countPerUser) {

        List<User> dummyUsers = userRepository.findAll().stream()
                .filter(user -> user.getEmail().startsWith("dummy") && user.getEmail().endsWith("@gmail.com"))
                .collect(Collectors.toList());

        if (dummyUsers.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("더미 유저가 없습니다. 먼저 더미 유저를 생성해주세요.", 0));
        }

        Random random = new Random();
        int totalCreated = 0;

        for (User user : dummyUsers) {
            for (int i = 0; i < countPerUser; i++) {
                Study study = createRandomStudy(user, random);

                // 임베딩 생성
                if (study.getContent() != null && !study.getContent().isBlank()) {
                    float[] embedding = embeddingService.getEmbedding(study.getContent());
                    if (embedding != null) {
                        study.setEmbedding(embeddingService.floatArrayToBytes(embedding));
                    }
                }

                studyRepository.save(study);

                // 캐시 업데이트
                if (study.getEmbedding() != null) {
                    studyEmbeddingCache.put(study.getId(), embeddingService.bytesToFloatArray(study.getEmbedding()));
                }

                StudyMember leader = StudyMember.builder()
                        .study(study)
                        .user(user)
                        .role(MemberRole.LEADER)
                        .build();
                studyMemberRepository.save(leader);

                totalCreated++;
            }
        }

        return ResponseEntity.ok(ApiResponse.success(
                dummyUsers.size() + "명의 유저에 대해 총 " + totalCreated + "개의 스터디가 생성되었습니다",
                totalCreated
        ));
    }

    private Study createRandomStudy(User user, Random random) {
        String examType = EXAM_TYPES.get(random.nextInt(EXAM_TYPES.size()));
        String region = REGIONS.get(random.nextInt(REGIONS.size()));
        String meetingFrequency = MEETING_FREQUENCIES.get(random.nextInt(MEETING_FREQUENCIES.size()));
        StudyType studyType = StudyType.values()[random.nextInt(StudyType.values().length)];
        int maxMembers = random.nextInt(8) + 3; // 3~10명
        Integer targetScore = generateTargetScore(examType, random);

        String title = generateStudyTitle(examType, region, targetScore, random);
        String content = generateStudyContent(random);

        return Study.builder()
                .user(user)
                .title(title)
                .content(content)
                .examType(examType)
                .region(region)
                .targetScore(targetScore)
                .maxMembers(maxMembers)
                .studyType(studyType)
                .meetingFrequency(meetingFrequency)
                .status(StudyStatus.RECRUITING)
                .build();
    }

    private Integer generateTargetScore(String examType, Random random) {
        return switch (examType) {
            case "TOEIC" -> (random.nextInt(45) + 55) * 10; // 550~990 (10점 단위)
            case "TOEFL" -> random.nextInt(61) + 60; // 60~120
            case "TEPS" -> random.nextInt(301) + 300; // 300~600
            case "OPIc" -> null; // OPIc는 레벨제라 점수 없음
            default -> null;
        };
    }

    private String generateStudyTitle(String examType, String region, Integer targetScore, Random random) {
        String prefix = STUDY_TITLE_PREFIXES.get(random.nextInt(STUDY_TITLE_PREFIXES.size()));
        String suffix = STUDY_TITLE_SUFFIXES.get(random.nextInt(STUDY_TITLE_SUFFIXES.size()));

        // 다양한 제목 패턴
        int pattern = random.nextInt(4);
        return switch (pattern) {
            case 0 -> String.format("[%s] %s %s", examType, prefix, suffix);
            case 1 -> String.format("[%s/%s] %s %s", examType, region, prefix, suffix);
            case 2 -> targetScore != null
                    ? String.format("[%s %d+] %s", examType, targetScore, suffix)
                    : String.format("[%s] %s %s", examType, prefix, suffix);
            default -> String.format("%s %s %s", region, examType, suffix);
        };
    }

    private String generateStudyContent(Random random) {
        List<String> shuffled = new ArrayList<>(STUDY_DESCRIPTIONS);
        Collections.shuffle(shuffled, random);
        int count = random.nextInt(3) + 3; // 3~5개
        return shuffled.stream()
                .limit(count)
                .collect(Collectors.joining(" "));
    }

    // ===== 리뷰 더미 데이터 =====
    // 추천 관련
    private static final List<String> REVIEW_RECOMMEND_POSITIVE = List.of(
            "다음에도 이 고사장에서 시험 볼 예정입니다.",
            "친구들에게도 추천했어요!",
            "여기서 시험 보길 잘했다고 생각해요.",
            "다른 고사장보다 훨씬 좋았습니다.",
            "강력 추천하는 고사장이에요."
    );
    private static final List<String> REVIEW_RECOMMEND_NEGATIVE = List.of(
            "다음에는 다른 고사장에서 볼 것 같아요.",
            "솔직히 추천하기 어렵네요.",
            "다른 고사장을 알아보시는 게 좋을 것 같아요.",
            "개인적으로 비추천입니다.",
            "다시는 여기서 안 볼 것 같아요."
    );

    // 시설 관련
    private static final List<String> REVIEW_FACILITY_POSITIVE = List.of(
            "시설이 전반적으로 깔끔했어요.",
            "책상이 넓어서 좋았습니다.",
            "의자가 편해서 집중하기 좋았어요.",
            "건물이 최신식이라 쾌적했습니다.",
            "화장실도 깨끗하고 좋았어요.",
            "책상 간격이 넉넉해서 편했습니다."
    );
    private static final List<String> REVIEW_FACILITY_NEGATIVE = List.of(
            "시설이 좀 노후되어 있었어요.",
            "책상이 좁아서 불편했습니다.",
            "의자가 딱딱해서 오래 앉기 힘들었어요.",
            "건물이 오래되어서 좀 그랬어요.",
            "화장실이 멀어서 불편했습니다.",
            "책상이 흔들려서 신경 쓰였어요."
    );

    // 조용함 관련
    private static final List<String> REVIEW_QUIET_POSITIVE = List.of(
            "주변이 조용해서 집중 잘 됐어요.",
            "소음이 거의 없었습니다.",
            "리스닝 들을 때 방해 없이 잘 들렸어요.",
            "교실 밖 소음이 전혀 안 들렸어요.",
            "방음이 잘 되어있는 것 같아요."
    );
    private static final List<String> REVIEW_QUIET_NEGATIVE = List.of(
            "외부 소음이 좀 있었어요.",
            "복도에서 소리가 들려서 신경 쓰였습니다.",
            "리스닝 시간에 잡음이 좀 있었어요.",
            "창문 밖 소리가 들려서 집중하기 어려웠어요.",
            "다른 교실 소리가 들렸습니다."
    );

    // 접근성 관련
    private static final List<String> REVIEW_ACCESS_POSITIVE = List.of(
            "대중교통으로 오기 편했어요.",
            "지하철역에서 가까워서 좋았습니다.",
            "버스 정류장이 바로 앞이에요.",
            "주차장도 넓어서 차로 오기도 좋아요.",
            "찾아오기 쉬웠습니다."
    );
    private static final List<String> REVIEW_ACCESS_NEGATIVE = List.of(
            "대중교통으로 오기 좀 불편했어요.",
            "역에서 걸어오는데 시간이 좀 걸렸습니다.",
            "버스가 자주 안 와서 힘들었어요.",
            "주차하기가 어려웠습니다.",
            "찾아오는 길이 복잡했어요."
    );

    // 청결 관련
    private static final List<String> REVIEW_CLEAN_POSITIVE = List.of(
            "교실이 정말 깨끗했어요.",
            "청소 상태가 좋았습니다.",
            "먼지 하나 없이 깔끔했어요.",
            "위생 상태가 좋아서 안심됐습니다.",
            "정리정돈이 잘 되어있었어요."
    );
    private static final List<String> REVIEW_CLEAN_NEGATIVE = List.of(
            "먼지가 좀 있었어요.",
            "청소가 덜 된 느낌이었습니다.",
            "책상이 좀 지저분했어요.",
            "바닥이 깨끗하지 않았습니다.",
            "전반적으로 청결하지 않았어요."
    );

    // 난방/냉방 관련
    private static final List<String> REVIEW_TEMP_POSITIVE = List.of(
            "실내 온도가 적당해서 좋았어요.",
            "에어컨이 잘 나와서 쾌적했습니다.",
            "난방이 잘 되어서 따뜻했어요.",
            "온도 조절이 잘 되어있었습니다.",
            "덥지도 춥지도 않아서 좋았어요."
    );
    private static final List<String> REVIEW_TEMP_NEGATIVE = List.of(
            "실내가 좀 추웠어요. 겉옷 챙기세요.",
            "에어컨이 너무 세서 추웠습니다.",
            "난방이 안 돼서 손이 시려웠어요.",
            "온도가 너무 높아서 졸렸습니다.",
            "환기가 안 돼서 답답했어요."
    );

    // 감독관 관련
    private static final List<String> REVIEW_SUPERVISOR_POSITIVE = List.of(
            "감독관분들이 친절하셨어요.",
            "진행이 매끄러웠습니다.",
            "설명을 명확하게 해주셨어요.",
            "시간 안내를 정확히 해주셨습니다.",
            "질문에 친절하게 답해주셨어요."
    );
    private static final List<String> REVIEW_SUPERVISOR_NEGATIVE = List.of(
            "감독관분이 좀 불친절했어요.",
            "진행이 매끄럽지 않았습니다.",
            "설명이 좀 부족했어요.",
            "시간 안내가 정확하지 않았습니다.",
            "감독관분이 돌아다녀서 신경 쓰였어요."
    );

    // 일반적인 후기
    private static final List<String> REVIEW_GENERAL = List.of(
            "전체적으로 무난했습니다.",
            "시험 보기 괜찮은 환경이었어요.",
            "큰 불편함 없이 시험 봤습니다.",
            "나쁘지 않았어요.",
            "평범한 고사장이었습니다.",
            "시험에 집중할 수 있었어요.",
            "무사히 시험 마쳤습니다.",
            "생각보다 괜찮았어요.",
            "기대했던 것과 비슷했습니다.",
            "특별히 좋거나 나쁜 점은 없었어요."
    );

    @PostMapping(value = "/dummy-reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "더미 리뷰 생성", description = "모든 더미 유저가 각 학교별로 확률적으로 리뷰를 생성합니다")
    public ResponseEntity<ApiResponse<Integer>> createDummyReviews(
            @Parameter(description = "각 유저가 각 학교에 리뷰를 작성할 확률 (0~100)") @RequestParam(defaultValue = "10") int reviewProbability,
            @Parameter(description = "리뷰에 사진이 포함될 확률 (0~100)") @RequestParam(defaultValue = "15") int imageProbability,
            @Parameter(description = "리뷰에 첨부할 이미지 파일들") @RequestParam(required = false) List<MultipartFile> images) {

        List<User> dummyUsers = userRepository.findAll().stream()
                .filter(user -> user.getEmail().startsWith("dummy") && user.getEmail().endsWith("@gmail.com"))
                .collect(Collectors.toList());

        if (dummyUsers.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("더미 유저가 없습니다. 먼저 더미 유저를 생성해주세요.", 0));
        }

        List<School> schools = schoolRepository.findAll();
        if (schools.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("학교가 없습니다.", 0));
        }

        // 이미지 파일들을 S3에 업로드하고 key 목록 생성
        List<String> uploadedImageKeys = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String key = s3Service.upload(image, "dummy-reviews");
                    uploadedImageKeys.add(key);
                }
            }
        }

        Random random = new Random();
        int totalCreated = 0;
        List<Long> affectedSchoolIds = new ArrayList<>();

        for (User user : dummyUsers) {
            for (School school : schools) {
                // 이미 리뷰를 작성했는지 확인
                if (reviewRepository.existsByUserIdAndSchoolId(user.getId(), school.getId())) {
                    continue;
                }

                // 확률적으로 리뷰 생성
                if (random.nextInt(100) >= reviewProbability) {
                    continue;
                }

                Review review = createRandomReview(user, school, random);
                reviewRepository.save(review);

                // 확률적으로 이미지 첨부
                if (!uploadedImageKeys.isEmpty() && random.nextInt(100) < imageProbability) {
                    int imageCount = random.nextInt(2) + 1; // 1~2장
                    List<String> shuffledKeys = new ArrayList<>(uploadedImageKeys);
                    Collections.shuffle(shuffledKeys, random);

                    for (int i = 0; i < Math.min(imageCount, shuffledKeys.size()); i++) {
                        String imageKey = shuffledKeys.get(i);
                        ReviewImage image = ReviewImage.builder()
                                .review(review)
                                .imageUrl(imageKey)
                                .imageKey(imageKey)
                                .originalFilename("dummy_image_" + i + ".jpg")
                                .build();
                        reviewImageRepository.save(image);
                        review.getImages().add(image);
                    }
                }

                school.getReviews().add(review);
                if (!affectedSchoolIds.contains(school.getId())) {
                    affectedSchoolIds.add(school.getId());
                }
                totalCreated++;
            }
        }

        // 모든 리뷰 생성 후 학교별 평점 업데이트 및 임베딩 생성
        for (Long schoolId : affectedSchoolIds) {
            School school = schoolRepository.findById(schoolId).orElse(null);
            if (school != null) {
                school.updateAvgRating();
                schoolRepository.save(school);
                updateSchoolEmbedding(school);
            }
        }

        return ResponseEntity.ok(ApiResponse.success(
                totalCreated + "개의 리뷰가 생성되었습니다. " + affectedSchoolIds.size() + "개 학교의 평가가 업데이트되었습니다. (업로드된 이미지: " + uploadedImageKeys.size() + "개)",
                totalCreated
        ));
    }

    private Review createRandomReview(User user, School school, Random random) {
        boolean recommended = random.nextBoolean();
        boolean facilityGood = random.nextBoolean();
        boolean quiet = random.nextBoolean();
        boolean accessible = random.nextBoolean();

        // rating은 Boolean 값들에 따라 가중치 부여 (1~5)
        int baseRating = 3;
        if (recommended) baseRating++;
        if (facilityGood) baseRating++;
        if (!recommended && !facilityGood) baseRating--;
        int rating = Math.max(1, Math.min(5, baseRating + random.nextInt(2) - 1));

        String content = generateReviewContent(recommended, facilityGood, quiet, accessible, random);

        return Review.builder()
                .user(user)
                .school(school)
                .rating(rating)
                .content(content)
                .recommended(recommended)
                .facilityGood(facilityGood)
                .quiet(quiet)
                .accessible(accessible)
                .build();
    }

    private String generateReviewContent(boolean recommended, boolean facilityGood, boolean quiet, boolean accessible, Random random) {
        List<String> sentences = new ArrayList<>();

        // Boolean 값에 따른 문장 추가
        if (recommended) {
            sentences.add(REVIEW_RECOMMEND_POSITIVE.get(random.nextInt(REVIEW_RECOMMEND_POSITIVE.size())));
        } else {
            sentences.add(REVIEW_RECOMMEND_NEGATIVE.get(random.nextInt(REVIEW_RECOMMEND_NEGATIVE.size())));
        }

        if (facilityGood) {
            sentences.add(REVIEW_FACILITY_POSITIVE.get(random.nextInt(REVIEW_FACILITY_POSITIVE.size())));
        } else {
            sentences.add(REVIEW_FACILITY_NEGATIVE.get(random.nextInt(REVIEW_FACILITY_NEGATIVE.size())));
        }

        if (quiet) {
            sentences.add(REVIEW_QUIET_POSITIVE.get(random.nextInt(REVIEW_QUIET_POSITIVE.size())));
        } else {
            sentences.add(REVIEW_QUIET_NEGATIVE.get(random.nextInt(REVIEW_QUIET_NEGATIVE.size())));
        }

        if (accessible) {
            sentences.add(REVIEW_ACCESS_POSITIVE.get(random.nextInt(REVIEW_ACCESS_POSITIVE.size())));
        } else {
            sentences.add(REVIEW_ACCESS_NEGATIVE.get(random.nextInt(REVIEW_ACCESS_NEGATIVE.size())));
        }

        // 추가 문장들 (청결, 온도, 감독관, 일반)
        boolean positiveOverall = recommended && facilityGood;

        if (random.nextBoolean()) {
            if (positiveOverall || random.nextBoolean()) {
                sentences.add(REVIEW_CLEAN_POSITIVE.get(random.nextInt(REVIEW_CLEAN_POSITIVE.size())));
            } else {
                sentences.add(REVIEW_CLEAN_NEGATIVE.get(random.nextInt(REVIEW_CLEAN_NEGATIVE.size())));
            }
        }

        if (random.nextBoolean()) {
            if (positiveOverall || random.nextBoolean()) {
                sentences.add(REVIEW_TEMP_POSITIVE.get(random.nextInt(REVIEW_TEMP_POSITIVE.size())));
            } else {
                sentences.add(REVIEW_TEMP_NEGATIVE.get(random.nextInt(REVIEW_TEMP_NEGATIVE.size())));
            }
        }

        if (random.nextBoolean()) {
            if (positiveOverall || random.nextBoolean()) {
                sentences.add(REVIEW_SUPERVISOR_POSITIVE.get(random.nextInt(REVIEW_SUPERVISOR_POSITIVE.size())));
            } else {
                sentences.add(REVIEW_SUPERVISOR_NEGATIVE.get(random.nextInt(REVIEW_SUPERVISOR_NEGATIVE.size())));
            }
        }

        // 일반 문장 추가
        if (random.nextInt(3) == 0) {
            sentences.add(REVIEW_GENERAL.get(random.nextInt(REVIEW_GENERAL.size())));
        }

        // 2~5개 문장으로 제한
        Collections.shuffle(sentences, random);
        int count = Math.min(sentences.size(), random.nextInt(4) + 2);

        return sentences.stream()
                .limit(count)
                .collect(Collectors.joining(" "));
    }

    private void updateSchoolEmbedding(School school) {
        List<String> reviewContents = school.getReviews().stream()
                .map(Review::getContent)
                .filter(content -> content != null && !content.isBlank())
                .toList();

        if (reviewContents.isEmpty()) {
            school.setEvaluation(null);
            school.setEmbedding(null);
            schoolEmbeddingCache.remove(school.getId());
            schoolRepository.save(school);
            return;
        }

        EmbeddingService.VenueEvalResult result = embeddingService.getVenueEvaluation(reviewContents);
        if (result != null) {
            school.setEvaluation(result.evaluation());
            school.setEmbedding(embeddingService.floatArrayToBytes(result.embedding()));
            schoolEmbeddingCache.put(school.getId(), result.embedding());
            schoolRepository.save(school);
        }
    }
}
