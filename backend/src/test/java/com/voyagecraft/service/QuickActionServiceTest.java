package com.voyagecraft.service;

import com.voyagecraft.dto.quickaction.*;
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
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QuickActionServiceTest {
    @InjectMocks private QuickActionService service;
    @Mock private TripRepository tripRepository;
    @Mock private TransportDetailRepository transportDetailRepository;
    @Mock private StayRepository stayRepository;
    @Mock private ItineraryRepository itineraryRepository;
    @Mock private ItineraryDayRepository itineraryDayRepository;
    @Mock private ActivityRepository activityRepository;
    @Mock private QuickNoteRepository quickNoteRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;
    @Mock private NotificationRepository notificationRepository;
    private User testUser; private Trip testTrip; private QuickNote testNote;
    @BeforeEach void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").createdBy(testUser)
                .startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(10)).createdAt(LocalDateTime.now()).build();
        testTrip.setDestinations(new ArrayList<>());
        testNote = QuickNote.builder().id(1L).trip(testTrip).user(testUser).title("N").content("C")
                .isSynced(true).isPinned(false).createdAt(LocalDateTime.now()).build();
    }
    private void stubDashboard() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(itineraryRepository.findByTripIdOrderByVersionNumberAsc(1L)).thenReturn(Collections.emptyList());
        when(activityRepository.findByTripIdOrderByActivityDateAscStartTimeAsc(1L)).thenReturn(Collections.emptyList());
        when(transportDetailRepository.findByTripIdOrderByDepartureTimeAsc(1L)).thenReturn(Collections.emptyList());
        when(stayRepository.findByTripIdOrderByCheckInDateAsc(1L)).thenReturn(Collections.emptyList());
        when(quickNoteRepository.findByTripIdAndUserIdOrderByCreatedAtDesc(1L,1L)).thenReturn(Collections.emptyList());
        when(notificationRepository.countByTripIdAndUserIdAndReadStatus(1L,1L,false)).thenReturn(0);
    }
    @Test void getDashboard_empty() { stubDashboard(); var r=service.getDashboard(1L,testUser); assertNotNull(r); assertEquals("Trip",r.getTripTitle()); }
    @Test void getDashboard_withTransport() {
        TransportDetail td=TransportDetail.builder().id(1L).trip(testTrip).type(TransportType.FLIGHT)
                .departureLocation("A").arrivalLocation("B").departureTime(LocalDateTime.now().plusHours(5))
                .arrivalTime(LocalDateTime.now().plusHours(10)).checkedIn(false).createdAt(LocalDateTime.now()).build();
        stubDashboard(); when(transportDetailRepository.findByTripIdOrderByDepartureTimeAsc(1L)).thenReturn(List.of(td));
        var r=service.getDashboard(1L,testUser); assertFalse(r.getUpcomingCheckIns().isEmpty());
    }
    @Test void getDashboard_withStay() {
        Stay s=Stay.builder().id(1L).trip(testTrip).name("H").type(StayType.HOTEL)
                .checkInDate(LocalDate.now().plusDays(1)).checkInTime(LocalTime.of(14,0))
                .checkOutDate(LocalDate.now().plusDays(3)).checkedIn(false).createdAt(LocalDateTime.now()).build();
        stubDashboard(); when(stayRepository.findByTripIdOrderByCheckInDateAsc(1L)).thenReturn(List.of(s));
        var r=service.getDashboard(1L,testUser); assertFalse(r.getUpcomingCheckIns().isEmpty());
    }
    @Test void getDashboard_withActivity() {
        Activity a=Activity.builder().id(1L).trip(testTrip).name("T").activityDate(LocalDate.now())
                .startTime(LocalTime.of(9,0)).endTime(LocalTime.of(12,0))
                .category(ActivityCategory.SIGHTSEEING).createdAt(LocalDateTime.now()).build();
        stubDashboard(); when(activityRepository.findByTripIdOrderByActivityDateAscStartTimeAsc(1L)).thenReturn(List.of(a));
        var r=service.getDashboard(1L,testUser); assertFalse(r.getTodaySchedule().isEmpty());
    }
    @Test void getDashboard_withNotes() {
        stubDashboard(); when(quickNoteRepository.findByTripIdAndUserIdOrderByCreatedAtDesc(1L,1L)).thenReturn(List.of(testNote));
        var r=service.getDashboard(1L,testUser); assertFalse(r.getRecentNotes().isEmpty());
    }
    @Test void getDashboard_noAccess() {
        User o=User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findByTripIdAndUserId(1L,2L)).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class,()->service.getDashboard(1L,o));
    }
    @Test void toggleTransportCheckIn_ok() {
        var td=TransportDetail.builder().id(1L).trip(testTrip).type(TransportType.FLIGHT).checkedIn(false).createdAt(LocalDateTime.now()).build();
        when(transportDetailRepository.findById(1L)).thenReturn(Optional.of(td));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(transportDetailRepository.save(any())).thenReturn(td);
        service.toggleTransportCheckIn(1L,testUser); verify(transportDetailRepository).save(any());
    }
    @Test void toggleTransportCheckIn_notFound() { when(transportDetailRepository.findById(99L)).thenReturn(Optional.empty()); assertThrows(ResourceNotFoundException.class,()->service.toggleTransportCheckIn(99L,testUser)); }
    @Test void toggleStayCheckIn_ok() {
        var s=Stay.builder().id(1L).trip(testTrip).name("H").checkedIn(false).createdAt(LocalDateTime.now()).build();
        when(stayRepository.findById(1L)).thenReturn(Optional.of(s)); when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(stayRepository.save(any())).thenReturn(s); service.toggleStayCheckIn(1L,testUser); verify(stayRepository).save(any());
    }
    @Test void toggleStayCheckIn_notFound() { when(stayRepository.findById(99L)).thenReturn(Optional.empty()); assertThrows(ResourceNotFoundException.class,()->service.toggleStayCheckIn(99L,testUser)); }
    @Test void reorderDayItems_ok() {
        var item=ItineraryItem.builder().id(1L).orderIndex(0).build();
        var itin=Itinerary.builder().id(1L).trip(testTrip).build();
        var day=ItineraryDay.builder().id(1L).itinerary(itin).items(new ArrayList<>(List.of(item))).build();
        when(itineraryDayRepository.findById(1L)).thenReturn(Optional.of(day)); when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(itineraryDayRepository.save(any())).thenReturn(day); service.reorderDayItems(1L,List.of(1L),testUser); verify(itineraryDayRepository).save(any());
    }
    @Test void reorderDayItems_notFound() { when(itineraryDayRepository.findById(99L)).thenReturn(Optional.empty()); assertThrows(ResourceNotFoundException.class,()->service.reorderDayItems(99L,List.of(),testUser)); }
    @Test void createNote_ok() {
        var req=new QuickNoteRequest(); req.setTripId(1L); req.setContent("C");
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip)); when(quickNoteRepository.save(any())).thenReturn(testNote);
        assertNotNull(service.createNote(req,testUser));
    }
    @Test void updateNote_ok() {
        var req=new QuickNoteRequest(); req.setTripId(1L); req.setContent("U");
        when(quickNoteRepository.findById(1L)).thenReturn(Optional.of(testNote)); when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(quickNoteRepository.save(any())).thenReturn(testNote); assertNotNull(service.updateNote(1L,req,testUser));
    }
    @Test void updateNote_notFound() { when(quickNoteRepository.findById(99L)).thenReturn(Optional.empty()); assertThrows(ResourceNotFoundException.class,()->service.updateNote(99L,new QuickNoteRequest(),testUser)); }
    @Test void deleteNote_ok() { when(quickNoteRepository.findById(1L)).thenReturn(Optional.of(testNote)); service.deleteNote(1L,testUser); verify(quickNoteRepository).delete(testNote); }
    @Test void deleteNote_notFound() { when(quickNoteRepository.findById(99L)).thenReturn(Optional.empty()); assertThrows(ResourceNotFoundException.class,()->service.deleteNote(99L,testUser)); }
    @Test void getNotes_ok() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(quickNoteRepository.findByTripIdAndUserIdOrderByCreatedAtDesc(1L,1L)).thenReturn(List.of(testNote));
        var r=service.getNotes(1L,testUser); assertFalse(r.isEmpty());
    }
}
