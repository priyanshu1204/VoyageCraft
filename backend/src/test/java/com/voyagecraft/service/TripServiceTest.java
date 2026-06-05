package com.voyagecraft.service;

import com.voyagecraft.dto.trip.*;
import com.voyagecraft.dto.destination.DestinationRequest;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.*;
import com.voyagecraft.exception.*;
import com.voyagecraft.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @InjectMocks private TripService tripService;
    @Mock private TripRepository tripRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private WeatherForecastRepository weatherForecastRepository;
    @Mock private OfflineCacheRepository offlineCacheRepository;
    @Mock private SyncLogRepository syncLogRepository;

    private User testUser;
    private Trip testTrip;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("user@test.com").firstName("John").lastName("Doe").build();
        testTrip = Trip.builder().id(1L).title("Europe Trip").description("Summer vacation")
                .startDate(LocalDate.now().plusDays(10)).endDate(LocalDate.now().plusDays(20))
                .status(TripStatus.PLANNING).pace(TripPace.STANDARD).currency("USD")
                .budgetTotal(BigDecimal.valueOf(5000)).createdBy(testUser)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        testTrip.setDestinations(new ArrayList<>());
        testTrip.setCollaborators(new ArrayList<>());
    }

    @Test
    void createTrip_success() {
        TripRequest req = TripRequest.builder().title("Europe Trip").description("Summer")
                .startDate(LocalDate.now().plusDays(10)).endDate(LocalDate.now().plusDays(20))
                .currency("USD").pace(TripPace.STANDARD).build();

        when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);

        TripResponse resp = tripService.createTrip(req, testUser);

        assertNotNull(resp);
        assertEquals("Europe Trip", resp.getTitle());
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void createTrip_withDestinations_success() {
        DestinationRequest destReq = DestinationRequest.builder()
                .destinationName("Paris").country("France").orderIndex(0).build();
        TripRequest req = TripRequest.builder().title("Paris Trip")
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .destinations(List.of(destReq)).build();

        when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);

        TripResponse resp = tripService.createTrip(req, testUser);
        assertNotNull(resp);
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void createTrip_endDateBeforeStartDate_throwsException() {
        TripRequest req = TripRequest.builder().title("Bad Trip")
                .startDate(LocalDate.now().plusDays(20)).endDate(LocalDate.now().plusDays(10)).build();

        assertThrows(BadRequestException.class, () -> tripService.createTrip(req, testUser));
    }

    @Test
    void getAllUserTrips_success() {
        when(tripRepository.findAllUserTrips(1L)).thenReturn(List.of(testTrip));

        List<TripResponse> result = tripService.getAllUserTrips(testUser);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllUserTrips_empty() {
        when(tripRepository.findAllUserTrips(1L)).thenReturn(Collections.emptyList());
        List<TripResponse> result = tripService.getAllUserTrips(testUser);
        assertTrue(result.isEmpty());
    }

    @Test
    void getTripById_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        TripResponse resp = tripService.getTripById(1L, testUser);

        assertNotNull(resp);
        assertEquals(1L, resp.getId());
    }

    @Test
    void getTripById_notFound_throwsException() {
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> tripService.getTripById(99L, testUser));
    }

    @Test
    void getTripById_noAccess_throwsException() {
        User otherUser = User.builder().id(2L).email("other@test.com").firstName("Jane").lastName("Doe").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findByTripIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> tripService.getTripById(1L, otherUser));
    }

    @Test
    void updateTrip_success() {
        TripRequest req = TripRequest.builder().title("Updated Trip").description("New desc")
                .startDate(LocalDate.now().plusDays(10)).endDate(LocalDate.now().plusDays(20)).build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(tripRepository.save(any(Trip.class))).thenReturn(testTrip);

        TripResponse resp = tripService.updateTrip(1L, req, testUser);
        assertNotNull(resp);
    }

    @Test
    void updateTrip_notFound_throwsException() {
        TripRequest req = TripRequest.builder().title("X")
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(1)).build();
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> tripService.updateTrip(99L, req, testUser));
    }

    @Test
    void updateTrip_endBeforeStart_throwsException() {
        TripRequest req = TripRequest.builder().title("X")
                .startDate(LocalDate.now().plusDays(20)).endDate(LocalDate.now().plusDays(10)).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        assertThrows(BadRequestException.class, () -> tripService.updateTrip(1L, req, testUser));
    }

    @Test
    void deleteTrip_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        tripService.deleteTrip(1L, testUser);

        verify(weatherForecastRepository).deleteByTripId(1L);
        verify(offlineCacheRepository).deleteByTripId(1L);
        verify(syncLogRepository).deleteByTripId(1L);
        verify(tripRepository).delete(testTrip);
    }

    @Test
    void deleteTrip_notOwner_throwsException() {
        User otherUser = User.builder().id(2L).email("other@test.com").firstName("X").lastName("Y").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        assertThrows(UnauthorizedException.class, () -> tripService.deleteTrip(1L, otherUser));
    }

    @Test
    void deleteTrip_notFound_throwsException() {
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> tripService.deleteTrip(99L, testUser));
    }

    @Test
    void getDashboard_success() {
        when(tripRepository.findAllUserTrips(1L)).thenReturn(List.of(testTrip));

        TripDashboardResponse resp = tripService.getDashboard(testUser);

        assertNotNull(resp);
        assertEquals(1, resp.getTotalTrips());
    }

    @Test
    void getDashboard_empty() {
        when(tripRepository.findAllUserTrips(1L)).thenReturn(Collections.emptyList());

        TripDashboardResponse resp = tripService.getDashboard(testUser);

        assertEquals(0, resp.getTotalTrips());
        assertEquals(0, resp.getPlanningTrips());
        assertEquals(0, resp.getActiveTrips());
    }

    @Test
    void getDashboard_countsActiveTripsCorrectly() {
        Trip activeTrip = Trip.builder().id(2L).title("Active").status(TripStatus.ACTIVE)
                .startDate(LocalDate.now().minusDays(2)).endDate(LocalDate.now().plusDays(5))
                .createdBy(testUser).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .currency("USD").pace(TripPace.STANDARD).build();
        activeTrip.setDestinations(new ArrayList<>());
        activeTrip.setCollaborators(new ArrayList<>());

        when(tripRepository.findAllUserTrips(1L)).thenReturn(List.of(testTrip, activeTrip));

        TripDashboardResponse resp = tripService.getDashboard(testUser);
        assertTrue(resp.getActiveTrips() >= 1);
    }

    @Test
    void mapToResponse_success() {
        TripResponse resp = tripService.mapToResponse(testTrip);

        assertNotNull(resp);
        assertEquals("Europe Trip", resp.getTitle());
        assertEquals("USD", resp.getCurrency());
        assertNotNull(resp.getCreatedBy());
    }
}
