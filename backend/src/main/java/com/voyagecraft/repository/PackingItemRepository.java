package com.voyagecraft.repository;

import com.voyagecraft.entity.PackingItem;
import com.voyagecraft.enums.PackingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackingItemRepository extends JpaRepository<PackingItem, Long> {
    List<PackingItem> findByTripIdOrderByCategoryAscNameAsc(Long tripId);
    List<PackingItem> findByTripIdAndCategory(Long tripId, PackingCategory category);
    long countByTripId(Long tripId);
    long countByTripIdAndPackedTrue(Long tripId);
}
