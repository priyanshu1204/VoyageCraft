package com.voyagecraft.service;

import com.voyagecraft.dto.document.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.ChecklistItemType;
import com.voyagecraft.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelDocumentService {

    private final TravelDocumentRepository documentRepository;
    private final DocumentChecklistRepository checklistRepository;
    private final DocumentReminderRepository reminderRepository;
    private final TripRepository tripRepository;
    private final TripCollaboratorRepository collaboratorRepository;

    @Transactional
    public TravelDocumentResponse createDocument(Long tripId, TravelDocumentRequest request, User user) {
        Trip trip = getTripWithAccessCheck(tripId, user);
        TravelDocument doc = TravelDocument.builder()
                .trip(trip).user(user).countryName(request.getCountryName()).countryCode(request.getCountryCode())
                .visaRequirements(request.getVisaRequirements()).entryGuidelines(request.getEntryGuidelines())
                .officialLink(request.getOfficialLink()).additionalNotes(request.getAdditionalNotes()).build();
        TravelDocument saved = documentRepository.save(doc);
        if (request.getChecklistItems() != null) {
            for (ChecklistItemRequest item : request.getChecklistItems()) { addChecklistItem(saved, item); }
        }
        if (request.getReminders() != null) {
            for (ReminderRequest rem : request.getReminders()) { addReminder(saved, rem); }
        }
        return mapToResponse(documentRepository.findById(saved.getId()).orElse(saved));
    }

    @Transactional
    public TravelDocumentResponse updateDocument(Long docId, TravelDocumentRequest request, User user) {
        TravelDocument doc = getDocumentWithAccessCheck(docId, user);
        doc.setCountryName(request.getCountryName()); doc.setCountryCode(request.getCountryCode());
        doc.setVisaRequirements(request.getVisaRequirements()); doc.setEntryGuidelines(request.getEntryGuidelines());
        doc.setOfficialLink(request.getOfficialLink()); doc.setAdditionalNotes(request.getAdditionalNotes());
        documentRepository.save(doc);
        return mapToResponse(doc);
    }

    @Transactional(readOnly = true)
    public List<TravelDocumentResponse> getTripDocuments(Long tripId, User user) {
        getTripWithAccessCheck(tripId, user);
        return documentRepository.findByTripIdAndUserIdOrderByCountryNameAsc(tripId, user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TravelDocumentResponse getDocument(Long docId, User user) {
        return mapToResponse(getDocumentWithAccessCheck(docId, user));
    }

    @Transactional
    public void deleteDocument(Long docId, User user) {
        TravelDocument doc = getDocumentWithAccessCheck(docId, user);
        documentRepository.delete(doc);
    }

    @Transactional
    public ChecklistItemResponse addChecklistItemToDoc(Long docId, ChecklistItemRequest request, User user) {
        TravelDocument doc = getDocumentWithAccessCheck(docId, user);
        return mapChecklistToResponse(addChecklistItem(doc, request));
    }

    @Transactional
    public ChecklistItemResponse toggleChecklistItem(Long itemId, User user) {
        DocumentChecklist item = checklistRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Checklist item not found"));
        getDocumentWithAccessCheck(item.getTravelDocument().getId(), user);
        item.setCompleted(!item.getCompleted());
        checklistRepository.save(item);
        return mapChecklistToResponse(item);
    }

    @Transactional
    public void deleteChecklistItem(Long itemId, User user) {
        DocumentChecklist item = checklistRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Checklist item not found"));
        getDocumentWithAccessCheck(item.getTravelDocument().getId(), user);
        checklistRepository.delete(item);
    }

    @Transactional
    public ReminderResponse addReminderToDoc(Long docId, ReminderRequest request, User user) {
        TravelDocument doc = getDocumentWithAccessCheck(docId, user);
        return mapReminderToResponse(addReminder(doc, request));
    }

    @Transactional
    public ReminderResponse dismissReminder(Long reminderId, User user) {
        DocumentReminder reminder = reminderRepository.findById(reminderId).orElseThrow(() -> new RuntimeException("Reminder not found"));
        getDocumentWithAccessCheck(reminder.getTravelDocument().getId(), user);
        reminder.setDismissed(true);
        reminderRepository.save(reminder);
        return mapReminderToResponse(reminder);
    }

    @Transactional
    public void deleteReminder(Long reminderId, User user) {
        DocumentReminder reminder = reminderRepository.findById(reminderId).orElseThrow(() -> new RuntimeException("Reminder not found"));
        getDocumentWithAccessCheck(reminder.getTravelDocument().getId(), user);
        reminderRepository.delete(reminder);
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getUpcomingReminders(User user) {
        return reminderRepository.findUpcomingReminders(user.getId(), LocalDate.now().plusDays(30))
                .stream().map(this::mapReminderToResponse).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getCountryLibrary() {
        List<Map<String, Object>> countries = new ArrayList<>();
        countries.add(buildCountryEntry("India", "IN", "E-Visa available for many nationalities. Apply at indianvisaonline.gov.in", "Valid passport (6+ months), return ticket, proof of funds", "https://indianvisaonline.gov.in", List.of("No mandatory vaccines for most countries", "COVID vaccine may be required", "Travel insurance recommended"), List.of("Passport copies", "Visa printout", "Travel insurance document", "Hotel bookings")));
        countries.add(buildCountryEntry("Thailand", "TH", "Visa exemption for 30 days for many countries. VOA available.", "Passport valid 6+ months, proof of onward travel, 20,000 THB equivalent", "https://www.thaiembassy.com", List.of("Hepatitis A & B recommended", "COVID vaccine certificate", "Yellow fever certificate if from endemic area"), List.of("Passport", "Flight itinerary", "Hotel confirmation", "Travel insurance")));
        countries.add(buildCountryEntry("United States", "US", "B1/B2 visitor visa required for most nationalities. ESTA for VWP countries.", "Valid passport, DS-160 form, interview appointment", "https://travel.state.gov/visa", List.of("No mandatory vaccines for entry", "COVID test may be required", "Standard immunizations up to date"), List.of("DS-160 confirmation", "Interview appointment letter", "Financial documents", "Invitation letter if applicable")));
        countries.add(buildCountryEntry("United Kingdom", "GB", "Standard Visitor visa or ETA. Apply online via gov.uk", "Valid passport, proof of accommodation, financial evidence", "https://www.gov.uk/standard-visitor", List.of("No mandatory vaccines", "NHS surcharge may apply", "Travel insurance recommended"), List.of("Visa/ETA confirmation", "Accommodation proof", "Return flight tickets", "Bank statements")));
        countries.add(buildCountryEntry("Japan", "JP", "Visa-free for many nationalities (90 days). Visit Japan Web registration.", "Valid passport, return ticket, sufficient funds", "https://www.mofa.go.jp/j_info/visit/visa", List.of("No mandatory vaccines", "COVID guidelines may apply", "Standard immunizations recommended"), List.of("Passport", "Visit Japan Web QR code", "Hotel reservations", "Travel insurance")));
        countries.add(buildCountryEntry("Singapore", "SG", "Visa-free for many nationalities (30-90 days). SG Arrival Card required.", "Valid passport (6+ months), SG Arrival Card submitted online", "https://www.ica.gov.sg", List.of("Yellow fever if from endemic area", "No other mandatory vaccines", "Dengue precautions advised"), List.of("SG Arrival Card", "Passport", "Hotel booking", "Return ticket")));
        countries.add(buildCountryEntry("Dubai (UAE)", "AE", "Visa on arrival for many nationalities (30 days). Pre-arranged visa for others.", "Valid passport (6+ months), return ticket, hotel booking", "https://www.government.ae/en/information-and-services/visa-and-emirates-id", List.of("No mandatory vaccines", "COVID vaccine may be requested", "Yellow fever certificate if applicable"), List.of("Passport", "Hotel confirmation", "Return ticket", "Travel insurance")));
        countries.add(buildCountryEntry("France", "FR", "Schengen visa required for non-EU citizens. Apply at consulate.", "Valid passport, travel insurance (30,000 EUR min), itinerary, financial proof", "https://france-visas.gouv.fr", List.of("No mandatory vaccines", "COVID booster recommended", "Travel insurance mandatory for Schengen"), List.of("Schengen visa", "Travel insurance certificate", "Hotel bookings", "Flight itinerary", "Bank statements")));
        return countries;
    }

    private Map<String, Object> buildCountryEntry(String name, String code, String visa, String entry, String link, List<String> vaccines, List<String> docs) {
        Map<String, Object> country = new LinkedHashMap<>();
        country.put("countryName", name); country.put("countryCode", code);
        country.put("visaRequirements", visa); country.put("entryGuidelines", entry);
        country.put("officialLink", link); country.put("suggestedVaccines", vaccines);
        country.put("suggestedDocuments", docs);
        return country;
    }

    private DocumentChecklist addChecklistItem(TravelDocument doc, ChecklistItemRequest request) {
        DocumentChecklist item = DocumentChecklist.builder().travelDocument(doc).itemName(request.getItemName())
                .itemType(ChecklistItemType.valueOf(request.getItemType())).description(request.getDescription())
                .documentLink(request.getDocumentLink()).completed(request.getCompleted() != null ? request.getCompleted() : false).build();
        return checklistRepository.save(item);
    }

    private DocumentReminder addReminder(TravelDocument doc, ReminderRequest request) {
        DocumentReminder reminder = DocumentReminder.builder().travelDocument(doc).title(request.getTitle())
                .note(request.getNote()).reminderDate(LocalDate.parse(request.getReminderDate())).build();
        return reminderRepository.save(reminder);
    }

    private TravelDocumentResponse mapToResponse(TravelDocument doc) {
        return TravelDocumentResponse.builder().id(doc.getId()).tripId(doc.getTrip().getId())
                .countryName(doc.getCountryName()).countryCode(doc.getCountryCode())
                .visaRequirements(doc.getVisaRequirements()).entryGuidelines(doc.getEntryGuidelines())
                .officialLink(doc.getOfficialLink()).additionalNotes(doc.getAdditionalNotes())
                .checklistItems(doc.getChecklistItems().stream().map(this::mapChecklistToResponse).collect(Collectors.toList()))
                .reminders(doc.getReminders().stream().map(this::mapReminderToResponse).collect(Collectors.toList()))
                .createdAt(doc.getCreatedAt()).updatedAt(doc.getUpdatedAt()).build();
    }

    private ChecklistItemResponse mapChecklistToResponse(DocumentChecklist item) {
        return ChecklistItemResponse.builder().id(item.getId()).itemName(item.getItemName()).itemType(item.getItemType().name())
                .description(item.getDescription()).documentLink(item.getDocumentLink()).completed(item.getCompleted()).build();
    }

    private ReminderResponse mapReminderToResponse(DocumentReminder r) {
        return ReminderResponse.builder().id(r.getId()).title(r.getTitle()).note(r.getNote())
                .reminderDate(r.getReminderDate()).dismissed(r.getDismissed()).build();
    }

    private Trip getTripWithAccessCheck(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new RuntimeException("Trip not found"));
        boolean isOwner = trip.getCreatedBy().getId().equals(user.getId());
        boolean isCollaborator = collaboratorRepository.findByTripIdAndUserId(trip.getId(), user.getId()).isPresent();
        if (!isOwner && !isCollaborator) throw new RuntimeException("Not authorized");
        return trip;
    }

    private TravelDocument getDocumentWithAccessCheck(Long docId, User user) {
        TravelDocument doc = documentRepository.findById(docId).orElseThrow(() -> new RuntimeException("Document not found"));
        if (!doc.getUser().getId().equals(user.getId())) throw new RuntimeException("Not authorized");
        return doc;
    }
}
