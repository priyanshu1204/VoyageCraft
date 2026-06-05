package com.voyagecraft.repository;

import com.voyagecraft.entity.TripCollaborator;
import com.voyagecraft.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripCollaboratorRepository extends JpaRepository<TripCollaborator, Long> {
    List<TripCollaborator> findByTripId(Long tripId);
    Optional<TripCollaborator> findByTripIdAndUserId(Long tripId, Long userId);
    boolean existsByTripIdAndUserId(Long tripId, Long userId);
    List<TripCollaborator> findByUserIdAndInvitationStatus(Long userId, InvitationStatus status);
    long countByTripId(Long tripId);
}
