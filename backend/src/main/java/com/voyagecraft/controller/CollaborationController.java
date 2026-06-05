package com.voyagecraft.controller;

import com.voyagecraft.dto.collab.*;
import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.entity.User;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.CollaborationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/collab")
@RequiredArgsConstructor
@Tag(name = "Collaboration", description = "Polls, comments, change log (US-07)")
public class CollaborationController {

    private final CollaborationService collaborationService;
    private final AuthService authService;

    // ── Polls ────────────────────────────────────────────────────────────

    @PostMapping("/polls")
    @Operation(summary = "Create a poll")
    public ResponseEntity<ApiResponse<PollResponse>> createPoll(
            @Valid @RequestBody PollRequest request,
            @AuthenticationPrincipal UserDetails ud) {
        User user = authService.getUserByEmail(ud.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Poll created", collaborationService.createPoll(request, user)));
    }

    @GetMapping("/polls/trip/{tripId}")
    @Operation(summary = "Get all polls for a trip")
    public ResponseEntity<ApiResponse<List<PollResponse>>> getTripPolls(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails ud) {
        User user = authService.getUserByEmail(ud.getUsername());
        return ResponseEntity.ok(ApiResponse.success(collaborationService.getTripPolls(tripId, user)));
    }

    @PostMapping("/polls/vote/{optionId}")
    @Operation(summary = "Vote or unvote on a poll option")
    public ResponseEntity<ApiResponse<PollResponse>> vote(
            @PathVariable Long optionId,
            @AuthenticationPrincipal UserDetails ud) {
        User user = authService.getUserByEmail(ud.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Vote recorded", collaborationService.vote(optionId, user)));
    }

    @PutMapping("/polls/{pollId}/close")
    @Operation(summary = "Close a poll")
    public ResponseEntity<ApiResponse<PollResponse>> closePoll(
            @PathVariable Long pollId,
            @AuthenticationPrincipal UserDetails ud) {
        User user = authService.getUserByEmail(ud.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Poll closed", collaborationService.closePoll(pollId, user)));
    }

    @DeleteMapping("/polls/{pollId}")
    @Operation(summary = "Delete a poll")
    public ResponseEntity<ApiResponse<Void>> deletePoll(
            @PathVariable Long pollId,
            @AuthenticationPrincipal UserDetails ud) {
        User user = authService.getUserByEmail(ud.getUsername());
        collaborationService.deletePoll(pollId, user);
        return ResponseEntity.ok(ApiResponse.success("Poll deleted", null));
    }

    // ── Comments ─────────────────────────────────────────────────────────

    @PostMapping("/comments")
    @Operation(summary = "Add a comment")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails ud) {
        User user = authService.getUserByEmail(ud.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added", collaborationService.addComment(request, user)));
    }

    @GetMapping("/comments/trip/{tripId}")
    @Operation(summary = "Get comments for an item")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getItemComments(
            @PathVariable Long tripId,
            @RequestParam String itemType,
            @RequestParam Long itemId,
            @AuthenticationPrincipal UserDetails ud) {
        User user = authService.getUserByEmail(ud.getUsername());
        return ResponseEntity.ok(ApiResponse.success(
                collaborationService.getItemComments(tripId, itemType, itemId, user)));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails ud) {
        User user = authService.getUserByEmail(ud.getUsername());
        collaborationService.deleteComment(commentId, user);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted", null));
    }

    // ── Change Log ───────────────────────────────────────────────────────

    @GetMapping("/changelog/trip/{tripId}")
    @Operation(summary = "Get change log for a trip")
    public ResponseEntity<ApiResponse<List<ChangeLogResponse>>> getChangeLog(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails ud) {
        User user = authService.getUserByEmail(ud.getUsername());
        return ResponseEntity.ok(ApiResponse.success(collaborationService.getChangeLog(tripId, user)));
    }

    // ── Role ─────────────────────────────────────────────────────────────

    @GetMapping("/role/trip/{tripId}")
    @Operation(summary = "Get current user's role for a trip")
    public ResponseEntity<ApiResponse<Map<String, String>>> getUserRole(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails ud) {
        User user = authService.getUserByEmail(ud.getUsername());
        String role = collaborationService.getUserRole(tripId, user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("role", role)));
    }
}
