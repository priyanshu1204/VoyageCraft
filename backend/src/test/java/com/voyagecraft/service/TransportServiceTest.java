package com.voyagecraft.service;

import com.voyagecraft.dto.transport.*;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransportServiceTest {

    @InjectMocks private TransportService transportService;
    @Mock private TransportDetailRepository transportRepository;
    @Mock private TripRepository tripRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;

    private User testUser;
    private Trip testTrip;
    private TransportDetail testTransport;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").currency("USD").createdBy(testUser)
                .createdAt(LocalDateTime.now()).build();
        testTransport = TransportDetail.builder().id(1L).trip(testTrip).type(TransportType.FLIGHT)
                .departureLocation("NYC").arrivalLocation("LAX")
                .departureTime(LocalDateTime.now().plusDays(5))
                .arrivalTime(LocalDateTime.now().plusDays(5).plusHours(5))
                .departureTimezone("America/New_York").arrivalTimezone("America/Los_Angeles")
                .cost(BigDecimal.valueOf(350)).bookingReference("ABC123")
                .createdAt(LocalDateTime.now()).build();
    }

    @Test
    void addTransport_success() {
        TransportRequest req = new TransportRequest();
        req.setTripId(1L); req.setType(TransportType.FLIGHT);
        req.setDepartureLocation("NYC"); req.setArrivalLocation("LAX");
        req.setDepartureTime("2026-06-01T10:00:00"); req.setArrivalTime("2026-06-01T15:00:00");

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(transportRepository.save(any())).thenReturn(testTransport);

        TransportResponse resp = transportService.addTransport(req, testUser);
        assertNotNull(resp);
        assertEquals(TransportType.FLIGHT, resp.getType());
        verify(transportRepository).save(any());
    }

    @Test
    void addTransport_tripNotFound_throwsException() {
        TransportRequest req = new TransportRequest();
        req.setTripId(99L); req.setType(TransportType.FLIGHT);
        req.setDepartureLocation("X"); req.setArrivalLocation("Y");
        req.setDepartureTime("2026-06-01T10:00:00"); req.setArrivalTime("2026-06-01T15:00:00");

        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transportService.addTransport(req, testUser));
    }

    @Test
    void updateTransport_success() {
        TransportRequest req = new TransportRequest();
        req.setTripId(1L); req.setType(TransportType.TRAIN);
        req.setDepartureLocation("Paris"); req.setArrivalLocation("Lyon");
        req.setDepartureTime("2026-06-01T10:00:00"); req.setArrivalTime("2026-06-01T13:00:00");

        when(transportRepository.findById(1L)).thenReturn(Optional.of(testTransport));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(transportRepository.save(any())).thenReturn(testTransport);

        TransportResponse resp = transportService.updateTransport(1L, req, testUser);
        assertNotNull(resp);
    }

    @Test
    void updateTransport_notFound_throwsException() {
        TransportRequest req = new TransportRequest();
        req.setDepartureTime("2026-06-01T10:00:00"); req.setArrivalTime("2026-06-01T15:00:00");
        when(transportRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transportService.updateTransport(99L, req, testUser));
    }

    @Test
    void deleteTransport_success() {
        when(transportRepository.findById(1L)).thenReturn(Optional.of(testTransport));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        transportService.deleteTransport(1L, testUser);
        verify(transportRepository).delete(testTransport);
    }

    @Test
    void deleteTransport_notFound_throwsException() {
        when(transportRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transportService.deleteTransport(99L, testUser));
    }

    @Test
    void getTransport_success() {
        when(transportRepository.findById(1L)).thenReturn(Optional.of(testTransport));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));

        TransportResponse resp = transportService.getTransport(1L, testUser);
        assertNotNull(resp);
        assertEquals("NYC", resp.getDepartureLocation());
    }

    @Test
    void getTransport_notFound_throwsException() {
        when(transportRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transportService.getTransport(99L, testUser));
    }

    @Test
    void addTransport_noAccess_throwsException() {
        User other = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        TransportRequest req = new TransportRequest();
        req.setTripId(1L); req.setType(TransportType.BUS);
        req.setDepartureLocation("A"); req.setArrivalLocation("B");
        req.setDepartureTime("2026-06-01T10:00:00"); req.setArrivalTime("2026-06-01T15:00:00");

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findByTripIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> transportService.addTransport(req, other));
    }

    @Test
    void searchMockTransport_flight() {
        List<MockTransportOption> results = transportService.searchMockTransport(
                TransportType.FLIGHT, "NYC", "LAX", "2026-06-01", "America/New_York", "America/Los_Angeles");
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void searchMockTransport_train() {
        List<MockTransportOption> results = transportService.searchMockTransport(
                TransportType.TRAIN, "Paris", "Lyon", "2026-06-01", null, null);
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void searchMockTransport_bus() {
        List<MockTransportOption> results = transportService.searchMockTransport(
                TransportType.BUS, "Delhi", "Jaipur", "2026-06-01", null, null);
        assertFalse(results.isEmpty());
    }

    @Test
    void searchMockTransport_car() {
        List<MockTransportOption> results = transportService.searchMockTransport(
                TransportType.CAR_RENTAL, "City", "City", "2026-06-01", null, null);
        assertFalse(results.isEmpty());
    }

    @Test void searchMockTransport_carRental2() {
        List<MockTransportOption> results = transportService.searchMockTransport(
                TransportType.CAR_RENTAL, "Dover", "Calais", "2026-06-01", null, null);
        assertNotNull(results);
    }

    @Test void getTripTransports_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(transportRepository.findByTripIdOrderByDepartureTimeAsc(1L)).thenReturn(List.of(testTransport));
        List<TransportResponse> r = transportService.getTripTransports(1L, testUser);
        assertEquals(1, r.size());
    }

    @Test void getTripTransports_empty() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(transportRepository.findByTripIdOrderByDepartureTimeAsc(1L)).thenReturn(Collections.emptyList());
        assertTrue(transportService.getTripTransports(1L, testUser).isEmpty());
    }

    @Test void detectConflicts_noConflicts() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(transportRepository.findByTripIdOrderByDepartureTimeAsc(1L)).thenReturn(List.of(testTransport));
        var conflicts = transportService.detectConflicts(1L, testUser);
        assertNotNull(conflicts);
    }

    @Test void detectConflicts_overlapping() {
        TransportDetail td2 = TransportDetail.builder().id(2L).trip(testTrip).type(TransportType.TRAIN)
                .departureLocation("LAX").arrivalLocation("SF")
                .departureTime(testTransport.getDepartureTime().plusHours(1))
                .arrivalTime(testTransport.getArrivalTime().plusHours(1))
                .departureTimezone("UTC").arrivalTimezone("UTC")
                .createdAt(LocalDateTime.now()).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(transportRepository.findByTripIdOrderByDepartureTimeAsc(1L)).thenReturn(List.of(testTransport, td2));
        var conflicts = transportService.detectConflicts(1L, testUser);
        assertNotNull(conflicts);
    }

    @Test void addTransport_allFields() {
        TransportRequest req = new TransportRequest();
        req.setTripId(1L); req.setType(TransportType.TRAIN);
        req.setDepartureLocation("Paris"); req.setArrivalLocation("Lyon");
        req.setDepartureTime("2026-06-01T10:00:00"); req.setArrivalTime("2026-06-01T13:00:00");
        req.setDepartureTimezone("Europe/Paris"); req.setArrivalTimezone("Europe/Paris");
        req.setProvider("SNCF"); req.setFlightNumber("TGV123");
        req.setBookingReference("REF456"); req.setCost(BigDecimal.valueOf(100));
        req.setNotes("Window seat");
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(transportRepository.save(any())).thenReturn(testTransport);
        assertNotNull(transportService.addTransport(req, testUser));
    }
}
