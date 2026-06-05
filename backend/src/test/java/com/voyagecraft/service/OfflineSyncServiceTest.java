package com.voyagecraft.service;

import com.voyagecraft.dto.offline.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.*;
import com.voyagecraft.exception.*;
import com.voyagecraft.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OfflineSyncServiceTest {

    @InjectMocks private OfflineSyncService service;
    @Mock private OfflineCacheRepository cacheRepository;
    @Mock private SyncLogRepository syncLogRepository;
    @Mock private TripRepository tripRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;

    private User testUser;
    private Trip testTrip;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").createdBy(testUser)
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .currency("USD").createdAt(LocalDateTime.now()).build();
        testTrip.setDestinations(new ArrayList<>());
        testTrip.setCollaborators(new ArrayList<>());
    }

    @Test
    void generateSnapshot_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(cacheRepository.save(any())).thenReturn(OfflineCache.builder().id(1L).build());
        when(syncLogRepository.save(any())).thenReturn(SyncLog.builder().id(1L).build());

        SyncResponse resp = service.generateSnapshot(1L, testUser, false);
        assertNotNull(resp);
    }

    @Test
    void generateSnapshot_tripNotFound_throwsException() {
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.generateSnapshot(99L, testUser, false));
    }

    @Test
    void processSync_success() {
        SyncRequest req = SyncRequest.builder().tripId(1L).clientVersion(1L).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(syncLogRepository.save(any())).thenReturn(SyncLog.builder().id(1L).build());

        SyncResponse resp = service.processSync(req, testUser);
        assertNotNull(resp);
    }

    @Test
    void getPendingConflicts_success() {
        when(syncLogRepository.countByUserIdAndSyncStatus(1L, SyncStatus.CONFLICT)).thenReturn(3L);
        long count = service.getPendingConflicts(testUser);
        assertEquals(3, count);
    }

    @Test
    void resolveConflict_success() {
        SyncLog log = SyncLog.builder().id(1L).trip(testTrip).user(testUser)
                .syncStatus(SyncStatus.CONFLICT).createdAt(LocalDateTime.now()).build();
        when(syncLogRepository.findById(1L)).thenReturn(Optional.of(log));
        when(syncLogRepository.save(any())).thenReturn(log);

        SyncLog result = service.resolveConflict(1L, "KEEP_SERVER", testUser);
        assertNotNull(result);
    }

    @Test
    void resolveConflict_notFound_throwsException() {
        when(syncLogRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.resolveConflict(99L, "X", testUser));
    }

    @Test
    void removeOfflineCache_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        service.removeOfflineCache(1L, testUser);
        verify(cacheRepository).deleteByTripIdAndUserId(1L, 1L);
    }

    @Test void generateSnapshot_reducedMode() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(cacheRepository.save(any())).thenReturn(OfflineCache.builder().id(1L).build());
        when(syncLogRepository.save(any())).thenReturn(SyncLog.builder().id(1L).build());
        SyncResponse resp = service.generateSnapshot(1L, testUser, true);
        assertNotNull(resp);
    }

    @Test void generateSnapshot_noAccess() {
        User other = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findByTripIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.generateSnapshot(1L, other, false));
    }

    @Test void getUserSnapshots_success() {
        when(cacheRepository.findByUserIdOrderByLastSyncedAtDesc(1L)).thenReturn(Collections.emptyList());
        List<OfflineTripSnapshot> result = service.getUserSnapshots(testUser);
        assertNotNull(result);
    }

    @Test void getSyncHistory_success() {
        SyncLog log = SyncLog.builder().id(1L).trip(testTrip).user(testUser)
                .syncStatus(SyncStatus.SYNCED).createdAt(LocalDateTime.now()).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(syncLogRepository.findByUserIdAndTripIdOrderByCreatedAtDesc(1L, 1L)).thenReturn(List.of(log));
        List<SyncLog> result = service.getSyncHistory(1L, testUser);
        assertEquals(1, result.size());
    }

    @Test void processSync_noAccess() {
        User other = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        SyncRequest req = SyncRequest.builder().tripId(1L).clientVersion(1L).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findByTripIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.processSync(req, other));
    }

    @Test void resolveConflict_keepClient() {
        SyncLog log = SyncLog.builder().id(1L).trip(testTrip).user(testUser)
                .syncStatus(SyncStatus.CONFLICT).createdAt(LocalDateTime.now()).build();
        when(syncLogRepository.findById(1L)).thenReturn(Optional.of(log));
        when(syncLogRepository.save(any())).thenReturn(log);
        SyncLog result = service.resolveConflict(1L, "KEEP_CLIENT", testUser);
        assertNotNull(result);
    }
}
