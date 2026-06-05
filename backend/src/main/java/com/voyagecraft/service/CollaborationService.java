package com.voyagecraft.service;

import com.voyagecraft.dto.collab.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.ChangeAction;
import com.voyagecraft.enums.PollStatus;
import com.voyagecraft.exception.ResourceNotFoundException;
import com.voyagecraft.exception.UnauthorizedException;
import com.voyagecraft.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborationService {

    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;
    private final CommentRepository commentRepository;
    private final ChangeLogRepository changeLogRepository;
    private final TripRepository tripRepository;

    // ── Polls ────────────────────────────────────────────────────────────

    @Transactional
    public PollResponse createPoll(PollRequest request, User user) {
        Trip trip = getTripChecked(request.getTripId(), user);

        Poll poll = Poll.builder()
                .trip(trip).createdBy(user)
                .question(request.getQuestion())
                .category(request.getCategory())
                .allowMultipleVotes(request.getAllowMultipleVotes() != null ? request.getAllowMultipleVotes() : false)
                .build();

        List<PollOption> options = new ArrayList<>();
        for (String text : request.getOptions()) {
            options.add(PollOption.builder().poll(poll).optionText(text).build());
        }
        poll.setOptions(options);
        Poll saved = pollRepository.save(poll);

        logChange(trip, user, ChangeAction.CREATED, "poll", saved.getId(),
                user.getFirstName() + " created poll: \"" + request.getQuestion() + "\"");

        return mapPoll(saved, user);
    }

    @Transactional(readOnly = true)
    public List<PollResponse> getTripPolls(Long tripId, User user) {
        getTripChecked(tripId, user);
        return pollRepository.findByTripIdOrderByCreatedAtDesc(tripId).stream()
                .map(p -> mapPoll(p, user)).collect(Collectors.toList());
    }

    @Transactional
    public PollResponse vote(Long optionId, User user) {
        PollOption option = findOption(optionId);
        Poll poll = option.getPoll();
        getTripChecked(poll.getTrip().getId(), user);

        if (poll.getStatus() == PollStatus.CLOSED) {
            throw new IllegalStateException("Poll is closed");
        }

        // Toggle vote
        var existing = pollVoteRepository.findByPollOptionIdAndUserId(optionId, user.getId());
        if (existing.isPresent()) {
            pollVoteRepository.delete(existing.get());
        } else {
            if (!poll.getAllowMultipleVotes()) {
                // Remove votes on other options in this poll
                for (PollOption o : poll.getOptions()) {
                    pollVoteRepository.findByPollOptionIdAndUserId(o.getId(), user.getId())
                            .ifPresent(pollVoteRepository::delete);
                }
            }
            pollVoteRepository.save(PollVote.builder().pollOption(option).user(user).build());
        }

        logChange(poll.getTrip(), user, ChangeAction.VOTED, "poll", poll.getId(),
                user.getFirstName() + " voted on poll: \"" + poll.getQuestion() + "\"");

        // Re-fetch to get updated counts
        Poll updated = pollRepository.findById(poll.getId()).orElseThrow();
        return mapPoll(updated, user);
    }

    @Transactional
    public PollResponse closePoll(Long pollId, User user) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));
        getTripChecked(poll.getTrip().getId(), user);
        poll.setStatus(PollStatus.CLOSED);
        return mapPoll(pollRepository.save(poll), user);
    }

    @Transactional
    public void deletePoll(Long pollId, User user) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found"));
        getTripChecked(poll.getTrip().getId(), user);
        pollRepository.delete(poll);
    }

    // ── Comments ─────────────────────────────────────────────────────────

    @Transactional
    public CommentResponse addComment(CommentRequest request, User user) {
        Trip trip = getTripChecked(request.getTripId(), user);

        Comment comment = Comment.builder()
                .trip(trip).user(user)
                .itemType(request.getItemType())
                .itemId(request.getItemId())
                .content(request.getContent())
                .build();

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            comment.setParent(parent);
        }

        Comment saved = commentRepository.save(comment);

        logChange(trip, user, ChangeAction.COMMENTED, request.getItemType(), request.getItemId(),
                user.getFirstName() + " commented on " + request.getItemType() + ": \"" +
                        (request.getContent().length() > 50 ? request.getContent().substring(0, 50) + "..." : request.getContent()) + "\"");

        return mapComment(saved);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getItemComments(Long tripId, String itemType, Long itemId, User user) {
        getTripChecked(tripId, user);
        List<Comment> roots = commentRepository
                .findByTripIdAndItemTypeAndItemIdAndParentIsNullOrderByCreatedAtDesc(tripId, itemType, itemId);

        return roots.stream().map(c -> {
            CommentResponse resp = mapComment(c);
            List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(c.getId());
            resp.setReplies(replies.stream().map(this::mapComment).collect(Collectors.toList()));
            return resp;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        getTripChecked(comment.getTrip().getId(), user);
        commentRepository.delete(comment);
    }

    // ── Change Log ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ChangeLogResponse> getChangeLog(Long tripId, User user) {
        getTripChecked(tripId, user);
        return changeLogRepository.findTop50ByTripIdOrderByCreatedAtDesc(tripId).stream()
                .map(this::mapChangeLog).collect(Collectors.toList());
    }

    public void logChange(Trip trip, User user, ChangeAction action, String entityType, Long entityId, String description) {
        try {
            changeLogRepository.save(ChangeLog.builder()
                    .trip(trip).user(user).action(action)
                    .entityType(entityType).entityId(entityId)
                    .description(description).build());
        } catch (Exception e) {
            log.warn("Failed to log change: {}", e.getMessage());
        }
    }

    // ── Role check ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public String getUserRole(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
        if (trip.getCreatedBy().getId().equals(user.getId())) return "OWNER";

        return trip.getCollaborators().stream()
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .map(c -> c.getRole().name())
                .findFirst().orElse("VIEWER");
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Trip getTripChecked(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        boolean isOwner = trip.getCreatedBy().getId().equals(user.getId());
        boolean isCollaborator = trip.getCollaborators().stream()
                .anyMatch(c -> c.getUser().getId().equals(user.getId()));
        if (!isOwner && !isCollaborator) {
            throw new UnauthorizedException("Access denied");
        }
        return trip;
    }

    private PollOption findOption(Long optionId) {
        // Walk through polls — simple approach since PollOption doesn't have its own repo
        return pollRepository.findAll().stream()
                .flatMap(p -> p.getOptions().stream())
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Poll option not found"));
    }

    private PollResponse mapPoll(Poll p, User currentUser) {
        int totalVotes = p.getOptions().stream().mapToInt(o -> o.getVotes().size()).sum();

        List<PollOptionResponse> opts = p.getOptions().stream().map(o -> {
            int count = o.getVotes().size();
            return PollOptionResponse.builder()
                    .id(o.getId())
                    .optionText(o.getOptionText())
                    .voteCount(count)
                    .votePercent(totalVotes > 0 ? (count * 100.0 / totalVotes) : 0)
                    .voterNames(o.getVotes().stream().map(v -> v.getUser().getFirstName()).collect(Collectors.toList()))
                    .currentUserVoted(o.getVotes().stream().anyMatch(v -> v.getUser().getId().equals(currentUser.getId())))
                    .build();
        }).collect(Collectors.toList());

        return PollResponse.builder()
                .id(p.getId()).tripId(p.getTrip().getId())
                .question(p.getQuestion()).category(p.getCategory())
                .status(p.getStatus()).allowMultipleVotes(p.getAllowMultipleVotes())
                .createdByName(p.getCreatedBy().getFirstName())
                .createdById(p.getCreatedBy().getId())
                .options(opts).totalVotes(totalVotes)
                .createdAt(p.getCreatedAt()).build();
    }

    private CommentResponse mapComment(Comment c) {
        return CommentResponse.builder()
                .id(c.getId()).tripId(c.getTrip().getId())
                .itemType(c.getItemType()).itemId(c.getItemId())
                .content(c.getContent())
                .authorName(c.getUser().getFirstName())
                .authorId(c.getUser().getId())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .replies(new ArrayList<>())
                .createdAt(c.getCreatedAt()).build();
    }

    private ChangeLogResponse mapChangeLog(ChangeLog cl) {
        return ChangeLogResponse.builder()
                .id(cl.getId()).action(cl.getAction())
                .entityType(cl.getEntityType()).entityId(cl.getEntityId())
                .description(cl.getDescription())
                .userName(cl.getUser().getFirstName())
                .userId(cl.getUser().getId())
                .createdAt(cl.getCreatedAt()).build();
    }
}
