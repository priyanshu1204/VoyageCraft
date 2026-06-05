package com.voyagecraft.repository;

import com.voyagecraft.entity.QuickNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuickNoteRepository extends JpaRepository<QuickNote, Long> {

    List<QuickNote> findByTripIdAndUserIdOrderByCreatedAtDesc(Long tripId, Long userId);

    List<QuickNote> findByTripIdOrderByCreatedAtDesc(Long tripId);

    long countByTripIdAndUserId(Long tripId, Long userId);
}
