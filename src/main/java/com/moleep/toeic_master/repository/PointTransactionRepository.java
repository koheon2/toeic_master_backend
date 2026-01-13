package com.moleep.toeic_master.repository;

import com.moleep.toeic_master.entity.PointTransaction;
import com.moleep.toeic_master.entity.ScoreType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    boolean existsByUserIdAndTypeAndRefId(Long userId, ScoreType type, Long refId);
}
