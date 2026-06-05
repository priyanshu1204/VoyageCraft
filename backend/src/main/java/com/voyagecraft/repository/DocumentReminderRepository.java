package com.voyagecraft.repository;

import com.voyagecraft.entity.DocumentReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DocumentReminderRepository extends JpaRepository<DocumentReminder, Long> {

    List<DocumentReminder> findByTravelDocumentIdOrderByReminderDateAsc(Long travelDocumentId);

    @Query("SELECT r FROM DocumentReminder r WHERE r.travelDocument.user.id = :userId AND r.dismissed = false AND r.reminderDate <= :date ORDER BY r.reminderDate ASC")
    List<DocumentReminder> findUpcomingReminders(@Param("userId") Long userId, @Param("date") LocalDate date);
}
