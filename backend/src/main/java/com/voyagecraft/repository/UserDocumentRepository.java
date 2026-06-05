package com.voyagecraft.repository;

import com.voyagecraft.entity.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserDocumentRepository extends JpaRepository<UserDocument, Long> {
    List<UserDocument> findByTripIdOrderByExpiryDateAsc(Long tripId);
    List<UserDocument> findByUserIdOrderByExpiryDateAsc(Long userId);
    
    @Query("SELECT d FROM UserDocument d WHERE d.trip.id = :tripId AND d.expiryDate <= :thresholdDate AND d.expiryDate IS NOT NULL ORDER BY d.expiryDate ASC")
    List<UserDocument> findExpiringByTrip(Long tripId, LocalDate thresholdDate);
    
    @Query("SELECT d FROM UserDocument d WHERE d.user.id = :userId AND d.expiryDate <= :thresholdDate AND d.expiryDate IS NOT NULL ORDER BY d.expiryDate ASC")
    List<UserDocument> findExpiringByUser(Long userId, LocalDate thresholdDate);
}
