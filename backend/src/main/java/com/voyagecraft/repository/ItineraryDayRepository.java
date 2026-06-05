package com.voyagecraft.repository;

import com.voyagecraft.entity.ItineraryDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryDayRepository extends JpaRepository<ItineraryDay, Long> {
    List<ItineraryDay> findByItineraryIdOrderByDayIndexAsc(Long itineraryId);
}
