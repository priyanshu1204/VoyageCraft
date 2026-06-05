package com.voyagecraft.repository;

import com.voyagecraft.entity.Trip;
import com.voyagecraft.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT t FROM Trip t JOIN t.collaborators c WHERE c.user.id = :userId AND c.invitationStatus = 'ACCEPTED' ORDER BY t.createdAt DESC")
    List<Trip> findCollaboratingTrips(@Param("userId") Long userId);

    @Query("SELECT t FROM Trip t WHERE t.createdBy.id = :userId OR (t.id IN (SELECT c.trip.id FROM TripCollaborator c WHERE c.user.id = :userId AND c.invitationStatus = 'ACCEPTED')) ORDER BY t.createdAt DESC")
    List<Trip> findAllUserTrips(@Param("userId") Long userId);

    List<Trip> findByCreatedByIdAndStatus(Long userId, TripStatus status);

    long countByCreatedById(Long userId);
}
