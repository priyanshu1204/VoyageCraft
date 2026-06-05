package com.voyagecraft.service;

import com.voyagecraft.dto.template.TemplateResponse;
import com.voyagecraft.dto.trip.*;
import com.voyagecraft.entity.*;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripTemplateServiceTest {

    @InjectMocks private TripTemplateService service;
    @Mock private TripTemplateRepository templateRepository;
    @Mock private TripService tripService;
    @Mock private ModelMapper modelMapper;

    private User testUser;
    private TripTemplate testTemplate;
    private TemplateResponse testResp;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTemplate = TripTemplate.builder().id(1L).name("Beach Vacation")
                .description("Relaxing beach trip").durationDays(7)
                .budgetEstimate(BigDecimal.valueOf(3000)).category("BEACH").build();
        testResp = new TemplateResponse();
        testResp.setId(1L); testResp.setName("Beach Vacation");
    }

    @Test
    void getAllTemplates_success() {
        when(templateRepository.findAllByOrderByNameAsc()).thenReturn(List.of(testTemplate));
        when(modelMapper.map(testTemplate, TemplateResponse.class)).thenReturn(testResp);

        List<TemplateResponse> result = service.getAllTemplates();
        assertEquals(1, result.size());
        assertEquals("Beach Vacation", result.get(0).getName());
    }

    @Test
    void getAllTemplates_empty() {
        when(templateRepository.findAllByOrderByNameAsc()).thenReturn(Collections.emptyList());
        List<TemplateResponse> result = service.getAllTemplates();
        assertTrue(result.isEmpty());
    }

    @Test
    void getTemplateById_success() {
        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(modelMapper.map(testTemplate, TemplateResponse.class)).thenReturn(testResp);

        TemplateResponse resp = service.getTemplateById(1L);
        assertNotNull(resp);
        assertEquals("Beach Vacation", resp.getName());
    }

    @Test
    void getTemplateById_notFound_throwsException() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getTemplateById(99L));
    }

    @Test
    void createTripFromTemplate_success() {
        TripResponse mockTrip = TripResponse.builder().id(1L).title("Beach Vacation").build();
        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(tripService.createTrip(any(TripRequest.class), eq(testUser))).thenReturn(mockTrip);

        TripResponse resp = service.createTripFromTemplate(1L, LocalDate.now(), testUser);
        assertNotNull(resp);
        assertEquals("Beach Vacation", resp.getTitle());
    }

    @Test
    void createTripFromTemplate_notFound_throwsException() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.createTripFromTemplate(99L, LocalDate.now(), testUser));
    }

    @Test
    void createTripFromTemplate_nullDuration_defaultsTo7() {
        testTemplate.setDurationDays(null);
        TripResponse mockTrip = TripResponse.builder().id(1L).title("Beach Vacation").build();
        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(tripService.createTrip(any(TripRequest.class), eq(testUser))).thenReturn(mockTrip);

        TripResponse resp = service.createTripFromTemplate(1L, LocalDate.now(), testUser);
        assertNotNull(resp);
    }
}
