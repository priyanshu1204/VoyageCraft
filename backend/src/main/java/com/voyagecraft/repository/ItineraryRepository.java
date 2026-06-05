package com.voyagecraft.repository;

import com.voyagecraft.entity.Itinerary;
import com.voyagecraft.enums.ItineraryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

    List<Itinerary> findByTripIdOrderByVersionNumberAsc(Long tripId);

    Optional<Itinerary> findByTripIdAndIsActiveTrue(Long tripId);

    @Query("SELECT MAX(i.versionNumber) FROM Itinerary i WHERE i.trip.id = :tripId")
    Optional<Integer> findMaxVersionNumberByTripId(@Param("tripId") Long tripId);

    List<Itinerary> findByTripIdAndStatus(Long tripId, ItineraryStatus status);

    long countByTripId(Long tripId);
}
