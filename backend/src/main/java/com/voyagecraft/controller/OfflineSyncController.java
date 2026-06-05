package com.voyagecraft.controller;

import com.voyagecraft.dto.common.ApiResponse;
import com.voyagecraft.dto.offline.*;
import com.voyagecraft.entity.SyncLog;
import com.voyagecraft.entity.User;
import com.voyagecraft.service.AuthService;
import com.voyagecraft.service.OfflineSyncService;
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
@RequestMapping("/api/v1/offline")
@RequiredArgsConstructor
@Tag(name = "Offline Sync", description = "Offline mode and data synchronization (US-09)")
public class OfflineSyncController {

    private final OfflineSyncService syncService;
    private final AuthService authService;

    @PostMapping("/snapshot/{tripId}")
    @Operation(summary = "Generate an offline snapshot for a trip")
    public ResponseEntity<ApiResponse<SyncResponse>> generateSnapshot(
            @PathVariable Long tripId,
            @RequestParam(defaultValue = "false") boolean reducedMode,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        SyncResponse response = syncService.generateSnapshot(tripId, user, reducedMode);
        return ResponseEntity.ok(ApiResponse.success("Snapshot generated", response));
    }

    @PostMapping("/sync")
    @Operation(summary = "Sync offline changes to the server")
    public ResponseEntity<ApiResponse<SyncResponse>> syncChanges(
            @RequestBody SyncRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        SyncResponse response = syncService.processSync(request, user);
        return ResponseEntity.ok(ApiResponse.success(response.getSyncStatusMessage(), response));
    }

    @GetMapping("/snapshots")
    @Operation(summary = "Get all offline-cached trips for the current user")
    public ResponseEntity<ApiResponse<List<OfflineTripSnapshot>>> getSnapshots(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(syncService.getUserSnapshots(user)));
    }

    @GetMapping("/history/{tripId}")
    @Operation(summary = "Get sync history for a trip")
    public ResponseEntity<ApiResponse<List<SyncLog>>> getSyncHistory(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(syncService.getSyncHistory(tripId, user)));
    }

    @GetMapping("/conflicts/count")
    @Operation(summary = "Get count of pending conflicts")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getConflictCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        long count = syncService.getPendingConflicts(user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("pendingConflicts", count)));
    }

    @PutMapping("/conflicts/{syncLogId}/resolve")
    @Operation(summary = "Resolve a sync conflict (ACCEPT_CLIENT or ACCEPT_SERVER)")
    public ResponseEntity<ApiResponse<SyncLog>> resolveConflict(
            @PathVariable Long syncLogId,
            @RequestParam String resolution,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        SyncLog resolved = syncService.resolveConflict(syncLogId, resolution, user);
        return ResponseEntity.ok(ApiResponse.success("Conflict resolved", resolved));
    }

    @DeleteMapping("/cache/{tripId}")
    @Operation(summary = "Remove offline cache for a trip")
    public ResponseEntity<ApiResponse<Void>> removeCache(
            @PathVariable Long tripId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        syncService.removeOfflineCache(tripId, user);
        return ResponseEntity.ok(ApiResponse.success("Offline cache removed", null));
    }
}
