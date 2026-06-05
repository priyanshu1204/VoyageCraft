package com.voyagecraft.controller;

import com.voyagecraft.dto.activity.ActivityRequest;
import com.voyagecraft.dto.activity.ActivityResponse;
import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.entity.User;
import com.voyagecraft.enums.ActivityCategory;
import com.voyagecraft.service.ActivityService;
import com.voyagecraft.service.AuthService;
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

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Tag(name = "Activities", description = "Activity discovery and reservation management (US-05)")
public class ActivityController {

    private final ActivityService activityService;
    private final AuthService authService;

    @PostMapping
    @Operation(summary = "Add an activity to a trip")
    public ResponseEntity<ApiResponse<ActivityResponse>> addActivity(
            @Valid @RequestBody ActivityRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        ActivityResponse response = activityService.addActivity(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Activity added", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an activity")
    public ResponseEntity<ApiResponse<ActivityResponse>> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody ActivityRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        ActivityResponse response = activityService.updateActivity(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Activity updated", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an activity")
    public ResponseEntity<ApiResponse<Void>> deleteActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        activityService.deleteActivity(id, user);
        return ResponseEntity.ok(ApiResponse.success("Activity deleted", null));
    }

    @GetMapping("/trip/{tripId}")
    @Operation(summary = "Get all activities for a trip")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getTripActivities(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        List<ActivityResponse> list = activityService.getTripActivities(tripId, user);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/trip/{tripId}/category/{category}")
    @Operation(summary = "Filter activities by category")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getByCategory(
            @PathVariable Long tripId,
            @PathVariable ActivityCategory category,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(activityService.getByCategory(tripId, category, user)));
    }

    @GetMapping("/trip/{tripId}/tag/{tag}")
    @Operation(summary = "Filter activities by tag")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getByTag(
            @PathVariable Long tripId,
            @PathVariable String tag,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(activityService.getByTag(tripId, tag, user)));
    }

    @GetMapping("/trip/{tripId}/waitlisted")
    @Operation(summary = "Get waitlisted activities")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getWaitlisted(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(activityService.getWaitlisted(tripId, user)));
    }

    @GetMapping("/trip/{tripId}/reminders")
    @Operation(summary = "Get activities with reminders set")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getReminders(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(activityService.getReminders(tripId, user)));
    }

    @GetMapping("/catalog")
    @Operation(summary = "Search mock activity catalog by destination and category")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> searchCatalog(
            @RequestParam String destination,
            @RequestParam(required = false) ActivityCategory category) {
        List<ActivityResponse> results = activityService.searchCatalog(destination, category);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
