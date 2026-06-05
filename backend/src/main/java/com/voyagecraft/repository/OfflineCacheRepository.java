package com.voyagecraft.repository;

import com.voyagecraft.entity.OfflineCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfflineCacheRepository extends JpaRepository<OfflineCache, Long> {

    Optional<OfflineCache> findByTripIdAndUserId(Long tripId, Long userId);

    List<OfflineCache> findByUserIdOrderByLastSyncedAtDesc(Long userId);

    void deleteByTripIdAndUserId(Long tripId, Long userId);

    void deleteByTripId(Long tripId);
}
