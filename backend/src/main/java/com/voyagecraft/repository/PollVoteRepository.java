package com.voyagecraft.repository;

import com.voyagecraft.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    Optional<PollVote> findByPollOptionIdAndUserId(Long optionId, Long userId);
    void deleteByPollOptionIdAndUserId(Long optionId, Long userId);
}
