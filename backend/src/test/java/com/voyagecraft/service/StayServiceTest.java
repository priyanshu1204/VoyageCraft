package com.voyagecraft.service;

import com.voyagecraft.dto.stay.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StayServiceTest {

    @InjectMocks private StayService stayService;
    @Mock private StayRepository stayRepository;
    @Mock private TripRepository tripRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;

    private User testUser;
    private Trip testTrip;
    private Stay testStay;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").currency("USD").createdBy(testUser)
                .createdAt(LocalDateTime.now()).build();
        testStay = Stay.builder().id(1L).trip(testTrip).name("Hilton").city("Paris")
                .type(StayType.HOTEL).checkInDate(LocalDate.now().plusDays(5))
                .checkOutDate(LocalDate.now().plusDays(8)).currency("USD")
                .costPerNight(BigDecimal.valueOf(150)).totalCost(BigDecimal.valueOf(450))
                .starRating(4).createdAt(LocalDateTime.now()).build();
    }

    @Test
    void addStay_success() {
        StayRequest req = new StayRequest();
        req.setTripId(1L); req.setName("Hilton"); req.setType(StayType.HOTEL); req.setCity("Paris");
        req.setCheckInDate(LocalDate.now().plusDays(5).toString());
        req.setCheckOutDate(LocalDate.now().plusDays(8).toString());
        req.setCurrency("USD"); req.setCostPerNight(BigDecimal.valueOf(150));

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(stayRepository.save(any(Stay.class))).thenReturn(testStay);

        StayResponse resp = stayService.addStay(req, testUser);
        assertNotNull(resp);
        assertEquals("Hilton", resp.getName());
    }

    @Test
    void addStay_checkOutBeforeCheckIn_throwsException() {
        StayRequest req = new StayRequest();
        req.setTripId(1L); req.setName("Hotel"); req.setType(StayType.HOTEL);
        req.setCheckInDate(LocalDate.now().plusDays(10).toString());
        req.setCheckOutDate(LocalDate.now().plusDays(5).toString());

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        assertThrows(BadRequestException.class, () -> stayService.addStay(req, testUser));
    }

    @Test
    void updateStay_success() {
        StayRequest req = new StayRequest();
        req.setTripId(1L); req.setName("Updated Hilton"); req.setType(StayType.RESORT); req.setCity("Paris");
        req.setCheckInDate(LocalDate.now().plusDays(5).toString());
        req.setCheckOutDate(LocalDate.now().plusDays(9).toString());

        when(stayRepository.findById(1L)).thenReturn(Optional.of(testStay));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(stayRepository.save(any(Stay.class))).thenReturn(testStay);

        StayResponse resp = stayService.updateStay(1L, req, testUser);
        assertNotNull(resp);
    }

    @Test
    void updateStay_notFound_throwsException() {
        StayRequest req = new StayRequest();
        req.setCheckInDate(LocalDate.now().toString());
        req.setCheckOutDate(LocalDate.now().plusDays(1).toString());
        when(stayRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> stayService.updateStay(99L, req, testUser));
    }

    @Test
    void deleteStay_success() {
        when(stayRepository.findById(1L)).thenReturn(Optional.of(testStay));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        stayService.deleteStay(1L, testUser);
        verify(stayRepository).delete(testStay);
    }

    @Test
    void deleteStay_notFound_throwsException() {
        when(stayRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> stayService.deleteStay(99L, testUser));
    }

    @Test
    void getTripStays_success() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(stayRepository.findByTripIdOrderByCheckInDateAsc(1L)).thenReturn(List.of(testStay));
        List<StayResponse> result = stayService.getTripStays(1L, testUser);
        assertEquals(1, result.size());
    }

    @Test
    void getStay_success() {
        when(stayRepository.findById(1L)).thenReturn(Optional.of(testStay));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        StayResponse resp = stayService.getStay(1L, testUser);
        assertNotNull(resp);
        assertEquals("Hilton", resp.getName());
    }

    @Test
    void getStay_notFound_throwsException() {
        when(stayRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> stayService.getStay(99L, testUser));
    }

    @Test
    void addStay_noAccess_throwsException() {
        User other = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        StayRequest req = new StayRequest();
        req.setTripId(1L); req.setName("H"); req.setType(StayType.HOSTEL);
        req.setCheckInDate(LocalDate.now().plusDays(1).toString());
        req.setCheckOutDate(LocalDate.now().plusDays(3).toString());

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findByTripIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class, () -> stayService.addStay(req, other));
    }
}
