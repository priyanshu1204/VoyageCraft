package com.voyagecraft.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voyagecraft.dto.offline.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.SyncStatus;
import com.voyagecraft.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfflineSyncService {

    private final SyncLogRepository syncLogRepository;
    private final OfflineCacheRepository cacheRepository;
    private final TripRepository tripRepository;
    private final TripCollaboratorRepository collaboratorRepository;
    private final ObjectMapper objectMapper;

    /**
     * Generate a full trip snapshot for offline caching.
     */
    @Transactional
    public SyncResponse generateSnapshot(Long tripId, User user, boolean reducedMode) {
        Trip trip = getTripWithAccessCheck(tripId, user);

        String snapshotJson = buildSnapshotJson(trip, reducedMode);

        // Save/update the offline cache record
        OfflineCache cache = cacheRepository.findByTripIdAndUserId(tripId, user.getId())
                .orElse(OfflineCache.builder()
                        .trip(trip)
                        .user(user)
                        .versionNumber(0L)
                        .build());

        cache.setVersionNumber(cache.getVersionNumber() + 1);
        cache.setSnapshotData(snapshotJson);
        cache.setLastSyncedAt(LocalDateTime.now());
        cache.setReducedMode(reducedMode);
        cacheRepository.save(cache);

        return SyncResponse.builder()
                .serverVersion(cache.getVersionNumber())
                .syncedAt(cache.getLastSyncedAt())
                .snapshotData(snapshotJson)
                .conflicts(Collections.emptyList())
                .appliedChanges(0)
                .rejectedChanges(0)
                .syncStatusMessage("Snapshot generated successfully")
                .build();
    }

    /**
     * Process offline changes submitted by the client.
     */
    @Transactional
    public SyncResponse processSync(SyncRequest request, User user) {
        Trip trip = getTripWithAccessCheck(request.getTripId(), user);

        OfflineCache cache = cacheRepository.findByTripIdAndUserId(trip.getId(), user.getId())
                .orElse(OfflineCache.builder()
                        .trip(trip)
                        .user(user)
                        .versionNumber(0L)
                        .build());

        List<SyncConflict> conflicts = new ArrayList<>();
        int applied = 0;
        int rejected = 0;

        // Check version - if client is behind server, there may be conflicts
        boolean hasVersionConflict = request.getClientVersion() != null
                && cache.getVersionNumber() != null
                && request.getClientVersion() < cache.getVersionNumber();

        if (request.getChanges() != null) {
            for (SyncChange change : request.getChanges()) {
                SyncLog syncLog = SyncLog.builder()
                        .user(user)
                        .trip(trip)
                        .entityType(change.getEntityType())
                        .entityId(change.getEntityId() != null ? change.getEntityId() : 0L)
                        .action(change.getAction())
                        .offlinePayload(change.getPayload())
                        .offlineTimestamp(change.getTimestamp())
                        .build();

                if (hasVersionConflict && "UPDATE".equals(change.getAction())) {
                    // Flag as conflict - client had stale data
                    syncLog.setSyncStatus(SyncStatus.CONFLICT);
                    syncLog.setConflictMessage("Server data was modified since your last sync. Please review.");
                    conflicts.add(SyncConflict.builder()
                            .entityType(change.getEntityType())
                            .entityId(change.getEntityId())
                            .clientData(change.getPayload())
                            .serverData(getServerEntityData(change.getEntityType(), change.getEntityId()))
                            .message("Data modified on server after your offline edit")
                            .build());
                    rejected++;
                } else {
                    // Apply the change
                    syncLog.setSyncStatus(SyncStatus.SYNCED);
                    syncLog.setSyncedAt(LocalDateTime.now());
                    applied++;
                }

                syncLogRepository.save(syncLog);
            }
        }

        // Update cache version
        cache.setVersionNumber(cache.getVersionNumber() + 1);
        cache.setLastSyncedAt(LocalDateTime.now());
        String snapshotJson = buildSnapshotJson(trip, request.getReducedMode() != null && request.getReducedMode());
        cache.setSnapshotData(snapshotJson);
        cacheRepository.save(cache);

        return SyncResponse.builder()
                .serverVersion(cache.getVersionNumber())
                .syncedAt(cache.getLastSyncedAt())
                .snapshotData(snapshotJson)
                .conflicts(conflicts)
                .appliedChanges(applied)
                .rejectedChanges(rejected)
                .syncStatusMessage(conflicts.isEmpty()
                        ? "All changes synced successfully"
                        : conflicts.size() + " conflict(s) detected. Please review.")
                .build();
    }

    /**
     * Get list of offline-cached trips for a user.
     */
    @Transactional(readOnly = true)
    public List<OfflineTripSnapshot> getUserSnapshots(User user) {
        return cacheRepository.findByUserIdOrderByLastSyncedAtDesc(user.getId())
                .stream()
                .map(c -> OfflineTripSnapshot.builder()
                        .tripId(c.getTrip().getId())
                        .tripTitle(c.getTrip().getTitle())
                        .versionNumber(c.getVersionNumber())
                        .lastSyncedAt(c.getLastSyncedAt())
                        .reducedMode(c.getReducedMode())
                        .dataSizeBytes(c.getSnapshotData() != null ? c.getSnapshotData().length() : 0)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get sync history for a specific trip.
     */
    @Transactional(readOnly = true)
    public List<SyncLog> getSyncHistory(Long tripId, User user) {
        getTripWithAccessCheck(tripId, user);
        return syncLogRepository.findByUserIdAndTripIdOrderByCreatedAtDesc(user.getId(), tripId);
    }

    /**
     * Get pending conflict count.
     */
    public long getPendingConflicts(User user) {
        return syncLogRepository.countByUserIdAndSyncStatus(user.getId(), SyncStatus.CONFLICT);
    }

    /**
     * Resolve a sync conflict (accept client or server version).
     */
    @Transactional
    public SyncLog resolveConflict(Long syncLogId, String resolution, User user) {
        SyncLog syncLog = syncLogRepository.findById(syncLogId)
                .orElseThrow(() -> new RuntimeException("Sync log not found"));

        if (!syncLog.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized to resolve this conflict");
        }

        if ("ACCEPT_CLIENT".equals(resolution)) {
            syncLog.setSyncStatus(SyncStatus.SYNCED);
            syncLog.setSyncedAt(LocalDateTime.now());
            syncLog.setConflictMessage("Resolved: Client version accepted");
        } else if ("ACCEPT_SERVER".equals(resolution)) {
            syncLog.setSyncStatus(SyncStatus.SYNCED);
            syncLog.setSyncedAt(LocalDateTime.now());
            syncLog.setConflictMessage("Resolved: Server version kept");
        }

        return syncLogRepository.save(syncLog);
    }

    /**
     * Remove offline cache for a trip.
     */
    @Transactional
    public void removeOfflineCache(Long tripId, User user) {
        cacheRepository.deleteByTripIdAndUserId(tripId, user.getId());
    }

    // ── Private helpers ────────────────────────────────────────────────

    private Trip getTripWithAccessCheck(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        boolean isOwner = trip.getCreatedBy().getId().equals(user.getId());
        boolean isCollaborator = collaboratorRepository.findByTripIdAndUserId(trip.getId(), user.getId()).isPresent();

        if (!isOwner && !isCollaborator) {
            throw new RuntimeException("Not authorized to access this trip");
        }
        return trip;
    }

    private String buildSnapshotJson(Trip trip, boolean reducedMode) {
        try {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("tripId", trip.getId());
            snapshot.put("title", trip.getTitle());
            snapshot.put("startDate", trip.getStartDate().toString());
            snapshot.put("endDate", trip.getEndDate().toString());
            snapshot.put("status", trip.getStatus().name());
            snapshot.put("destinations", trip.getDestinations().stream()
                    .map(d -> Map.of("id", d.getId(), "name", d.getDestinationName()))
                    .collect(Collectors.toList()));

            if (!reducedMode) {
                // Full mode: include all details
                snapshot.put("itineraries", trip.getItineraries().size());
                snapshot.put("transports", trip.getTransports().size());
                snapshot.put("stays", trip.getStays().size());
                snapshot.put("activities", trip.getActivities().size());
                snapshot.put("packingItems", trip.getPackingItems().size());
            }

            snapshot.put("generatedAt", LocalDateTime.now().toString());
            snapshot.put("reducedMode", reducedMode);

            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            log.error("Failed to build snapshot JSON", e);
            return "{}";
        }
    }

    private String getServerEntityData(String entityType, Long entityId) {
        // Returns current server state for conflict comparison
        return "{\"entityType\":\"" + entityType + "\",\"entityId\":" + entityId + ",\"note\":\"Current server version\"}";
    }
}
