package com.voyagecraft.service;

import com.voyagecraft.dto.navigation.*;
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
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NavigationServiceTest {
    @InjectMocks private NavigationService service;
    @Mock private NavigationRouteRepository routeRepository;
    @Mock private TripRepository tripRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;
    private User testUser; private Trip testTrip; private NavigationRoute testRoute;
    @BeforeEach void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").createdBy(testUser)
                .startDate(java.time.LocalDate.now()).endDate(java.time.LocalDate.now().plusDays(5))
                .createdAt(LocalDateTime.now()).build();
        testRoute = NavigationRoute.builder().id(1L).trip(testTrip)
                .fromLocation("Hotel").toLocation("Museum")
                .fromLatitude(48.86).fromLongitude(2.35).toLatitude(48.87).toLongitude(2.36)
                .transportMode(TransportMode.WALKING).estimatedMinutes(15).distanceKm(1.2)
                .bufferMinutes(5).dayNumber(1).orderIndex(0).createdAt(LocalDateTime.now()).build();
    }
    @Test void addRoute_ok() {
        var req = NavigationRouteRequest.builder().fromLocation("Hotel").toLocation("Museum")
                .fromLatitude(48.86).fromLongitude(2.35).toLatitude(48.87).toLongitude(2.36)
                .transportMode("WALKING").dayNumber(1).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(routeRepository.save(any())).thenReturn(testRoute);
        var resp = service.addRoute(1L, req, testUser);
        assertNotNull(resp); assertEquals("Hotel", resp.getFromLocation());
    }
    @Test void addRoute_driving() {
        var req = NavigationRouteRequest.builder().fromLocation("A").toLocation("B")
                .fromLatitude(40.0).fromLongitude(-74.0).toLatitude(41.0).toLongitude(-73.0)
                .transportMode("DRIVING").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(routeRepository.save(any())).thenReturn(testRoute);
        assertNotNull(service.addRoute(1L, req, testUser));
    }
    @Test void addRoute_publicTransport() {
        var req = NavigationRouteRequest.builder().fromLocation("A").toLocation("B")
                .fromLatitude(48.0).fromLongitude(2.0).toLatitude(48.1).toLongitude(2.1)
                .transportMode("PUBLIC_TRANSPORT").bufferMinutes(10).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(routeRepository.save(any())).thenReturn(testRoute);
        assertNotNull(service.addRoute(1L, req, testUser));
    }
    @Test void addRoute_cycling() {
        var req = NavigationRouteRequest.builder().fromLocation("A").toLocation("B")
                .fromLatitude(48.0).fromLongitude(2.0).toLatitude(48.01).toLongitude(2.01)
                .transportMode("CYCLING").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(routeRepository.save(any())).thenReturn(testRoute);
        assertNotNull(service.addRoute(1L, req, testUser));
    }
    @Test void addRoute_noAccess() {
        User o = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        var req = NavigationRouteRequest.builder().fromLocation("A").toLocation("B").transportMode("WALKING").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findByTripIdAndUserId(1L,2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.addRoute(1L, req, o));
    }
    @Test void updateRoute_ok() {
        var req = NavigationRouteRequest.builder().fromLocation("A").toLocation("B")
                .fromLatitude(48.0).fromLongitude(2.0).toLatitude(48.1).toLongitude(2.1)
                .transportMode("TAXI").build();
        when(routeRepository.findById(1L)).thenReturn(Optional.of(testRoute));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(routeRepository.save(any())).thenReturn(testRoute);
        assertNotNull(service.updateRoute(1L, req, testUser));
    }
    @Test void updateRoute_notFound() {
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.updateRoute(99L, NavigationRouteRequest.builder().build(), testUser));
    }
    @Test void switchTransportMode_ok() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(testRoute));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(routeRepository.save(any())).thenReturn(testRoute);
        assertNotNull(service.switchTransportMode(1L, "DRIVING", testUser));
    }
    @Test void switchTransportMode_taxi() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(testRoute));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(routeRepository.save(any())).thenReturn(testRoute);
        assertNotNull(service.switchTransportMode(1L, "TAXI", testUser));
    }
    @Test void switchTransportMode_rideShare() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(testRoute));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(routeRepository.save(any())).thenReturn(testRoute);
        assertNotNull(service.switchTransportMode(1L, "RIDE_SHARE", testUser));
    }
    @Test void getTripRoutes_ok() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(routeRepository.findByTripIdOrderByDayNumberAscOrderIndexAsc(1L)).thenReturn(List.of(testRoute));
        assertEquals(1, service.getTripRoutes(1L, testUser).size());
    }
    @Test void getDayRoutes_ok() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(routeRepository.findByTripIdAndDayNumberOrderByOrderIndexAsc(1L,1)).thenReturn(List.of(testRoute));
        assertEquals(1, service.getDayRoutes(1L, 1, testUser).size());
    }
    @Test void deleteRoute_ok() {
        when(routeRepository.findById(1L)).thenReturn(Optional.of(testRoute));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        service.deleteRoute(1L, testUser); verify(routeRepository).delete(testRoute);
    }
    @Test void deleteRoute_notFound() {
        when(routeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.deleteRoute(99L, testUser));
    }
    @Test void getDaySheets_ok() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(routeRepository.findByTripIdOrderByDayNumberAscOrderIndexAsc(1L)).thenReturn(List.of(testRoute));
        var sheets = service.getDaySheets(1L, testUser);
        assertNotNull(sheets);
    }
}
