package com.voyagecraft.repository;

import com.voyagecraft.entity.NavigationRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NavigationRouteRepository extends JpaRepository<NavigationRoute, Long> {

    List<NavigationRoute> findByTripIdOrderByDayNumberAscOrderIndexAsc(Long tripId);

    List<NavigationRoute> findByTripIdAndDayNumberOrderByOrderIndexAsc(Long tripId, Integer dayNumber);

    void deleteByTripId(Long tripId);
}
