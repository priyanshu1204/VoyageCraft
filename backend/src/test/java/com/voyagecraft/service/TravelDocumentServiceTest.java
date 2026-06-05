package com.voyagecraft.service;

import com.voyagecraft.dto.document.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.ChecklistItemType;
import com.voyagecraft.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TravelDocumentServiceTest {

    @InjectMocks private TravelDocumentService service;
    @Mock private TravelDocumentRepository docRepository;
    @Mock private DocumentChecklistRepository checklistRepository;
    @Mock private DocumentReminderRepository reminderRepository;
    @Mock private TripRepository tripRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;

    private User testUser;
    private Trip testTrip;
    private TravelDocument testDoc;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").createdBy(testUser).createdAt(LocalDateTime.now()).build();
        testDoc = TravelDocument.builder().id(1L).trip(testTrip).user(testUser)
                .countryName("India").countryCode("IN")
                .visaRequirements("E-Visa").entryGuidelines("Valid passport")
                .createdAt(LocalDateTime.now()).build();
        testDoc.setChecklistItems(new ArrayList<>());
        testDoc.setReminders(new ArrayList<>());
    }

    @Test
    void createDocument_success() {
        TravelDocumentRequest req = TravelDocumentRequest.builder()
                .countryName("India").countryCode("IN")
                .visaRequirements("E-Visa").entryGuidelines("Valid passport").build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(docRepository.save(any())).thenReturn(testDoc);

        TravelDocumentResponse resp = service.createDocument(1L, req, testUser);
        assertNotNull(resp);
        assertEquals("India", resp.getCountryName());
    }

    @Test
    void createDocument_tripNotFound() {
        TravelDocumentRequest req = TravelDocumentRequest.builder().countryName("X").build();
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.createDocument(99L, req, testUser));
    }

    @Test
    void updateDocument_success() {
        TravelDocumentRequest req = TravelDocumentRequest.builder()
                .countryName("Japan").countryCode("JP").build();

        when(docRepository.findById(1L)).thenReturn(Optional.of(testDoc));
        when(docRepository.save(any())).thenReturn(testDoc);

        TravelDocumentResponse resp = service.updateDocument(1L, req, testUser);
        assertNotNull(resp);
    }

    @Test
    void updateDocument_notFound() {
        TravelDocumentRequest req = TravelDocumentRequest.builder().countryName("X").build();
        when(docRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.updateDocument(99L, req, testUser));
    }

    @Test
    void getDocument_success() {
        when(docRepository.findById(1L)).thenReturn(Optional.of(testDoc));
        TravelDocumentResponse resp = service.getDocument(1L, testUser);
        assertNotNull(resp);
        assertEquals("India", resp.getCountryName());
    }

    @Test
    void getDocument_notFound() {
        when(docRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getDocument(99L, testUser));
    }

    @Test
    void deleteDocument_success() {
        when(docRepository.findById(1L)).thenReturn(Optional.of(testDoc));
        service.deleteDocument(1L, testUser);
        verify(docRepository).delete(testDoc);
    }

    @Test
    void addChecklistItem_success() {
        ChecklistItemRequest req = new ChecklistItemRequest();
        req.setItemName("Visa copy"); req.setItemType("VISA");
        when(docRepository.findById(1L)).thenReturn(Optional.of(testDoc));
        DocumentChecklist saved = DocumentChecklist.builder().id(1L).travelDocument(testDoc)
                .itemName("Visa copy").itemType(ChecklistItemType.VISA).completed(false).build();
        when(checklistRepository.save(any())).thenReturn(saved);
        ChecklistItemResponse resp = service.addChecklistItemToDoc(1L, req, testUser);
        assertNotNull(resp);
    }

    @Test
    void toggleChecklistItem_success() {
        DocumentChecklist item = DocumentChecklist.builder().id(1L).travelDocument(testDoc)
                .itemName("Visa").itemType(ChecklistItemType.VISA).completed(false).build();
        when(checklistRepository.findById(1L)).thenReturn(Optional.of(item));
        when(docRepository.findById(1L)).thenReturn(Optional.of(testDoc));
        when(checklistRepository.save(any())).thenReturn(item);
        ChecklistItemResponse resp = service.toggleChecklistItem(1L, testUser);
        assertNotNull(resp);
    }

    @Test
    void deleteChecklistItem_success() {
        DocumentChecklist item = DocumentChecklist.builder().id(1L).travelDocument(testDoc)
                .itemName("X").itemType(ChecklistItemType.VISA).build();
        when(checklistRepository.findById(1L)).thenReturn(Optional.of(item));
        when(docRepository.findById(1L)).thenReturn(Optional.of(testDoc));
        service.deleteChecklistItem(1L, testUser);
        verify(checklistRepository).delete(item);
    }

    @Test
    void addReminder_success() {
        ReminderRequest req = new ReminderRequest();
        req.setTitle("Renew passport"); req.setReminderDate(LocalDate.now().plusMonths(1).toString());
        when(docRepository.findById(1L)).thenReturn(Optional.of(testDoc));
        DocumentReminder saved = DocumentReminder.builder().id(1L).travelDocument(testDoc)
                .title("Renew passport").reminderDate(LocalDate.now().plusMonths(1))
                .dismissed(false).build();
        when(reminderRepository.save(any())).thenReturn(saved);
        ReminderResponse resp = service.addReminderToDoc(1L, req, testUser);
        assertNotNull(resp);
    }

    @Test
    void dismissReminder_success() {
        DocumentReminder rem = DocumentReminder.builder().id(1L).travelDocument(testDoc)
                .title("X").dismissed(false).build();
        when(reminderRepository.findById(1L)).thenReturn(Optional.of(rem));
        when(docRepository.findById(1L)).thenReturn(Optional.of(testDoc));
        when(reminderRepository.save(any())).thenReturn(rem);
        ReminderResponse resp = service.dismissReminder(1L, testUser);
        assertNotNull(resp);
    }

    @Test
    void deleteReminder_success() {
        DocumentReminder rem = DocumentReminder.builder().id(1L).travelDocument(testDoc).title("X").build();
        when(reminderRepository.findById(1L)).thenReturn(Optional.of(rem));
        when(docRepository.findById(1L)).thenReturn(Optional.of(testDoc));
        service.deleteReminder(1L, testUser);
        verify(reminderRepository).delete(rem);
    }

    @Test
    void getCountryLibrary_success() {
        List<Map<String, Object>> library = service.getCountryLibrary();
        assertNotNull(library);
        assertFalse(library.isEmpty());
    }
}
