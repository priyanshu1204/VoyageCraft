package com.voyagecraft.service;

import com.voyagecraft.dto.destination.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.exception.*;
import com.voyagecraft.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripDestinationServiceTest {

    @InjectMocks private TripDestinationService service;
    @Mock private TripDestinationRepository destinationRepository;
    @Mock private TripRepository tripRepository;
    @Mock private ModelMapper modelMapper;

    private Trip testTrip;
    private TripDestination testDest;
    private DestinationResponse testResp;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").createdBy(user).build();
        testDest = TripDestination.builder().id(1L).trip(testTrip)
                .destinationName("Paris").country("France").orderIndex(0).build();
        testResp = new DestinationResponse();
        testResp.setId(1L); testResp.setDestinationName("Paris"); testResp.setCountry("France");
    }

    @Test
    void addDestination_success() {
        DestinationRequest req = DestinationRequest.builder()
                .destinationName("Paris").country("France").orderIndex(0).build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(destinationRepository.save(any())).thenReturn(testDest);
        when(modelMapper.map(testDest, DestinationResponse.class)).thenReturn(testResp);

        DestinationResponse resp = service.addDestination(1L, req);
        assertNotNull(resp);
        assertEquals("Paris", resp.getDestinationName());
    }

    @Test
    void addDestination_tripNotFound_throwsException() {
        DestinationRequest req = DestinationRequest.builder().destinationName("X").build();
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.addDestination(99L, req));
    }

    @Test
    void getDestinations_success() {
        when(destinationRepository.findByTripIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(testDest));
        when(modelMapper.map(testDest, DestinationResponse.class)).thenReturn(testResp);

        List<DestinationResponse> result = service.getDestinations(1L);
        assertEquals(1, result.size());
    }

    @Test
    void updateDestination_success() {
        DestinationRequest req = DestinationRequest.builder()
                .destinationName("Lyon").country("France").orderIndex(1).build();

        when(destinationRepository.findById(1L)).thenReturn(Optional.of(testDest));
        when(destinationRepository.save(any())).thenReturn(testDest);
        when(modelMapper.map(testDest, DestinationResponse.class)).thenReturn(testResp);

        DestinationResponse resp = service.updateDestination(1L, 1L, req);
        assertNotNull(resp);
    }

    @Test
    void updateDestination_notFound_throwsException() {
        DestinationRequest req = DestinationRequest.builder().destinationName("X").build();
        when(destinationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.updateDestination(1L, 99L, req));
    }

    @Test
    void deleteDestination_success() {
        when(destinationRepository.findById(1L)).thenReturn(Optional.of(testDest));
        service.deleteDestination(1L, 1L);
        verify(destinationRepository).delete(testDest);
    }

    @Test
    void deleteDestination_notFound_throwsException() {
        when(destinationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteDestination(1L, 99L));
    }

    @Test
    void addDestination_nullOrderIndex_defaultsToZero() {
        DestinationRequest req = DestinationRequest.builder()
                .destinationName("Rome").country("Italy").orderIndex(null).build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(destinationRepository.save(any())).thenReturn(testDest);
        when(modelMapper.map(any(), eq(DestinationResponse.class))).thenReturn(testResp);

        DestinationResponse resp = service.addDestination(1L, req);
        assertNotNull(resp);
    }
}
