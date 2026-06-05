package com.voyagecraft.repository;

import com.voyagecraft.entity.ChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long> {
    List<ChangeLog> findByTripIdOrderByCreatedAtDesc(Long tripId);
    List<ChangeLog> findTop50ByTripIdOrderByCreatedAtDesc(Long tripId);
}
