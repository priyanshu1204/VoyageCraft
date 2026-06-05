package com.voyagecraft.repository;

import com.voyagecraft.entity.TransportDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportDetailRepository extends JpaRepository<TransportDetail, Long> {

    List<TransportDetail> findByTripIdOrderByDepartureTimeAsc(Long tripId);

    long countByTripId(Long tripId);
}
