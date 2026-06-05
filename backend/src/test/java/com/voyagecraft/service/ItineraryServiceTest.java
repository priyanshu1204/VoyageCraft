package com.voyagecraft.service;

import com.voyagecraft.dto.itinerary.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.*;
import com.voyagecraft.exception.*;
import com.voyagecraft.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ItineraryServiceTest {

    @InjectMocks private ItineraryService service;
    @Mock private ItineraryRepository itineraryRepository;
    @Mock private TripRepository tripRepository;

    private User testUser;
    private Trip testTrip;
    private Itinerary testItinerary;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").createdBy(testUser)
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(8))
                .pace(TripPace.STANDARD).createdAt(LocalDateTime.now()).build();
        testTrip.setDestinations(new ArrayList<>());

        testItinerary = Itinerary.builder().id(1L).trip(testTrip).versionName("V1")
                .versionNumber(1).status(ItineraryStatus.DRAFT).pace(TripPace.STANDARD)
                .generatedBy(testUser).isActive(false).notes("Test").createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
        testItinerary.setDays(new ArrayList<>());
    }

    @Test
    void generateItinerary_success() {
        GenerateItineraryRequest req = new GenerateItineraryRequest();
        req.setTripId(1L); req.setPace(TripPace.STANDARD);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(itineraryRepository.findMaxVersionNumberByTripId(1L)).thenReturn(Optional.empty());
        when(itineraryRepository.save(any())).thenReturn(testItinerary);

        ItineraryResponse resp = service.generateItinerary(req, testUser);
        assertNotNull(resp);
    }

    @Test
    void generateItinerary_relaxedPace() {
        GenerateItineraryRequest req = new GenerateItineraryRequest();
        req.setTripId(1L); req.setPace(TripPace.RELAXED); req.setVersionName("Relaxed V");

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(itineraryRepository.findMaxVersionNumberByTripId(1L)).thenReturn(Optional.of(2));
        when(itineraryRepository.save(any())).thenReturn(testItinerary);

        ItineraryResponse resp = service.generateItinerary(req, testUser);
        assertNotNull(resp);
    }

    @Test
    void generateItinerary_intensePace() {
        GenerateItineraryRequest req = new GenerateItineraryRequest();
        req.setTripId(1L); req.setPace(TripPace.INTENSE);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(itineraryRepository.findMaxVersionNumberByTripId(1L)).thenReturn(Optional.of(1));
        when(itineraryRepository.save(any())).thenReturn(testItinerary);

        ItineraryResponse resp = service.generateItinerary(req, testUser);
        assertNotNull(resp);
    }

    @Test
    void generateItinerary_tripNotFound_throwsException() {
        GenerateItineraryRequest req = new GenerateItineraryRequest();
        req.setTripId(99L); req.setPace(TripPace.STANDARD);
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.generateItinerary(req, testUser));
    }

    @Test
    void generateItinerary_notOwner_throwsException() {
        User other = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        GenerateItineraryRequest req = new GenerateItineraryRequest();
        req.setTripId(1L); req.setPace(TripPace.STANDARD);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        assertThrows(UnauthorizedException.class, () -> service.generateItinerary(req, other));
    }

    @Test
    void getItinerary_success() {
        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(testItinerary));
        ItineraryResponse resp = service.getItinerary(1L, testUser);
        assertNotNull(resp);
    }

    @Test
    void getItinerary_notFound_throwsException() {
        when(itineraryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getItinerary(99L, testUser));
    }

    @Test
    void getItinerary_noAccess_throwsException() {
        User other = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(testItinerary));
        assertThrows(UnauthorizedException.class, () -> service.getItinerary(1L, other));
    }

    @Test
    void activateItinerary_success() {
        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(testItinerary));
        when(itineraryRepository.findByTripIdOrderByVersionNumberAsc(1L)).thenReturn(List.of(testItinerary));
        when(itineraryRepository.save(any())).thenReturn(testItinerary);

        ItineraryResponse resp = service.activateItinerary(1L, testUser);
        assertNotNull(resp);
    }

    @Test
    void activateItinerary_notOwner_throwsException() {
        User other = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(testItinerary));
        assertThrows(UnauthorizedException.class, () -> service.activateItinerary(1L, other));
    }

    @Test
    void deleteItinerary_success() {
        testItinerary.setIsActive(false);
        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(testItinerary));
        service.deleteItinerary(1L, testUser);
        verify(itineraryRepository).delete(testItinerary);
    }

    @Test
    void deleteItinerary_activeItinerary_throwsException() {
        testItinerary.setIsActive(true);
        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(testItinerary));
        assertThrows(BadRequestException.class, () -> service.deleteItinerary(1L, testUser));
    }

    @Test
    void deleteItinerary_notOwner_throwsException() {
        User other = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(testItinerary));
        assertThrows(UnauthorizedException.class, () -> service.deleteItinerary(1L, other));
    }

    @Test
    void compareVersions_success() {
        Itinerary itB = Itinerary.builder().id(2L).trip(testTrip).versionName("V2")
                .versionNumber(2).status(ItineraryStatus.DRAFT).pace(TripPace.RELAXED)
                .isActive(false).createdAt(LocalDateTime.now()).build();
        itB.setDays(new ArrayList<>());

        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(testItinerary));
        when(itineraryRepository.findById(2L)).thenReturn(Optional.of(itB));

        ItineraryCompareResponse resp = service.compareVersions(1L, 2L, testUser);
        assertNotNull(resp);
    }

    @Test
    void compareVersions_differentTrips_throwsException() {
        Trip otherTrip = Trip.builder().id(2L).title("Other").createdBy(testUser).build();
        otherTrip.setDestinations(new ArrayList<>());
        Itinerary itB = Itinerary.builder().id(2L).trip(otherTrip).versionName("V2")
                .versionNumber(1).pace(TripPace.STANDARD).isActive(false)
                .createdAt(LocalDateTime.now()).build();
        itB.setDays(new ArrayList<>());

        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(testItinerary));
        when(itineraryRepository.findById(2L)).thenReturn(Optional.of(itB));

        assertThrows(BadRequestException.class, () -> service.compareVersions(1L, 2L, testUser));
    }

    @Test void getVersionHistory_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(itineraryRepository.findByTripIdOrderByVersionNumberAsc(1L)).thenReturn(List.of(testItinerary));
        var history = service.getVersionHistory(1L, testUser);
        assertNotNull(history); assertEquals(1, history.size());
    }

    @Test void getVersionHistory_empty() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(itineraryRepository.findByTripIdOrderByVersionNumberAsc(1L)).thenReturn(Collections.emptyList());
        assertTrue(service.getVersionHistory(1L, testUser).isEmpty());
    }

    @Test void generateItinerary_withDestinations() {
        TripDestination d = TripDestination.builder().id(1L).destinationName("Paris").country("France").build();
        testTrip.setDestinations(List.of(d));
        GenerateItineraryRequest req = new GenerateItineraryRequest();
        req.setTripId(1L); req.setPace(TripPace.STANDARD);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(itineraryRepository.findMaxVersionNumberByTripId(1L)).thenReturn(Optional.empty());
        when(itineraryRepository.save(any())).thenReturn(testItinerary);
        assertNotNull(service.generateItinerary(req, testUser));
    }

    @Test void generateItinerary_relaxedWithDest() {
        TripDestination d = TripDestination.builder().id(1L).destinationName("Tokyo").build();
        testTrip.setDestinations(List.of(d));
        GenerateItineraryRequest req = new GenerateItineraryRequest();
        req.setTripId(1L); req.setPace(TripPace.RELAXED);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(itineraryRepository.findMaxVersionNumberByTripId(1L)).thenReturn(Optional.of(1));
        when(itineraryRepository.save(any())).thenReturn(testItinerary);
        assertNotNull(service.generateItinerary(req, testUser));
    }

    @Test void generateItinerary_intenseWithDest() {
        TripDestination d = TripDestination.builder().id(1L).destinationName("NYC").build();
        testTrip.setDestinations(List.of(d));
        GenerateItineraryRequest req = new GenerateItineraryRequest();
        req.setTripId(1L); req.setPace(TripPace.INTENSE);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(itineraryRepository.findMaxVersionNumberByTripId(1L)).thenReturn(Optional.of(2));
        when(itineraryRepository.save(any())).thenReturn(testItinerary);
        assertNotNull(service.generateItinerary(req, testUser));
    }

    @Test void activateItinerary_withMultipleVersions() {
        Itinerary it2 = Itinerary.builder().id(2L).trip(testTrip).versionName("V2")
                .versionNumber(2).isActive(true).createdAt(LocalDateTime.now()).build();
        it2.setDays(new ArrayList<>());
        when(itineraryRepository.findById(1L)).thenReturn(Optional.of(testItinerary));
        when(itineraryRepository.findByTripIdOrderByVersionNumberAsc(1L)).thenReturn(List.of(testItinerary, it2));
        when(itineraryRepository.save(any())).thenReturn(testItinerary);
        assertNotNull(service.activateItinerary(1L, testUser));
    }
}
