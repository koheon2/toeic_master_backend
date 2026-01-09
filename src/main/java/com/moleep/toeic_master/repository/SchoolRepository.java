package com.moleep.toeic_master.repository;

import com.moleep.toeic_master.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

    @Query("SELECT s FROM School s WHERE " +
            "s.latitude BETWEEN :minLat AND :maxLat AND " +
            "s.longitude BETWEEN :minLng AND :maxLng")
    List<School> findByLocationBounds(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLng") BigDecimal minLng,
            @Param("maxLng") BigDecimal maxLng
    );

    List<School> findByNameContaining(String name);
}
