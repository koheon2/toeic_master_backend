package com.moleep.toeic_master.config;

import com.moleep.toeic_master.entity.School;
import com.moleep.toeic_master.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final SchoolRepository schoolRepository;

    @Override
    @Transactional
    public void run(String... args) {
        loadSchools();
    }

    private void loadSchools() {
        if (schoolRepository.count() > 0) {
            log.info("Schools already exist, skipping initial data load");
            return;
        }

        List<School> schools = List.of(
                School.builder()
                        .name("갑천중")
                        .address("대전광역시 서구 월평동로 80")
                        .latitude(new BigDecimal("36.36143570"))
                        .longitude(new BigDecimal("127.36859460"))
                        .build(),
                School.builder()
                        .name("남선중")
                        .address("대전광역시 서구 월평북로 41 (월평동, 남선중학교)")
                        .latitude(new BigDecimal("36.36268670"))
                        .longitude(new BigDecimal("127.37169570"))
                        .build(),
                School.builder()
                        .name("둔산중")
                        .address("대전광역시 서구 갈마역로25번길 70")
                        .latitude(new BigDecimal("36.35343610"))
                        .longitude(new BigDecimal("127.37175820"))
                        .build(),
                School.builder()
                        .name("만년중")
                        .address("대전광역시 서구 만년남로 17")
                        .latitude(new BigDecimal("36.36660610"))
                        .longitude(new BigDecimal("127.37547130"))
                        .build(),
                School.builder()
                        .name("문정중")
                        .address("대전광역시 서구 둔산북로 232-11 (둔산동, 문정중학교)")
                        .latitude(new BigDecimal("36.35512930"))
                        .longitude(new BigDecimal("127.40053470"))
                        .build(),

                // ===== 추가된 학교 =====

                School.builder()
                        .name("송촌중")
                        .address("대전광역시 대덕구 계족산로 110")
                        .latitude(new BigDecimal("36.36396610"))
                        .longitude(new BigDecimal("127.44280800"))
                        .build(),

                School.builder()
                        .name("대전봉명중")
                        .address("대전광역시 유성구 계룡로132번길 71 (봉명동, 대전봉명중학교)")
                        .latitude(new BigDecimal("36.34943980"))
                        .longitude(new BigDecimal("127.34438890"))
                        .build(),

                School.builder()
                        .name("지족중")
                        .address("대전광역시 유성구 노은동로 193")
                        .latitude(new BigDecimal("36.37758990"))
                        .longitude(new BigDecimal("127.32006050"))
                        .build(),

                School.builder()
                        .name("하기중")
                        .address("대전광역시 유성구 송림로48번길 6-30 (하기동, 대전하기중학교)")
                        .latitude(new BigDecimal("36.38563140"))
                        .longitude(new BigDecimal("127.32179700"))
                        .build(),

                School.builder()
                        .name("대전여중")
                        .address("대전광역시 중구 보문로230번길 69")
                        .latitude(new BigDecimal("36.32483650"))
                        .longitude(new BigDecimal("127.42579660"))
                        .build(),

                School.builder()
                        .name("문화여중")
                        .address("대전광역시 중구 문화로218번길 63")
                        .latitude(new BigDecimal("36.31306560"))
                        .longitude(new BigDecimal("127.40905350"))
                        .build(),

                School.builder()
                        .name("충남여중")
                        .address("대전광역시 중구 동서대로1352번길 33")
                        .latitude(new BigDecimal("36.33007650"))
                        .longitude(new BigDecimal("127.41101070"))
                        .build(),

                //여기까지 토익

                School.builder()
                        .name("충남대학교국제언어교육센터")
                        .address("대전광역시 유성구 궁동 대학로 99")
                        .latitude(new BigDecimal("36.3688066"))
                        .longitude(new BigDecimal("127.3467804"))
                        .build(),

                School.builder()
                        .name("대덕대학교")
                        .address("대전광역시 유성구 장동로 48")
                        .latitude(new BigDecimal("36.3907123"))
                        .longitude(new BigDecimal("127.3654646"))
                        .build(),

                School.builder()
                        .name("카이스트")
                        .address("대전광역시 유성구 대학로 291")
                        .latitude(new BigDecimal("36.3721427"))
                        .longitude(new BigDecimal("127.36039"))
                        .build(),

                School.builder()
                        .name("둔산SDA")
                        .address("대전광역시 서구 한밭대로707번길 31")
                        .latitude(new BigDecimal("36.35921"))
                        .longitude(new BigDecimal("127.377204"))
                        .build(),

                School.builder()
                        .name("그라운드 에듀")
                        .address("대전광역시 서구 둔산로 69 금성빌딩 7층 704호")
                        .latitude(new BigDecimal("36.334444"))
                        .longitude(new BigDecimal("127.3365627"))
                        .build(),

                School.builder()
                        .name("대흥 리더스교육평가원")
                        .address("대전광역시 중구 대종로 417-1 (대흥동) 2층")
                        .latitude(new BigDecimal("36.3222244"))
                        .longitude(new BigDecimal("127.42934"))
                        .build()
        );


        schoolRepository.saveAll(schools);
        log.info("Loaded {} schools", schools.size());
    }
}
