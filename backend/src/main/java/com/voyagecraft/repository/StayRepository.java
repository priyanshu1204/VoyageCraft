package com.voyagecraft.repository;

import com.voyagecraft.entity.Stay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StayRepository extends JpaRepository<Stay, Long> {

    List<Stay> findByTripIdOrderByCheckInDateAsc(Long tripId);

    long countByTripId(Long tripId);
}
