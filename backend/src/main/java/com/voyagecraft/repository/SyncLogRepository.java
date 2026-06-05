package com.voyagecraft.repository;

import com.voyagecraft.entity.SyncLog;
import com.voyagecraft.enums.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyncLogRepository extends JpaRepository<SyncLog, Long> {

    List<SyncLog> findByUserIdAndTripIdOrderByCreatedAtDesc(Long userId, Long tripId);

    List<SyncLog> findByUserIdAndSyncStatusOrderByCreatedAtAsc(Long userId, SyncStatus status);

    List<SyncLog> findByTripIdAndSyncStatusOrderByCreatedAtAsc(Long tripId, SyncStatus status);

    long countByUserIdAndSyncStatus(Long userId, SyncStatus status);

    void deleteByTripId(Long tripId);
}
