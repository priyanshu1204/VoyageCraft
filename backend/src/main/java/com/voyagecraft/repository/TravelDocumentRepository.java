package com.voyagecraft.repository;

import com.voyagecraft.entity.TravelDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelDocumentRepository extends JpaRepository<TravelDocument, Long> {

    List<TravelDocument> findByTripIdAndUserIdOrderByCountryNameAsc(Long tripId, Long userId);

    Optional<TravelDocument> findByTripIdAndUserIdAndCountryNameIgnoreCase(Long tripId, Long userId, String countryName);

    List<TravelDocument> findByTripIdOrderByCountryNameAsc(Long tripId);
}
