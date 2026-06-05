package com.voyagecraft.controller;

import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.dto.notification.*;
import com.voyagecraft.entity.User;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Alerts & Notifications (US-13)")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    @GetMapping
    @Operation(summary = "Get all notifications for the user")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUserNotifications(user)));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnread(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadNotifications(user)));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", notificationService.getUnreadCount(user))));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get notifications by type")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByType(
            @PathVariable String type, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(notificationService.getByType(user, type)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(ApiResponse.success("All marked as read", null));
    }

    @PatchMapping("/{id}/dismiss")
    @Operation(summary = "Dismiss a notification")
    public ResponseEntity<ApiResponse<Void>> dismiss(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        notificationService.dismiss(id, user);
        return ResponseEntity.ok(ApiResponse.success("Dismissed", null));
    }

    @PostMapping("/generate/{tripId}")
    @Operation(summary = "Generate alerts for a trip (departure, budget, collaborator)")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> generate(
            @PathVariable Long tripId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Alerts generated", notificationService.generateTripAlerts(tripId, user)));
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPrefs(@AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(notificationService.getPreferences(user)));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePrefs(
            @RequestBody NotificationPreferenceRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Preferences updated", notificationService.updatePreferences(request, user)));
    }
}
