package com.voyagecraft.controller;

import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.dto.quickaction.*;
import com.voyagecraft.dto.quickaction.QuickActionDashboard.ReorderRequest;
import com.voyagecraft.entity.User;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.QuickActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quick-actions")
@RequiredArgsConstructor
@Tag(name = "Quick Actions", description = "Mobile quick actions (US-15)")
public class QuickActionController {

    private final QuickActionService quickActionService;
    private final AuthService authService;

    // ── Dashboard ──

    @GetMapping("/dashboard/{tripId}")
    @Operation(summary = "Get quick action dashboard for a trip")
    public ResponseEntity<ApiResponse<QuickActionDashboard>> getDashboard(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(quickActionService.getDashboard(tripId, user)));
    }

    // ── Check-In Shortcuts ──

    @PostMapping("/check-in/transport/{id}")
    @Operation(summary = "Toggle flight/transport check-in status")
    public ResponseEntity<ApiResponse<Void>> toggleTransportCheckIn(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        quickActionService.toggleTransportCheckIn(id, user);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/check-in/stay/{id}")
    @Operation(summary = "Toggle stay check-in status")
    public ResponseEntity<ApiResponse<Void>> toggleStayCheckIn(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        quickActionService.toggleStayCheckIn(id, user);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── Reorder ──

    @PostMapping("/reorder")
    @Operation(summary = "Reorder itinerary items for a day")
    public ResponseEntity<ApiResponse<Void>> reorderDayItems(
            @RequestBody ReorderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        quickActionService.reorderDayItems(request.getDayId(), request.getItemIds(), user);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── Quick Notes ──

    @GetMapping("/notes/{tripId}")
    @Operation(summary = "Get all quick notes for a trip")
    public ResponseEntity<ApiResponse<List<QuickNoteResponse>>> getNotes(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(quickActionService.getNotes(tripId, user)));
    }

    @PostMapping("/notes")
    @Operation(summary = "Create a quick note")
    public ResponseEntity<ApiResponse<QuickNoteResponse>> createNote(
            @Valid @RequestBody QuickNoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(quickActionService.createNote(request, user)));
    }

    @PutMapping("/notes/{id}")
    @Operation(summary = "Update a quick note")
    public ResponseEntity<ApiResponse<QuickNoteResponse>> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody QuickNoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(quickActionService.updateNote(id, request, user)));
    }

    @DeleteMapping("/notes/{id}")
    @Operation(summary = "Delete a quick note")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        quickActionService.deleteNote(id, user);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── Push Notification Token ──

    @PostMapping("/push-token")
    @Operation(summary = "Register push notification token")
    public ResponseEntity<ApiResponse<Void>> registerPushToken(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        quickActionService.registerPushToken(user.getId(), body.get("token"), body.getOrDefault("deviceType", "web"));
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
