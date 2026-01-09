package com.moleep.toeic_master.dto.response;

import com.moleep.toeic_master.entity.School;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class SchoolResponse {
    private Long id;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal avgRating;
    private int reviewCount;

    public static SchoolResponse from(School school) {
        return SchoolResponse.builder()
                .id(school.getId())
                .name(school.getName())
                .address(school.getAddress())
                .latitude(school.getLatitude())
                .longitude(school.getLongitude())
                .avgRating(school.getAvgRating())
                .reviewCount(school.getReviews().size())
                .build();
    }
}
