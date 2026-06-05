package com.voyagecraft.repository;

import com.voyagecraft.entity.DocumentChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChecklistRepository extends JpaRepository<DocumentChecklist, Long> {

    List<DocumentChecklist> findByTravelDocumentIdOrderByCreatedAtAsc(Long travelDocumentId);
}
