package com.voyagecraft.repository;

import com.voyagecraft.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTripIdAndItemTypeAndItemIdAndParentIsNullOrderByCreatedAtDesc(
            Long tripId, String itemType, Long itemId);

    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    List<Comment> findByTripIdOrderByCreatedAtDesc(Long tripId);

    long countByTripId(Long tripId);
}
