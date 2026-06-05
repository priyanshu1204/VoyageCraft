package com.voyagecraft.repository;

import com.voyagecraft.entity.Activity;
import com.voyagecraft.enums.ActivityCategory;
import com.voyagecraft.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByTripIdOrderByActivityDateAscStartTimeAsc(Long tripId);

    List<Activity> findByTripIdAndCategory(Long tripId, ActivityCategory category);

    List<Activity> findByTripIdAndReservationStatus(Long tripId, ReservationStatus status);

    @Query("SELECT a FROM Activity a WHERE a.trip.id = :tripId AND a.tags LIKE %:tag%")
    List<Activity> findByTripIdAndTag(@Param("tripId") Long tripId, @Param("tag") String tag);

    @Query("SELECT a FROM Activity a WHERE a.reminderAt IS NOT NULL AND a.reminderAt <= :now AND a.reservationStatus IN ('PENDING','WAITLISTED')")
    List<Activity> findDueReminders(@Param("now") LocalDateTime now);

    long countByTripId(Long tripId);
}
