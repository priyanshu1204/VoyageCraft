package com.voyagecraft.repository;

import com.voyagecraft.entity.TripDestination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripDestinationRepository extends JpaRepository<TripDestination, Long> {
    List<TripDestination> findByTripIdOrderByOrderIndexAsc(Long tripId);
    void deleteByTripId(Long tripId);
}
