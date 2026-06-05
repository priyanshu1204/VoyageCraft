package com.voyagecraft.service;

import com.voyagecraft.dto.packing.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.*;
import com.voyagecraft.exception.*;
import com.voyagecraft.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackingServiceTest {

    @InjectMocks private PackingService service;
    @Mock private PackingItemRepository packingItemRepository;
    @Mock private TripRepository tripRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;

    private User testUser;
    private Trip testTrip;
    private PackingItem testItem;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").createdBy(testUser).createdAt(LocalDateTime.now()).build();
        testItem = PackingItem.builder().id(1L).trip(testTrip).name("Passport")
                .category(PackingCategory.DOCUMENTS).quantity(1).packed(false)
                .isFromTemplate(false).createdAt(LocalDateTime.now()).build();
    }

    @Test
    void addItem_success() {
        PackingItemRequest req = new PackingItemRequest();
        req.setTripId(1L); req.setName("Passport"); req.setCategory(PackingCategory.DOCUMENTS);

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(packingItemRepository.save(any())).thenReturn(testItem);

        PackingItemResponse resp = service.addItem(req, testUser);
        assertNotNull(resp);
        assertEquals("Passport", resp.getName());
    }

    @Test
    void addItem_tripNotFound_throwsException() {
        PackingItemRequest req = new PackingItemRequest();
        req.setTripId(99L);
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.addItem(req, testUser));
    }

    @Test
    void updateItem_success() {
        PackingItemRequest req = new PackingItemRequest();
        req.setName("Updated"); req.setCategory(PackingCategory.CLOTHING); req.setQuantity(3);
        when(packingItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(packingItemRepository.save(any())).thenReturn(testItem);
        PackingItemResponse resp = service.updateItem(1L, req, testUser);
        assertNotNull(resp);
    }

    @Test
    void updateItem_notFound_throwsException() {
        PackingItemRequest req = new PackingItemRequest();
        when(packingItemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.updateItem(99L, req, testUser));
    }

    @Test
    void togglePacked_success() {
        when(packingItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(packingItemRepository.save(any())).thenReturn(testItem);
        PackingItemResponse resp = service.togglePacked(1L, testUser);
        assertNotNull(resp);
    }

    @Test
    void deleteItem_success() {
        when(packingItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        service.deleteItem(1L, testUser);
        verify(packingItemRepository).delete(testItem);
    }

    @Test
    void deleteItem_notFound_throwsException() {
        when(packingItemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteItem(99L, testUser));
    }

    @Test
    void getSummary_success() {
        PackingItem packed = PackingItem.builder().id(2L).trip(testTrip).name("Socks")
                .category(PackingCategory.CLOTHING).quantity(3).packed(true)
                .isFromTemplate(false).createdAt(LocalDateTime.now()).build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(packingItemRepository.findByTripIdOrderByCategoryAscNameAsc(1L)).thenReturn(List.of(testItem, packed));

        PackingSummaryResponse resp = service.getSummary(1L, testUser);
        assertNotNull(resp);
        assertEquals(2, resp.getTotalItems());
        assertEquals(1, resp.getPackedItems());
        assertTrue(resp.getPackedPercent() > 0);
    }

    @Test
    void getSummary_empty() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(packingItemRepository.findByTripIdOrderByCategoryAscNameAsc(1L)).thenReturn(Collections.emptyList());
        PackingSummaryResponse resp = service.getSummary(1L, testUser);
        assertEquals(0, resp.getTotalItems());
        assertEquals(0, resp.getPackedPercent());
    }

    @Test
    void applyTemplate_tropical() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(packingItemRepository.save(any())).thenReturn(testItem);
        List<PackingItemResponse> result = service.applyTemplate(1L, ClimateType.TROPICAL, testUser);
        assertFalse(result.isEmpty());
    }

    @Test
    void applyTemplate_cold() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(packingItemRepository.save(any())).thenReturn(testItem);
        List<PackingItemResponse> result = service.applyTemplate(1L, ClimateType.COLD, testUser);
        assertFalse(result.isEmpty());
    }

    @Test
    void applyTemplate_desert() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(packingItemRepository.save(any())).thenReturn(testItem);
        List<PackingItemResponse> result = service.applyTemplate(1L, ClimateType.DESERT, testUser);
        assertFalse(result.isEmpty());
    }

    @Test
    void applyTemplate_urban() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(packingItemRepository.save(any())).thenReturn(testItem);
        List<PackingItemResponse> result = service.applyTemplate(1L, ClimateType.URBAN, testUser);
        assertFalse(result.isEmpty());
    }

    @Test
    void applyTemplate_rainy() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(packingItemRepository.save(any())).thenReturn(testItem);
        List<PackingItemResponse> result = service.applyTemplate(1L, ClimateType.RAINY, testUser);
        assertFalse(result.isEmpty());
    }

    @Test
    void getAvailableClimates_success() {
        List<String> climates = service.getAvailableClimates();
        assertNotNull(climates);
        assertFalse(climates.isEmpty());
        assertTrue(climates.contains("TROPICAL"));
    }

    @Test
    void getTemplateItems_temperate() {
        List<PackingTemplateItem> items = service.getTemplateItems(ClimateType.TEMPERATE);
        assertFalse(items.isEmpty());
    }

    @Test
    void getTemplateItems_beach() {
        List<PackingTemplateItem> items = service.getTemplateItems(ClimateType.BEACH);
        assertFalse(items.isEmpty());
    }

    @Test
    void getTemplateItems_mountain() {
        List<PackingTemplateItem> items = service.getTemplateItems(ClimateType.MOUNTAIN);
        assertFalse(items.isEmpty());
    }
}
