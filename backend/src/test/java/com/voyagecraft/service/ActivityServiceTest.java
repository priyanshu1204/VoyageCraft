package com.voyagecraft.service;

import com.voyagecraft.dto.activity.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.*;
import com.voyagecraft.exception.*;
import com.voyagecraft.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @InjectMocks private ActivityService service;
    @Mock private ActivityRepository activityRepository;
    @Mock private TripRepository tripRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;

    private User testUser;
    private Trip testTrip;
    private Activity testActivity;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").createdBy(testUser).createdAt(LocalDateTime.now()).build();
        testActivity = Activity.builder().id(1L).trip(testTrip).name("City Tour")
                .category(ActivityCategory.SIGHTSEEING).cost(BigDecimal.valueOf(50))
                .currency("USD").tags("sightseeing,outdoor").alternatives("Museum,Park")
                .priority(1).reservationStatus(ReservationStatus.PENDING)
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    void addActivity_success() {
        ActivityRequest req = new ActivityRequest();
        req.setTripId(1L); req.setName("City Tour"); req.setCategory(ActivityCategory.SIGHTSEEING);
        req.setCost(BigDecimal.valueOf(50));

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(activityRepository.save(any())).thenReturn(testActivity);

        ActivityResponse resp = service.addActivity(req, testUser);
        assertNotNull(resp);
        assertEquals("City Tour", resp.getName());
    }

    @Test
    void addActivity_tripNotFound_throwsException() {
        ActivityRequest req = new ActivityRequest();
        req.setTripId(99L); req.setName("X");
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.addActivity(req, testUser));
    }

    @Test
    void addActivity_noAccess_throwsException() {
        User other = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        ActivityRequest req = new ActivityRequest();
        req.setTripId(1L); req.setName("X");
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findByTripIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class, () -> service.addActivity(req, other));
    }

    @Test
    void updateActivity_success() {
        ActivityRequest req = new ActivityRequest();
        req.setName("Updated Tour"); req.setCategory(ActivityCategory.CULTURAL);
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(activityRepository.save(any())).thenReturn(testActivity);
        ActivityResponse resp = service.updateActivity(1L, req, testUser);
        assertNotNull(resp);
    }

    @Test
    void updateActivity_notFound_throwsException() {
        ActivityRequest req = new ActivityRequest();
        when(activityRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.updateActivity(99L, req, testUser));
    }

    @Test
    void deleteActivity_success() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(testActivity));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        service.deleteActivity(1L, testUser);
        verify(activityRepository).delete(testActivity);
    }

    @Test
    void deleteActivity_notFound_throwsException() {
        when(activityRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteActivity(99L, testUser));
    }

    @Test
    void getTripActivities_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(activityRepository.findByTripIdOrderByActivityDateAscStartTimeAsc(1L)).thenReturn(List.of(testActivity));
        List<ActivityResponse> result = service.getTripActivities(1L, testUser);
        assertEquals(1, result.size());
    }

    @Test
    void getByCategory_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(activityRepository.findByTripIdAndCategory(1L, ActivityCategory.SIGHTSEEING)).thenReturn(List.of(testActivity));
        List<ActivityResponse> result = service.getByCategory(1L, ActivityCategory.SIGHTSEEING, testUser);
        assertEquals(1, result.size());
    }

    @Test
    void getByTag_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(activityRepository.findByTripIdAndTag(1L, "outdoor")).thenReturn(List.of(testActivity));
        List<ActivityResponse> result = service.getByTag(1L, "outdoor", testUser);
        assertEquals(1, result.size());
    }

    @Test
    void getWaitlisted_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(activityRepository.findByTripIdAndReservationStatus(1L, ReservationStatus.WAITLISTED)).thenReturn(Collections.emptyList());
        List<ActivityResponse> result = service.getWaitlisted(1L, testUser);
        assertTrue(result.isEmpty());
    }

    @Test
    void getReminders_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(activityRepository.findByTripIdOrderByActivityDateAscStartTimeAsc(1L)).thenReturn(List.of(testActivity));
        List<ActivityResponse> result = service.getReminders(1L, testUser);
        assertNotNull(result);
    }

    @Test
    void searchCatalog_adventure() {
        List<ActivityResponse> result = service.searchCatalog("Paris", ActivityCategory.ADVENTURE);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void searchCatalog_family() {
        List<ActivityResponse> result = service.searchCatalog("Paris", ActivityCategory.FAMILY);
        assertFalse(result.isEmpty());
    }

    @Test
    void searchCatalog_cultural() {
        List<ActivityResponse> result = service.searchCatalog("Tokyo", ActivityCategory.CULTURAL);
        assertFalse(result.isEmpty());
    }

    @Test
    void searchCatalog_food() {
        List<ActivityResponse> result = service.searchCatalog("Rome", ActivityCategory.FOOD);
        assertFalse(result.isEmpty());
    }

    @Test
    void searchCatalog_default() {
        List<ActivityResponse> result = service.searchCatalog("NYC", ActivityCategory.SIGHTSEEING);
        assertFalse(result.isEmpty());
    }

    @Test
    void searchCatalog_nullParams() {
        List<ActivityResponse> result = service.searchCatalog(null, null);
        assertFalse(result.isEmpty());
    }
}
