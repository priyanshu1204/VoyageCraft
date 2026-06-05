package com.voyagecraft.service;

import com.voyagecraft.dto.quickaction.*;
import com.voyagecraft.dto.quickaction.QuickActionDashboard.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.exception.ResourceNotFoundException;
import com.voyagecraft.exception.UnauthorizedException;
import com.voyagecraft.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuickActionService {

    private final TripRepository tripRepository;
    private final TripCollaboratorRepository collaboratorRepository;
    private final TransportDetailRepository transportDetailRepository;
    private final StayRepository stayRepository;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryDayRepository itineraryDayRepository;
    private final ActivityRepository activityRepository;
    private final QuickNoteRepository quickNoteRepository;
    private final NotificationRepository notificationRepository;

    // ── Dashboard ──

    @Transactional(readOnly = true)
    public QuickActionDashboard getDashboard(Long tripId, User user) {
        Trip trip = getTripWithAccess(tripId, user);
        LocalDate today = LocalDate.now();

        // Today's schedule from itinerary
        List<ScheduleItem> todaySchedule = buildTodaySchedule(tripId, today);

        // Upcoming check-ins (flights + stays within the next 3 days)
        List<CheckInItem> checkIns = buildUpcomingCheckIns(tripId, today);

        // Recent notes
        List<QuickNoteResponse> notes = quickNoteRepository
                .findByTripIdAndUserIdOrderByCreatedAtDesc(tripId, user.getId())
                .stream().limit(10).map(this::mapNote).collect(Collectors.toList());

        // Unread notifications
        int unread = notificationRepository.countByTripIdAndUserIdAndReadStatus(tripId, user.getId(), false);

        return QuickActionDashboard.builder()
                .tripId(tripId)
                .tripTitle(trip.getTitle())
                .todaySchedule(todaySchedule)
                .upcomingCheckIns(checkIns)
                .recentNotes(notes)
                .unreadNotifications(unread)
                .build();
    }

    // ── Check-In Shortcuts ──

    @Transactional
    public void toggleTransportCheckIn(Long transportId, User user) {
        TransportDetail td = transportDetailRepository.findById(transportId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport not found: " + transportId));
        getTripWithAccess(td.getTrip().getId(), user);

        Boolean current = td.getCheckedIn() != null ? td.getCheckedIn() : false;
        td.setCheckedIn(!current);
        transportDetailRepository.save(td);
        log.info("Transport {} check-in toggled to {}", transportId, !current);
    }

    @Transactional
    public void toggleStayCheckIn(Long stayId, User user) {
        Stay stay = stayRepository.findById(stayId)
                .orElseThrow(() -> new ResourceNotFoundException("Stay not found: " + stayId));
        getTripWithAccess(stay.getTrip().getId(), user);

        Boolean current = stay.getCheckedIn() != null ? stay.getCheckedIn() : false;
        stay.setCheckedIn(!current);
        stayRepository.save(stay);
        log.info("Stay {} check-in toggled to {}", stayId, !current);
    }

    // ── One-Tap Reorder ──

    @Transactional
    public void reorderDayItems(Long dayId, List<Long> orderedItemIds, User user) {
        ItineraryDay day = itineraryDayRepository.findById(dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary day not found: " + dayId));
        getTripWithAccess(day.getItinerary().getTrip().getId(), user);

        List<ItineraryItem> items = day.getItems();
        Map<Long, ItineraryItem> itemMap = items.stream()
                .collect(Collectors.toMap(ItineraryItem::getId, i -> i));

        for (int i = 0; i < orderedItemIds.size(); i++) {
            ItineraryItem item = itemMap.get(orderedItemIds.get(i));
            if (item != null) {
                item.setOrderIndex(i);
            }
        }
        itineraryDayRepository.save(day);
        log.info("Reordered {} items for day {}", orderedItemIds.size(), dayId);
    }

    // ── Quick Notes CRUD ──

    @Transactional
    public QuickNoteResponse createNote(QuickNoteRequest request, User user) {
        Trip trip = getTripWithAccess(request.getTripId(), user);

        QuickNote note = QuickNote.builder()
                .trip(trip)
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .photoUrls(request.getPhotoUrls())
                .capturedLatitude(request.getCapturedLatitude())
                .capturedLongitude(request.getCapturedLongitude())
                .capturedLocationName(request.getCapturedLocationName())
                .isSynced(request.getIsSynced() != null ? request.getIsSynced() : true)
                .isPinned(request.getIsPinned() != null ? request.getIsPinned() : false)
                .build();

        return mapNote(quickNoteRepository.save(note));
    }

    @Transactional
    public QuickNoteResponse updateNote(Long noteId, QuickNoteRequest request, User user) {
        QuickNote note = quickNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + noteId));
        if (!note.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not your note");
        }

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setPhotoUrls(request.getPhotoUrls());
        note.setCapturedLatitude(request.getCapturedLatitude());
        note.setCapturedLongitude(request.getCapturedLongitude());
        note.setCapturedLocationName(request.getCapturedLocationName());
        note.setIsPinned(request.getIsPinned() != null ? request.getIsPinned() : note.getIsPinned());
        note.setIsSynced(true);

        return mapNote(quickNoteRepository.save(note));
    }

    @Transactional
    public void deleteNote(Long noteId, User user) {
        QuickNote note = quickNoteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found: " + noteId));
        if (!note.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Not your note");
        }
        quickNoteRepository.delete(note);
    }

    @Transactional(readOnly = true)
    public List<QuickNoteResponse> getNotes(Long tripId, User user) {
        getTripWithAccess(tripId, user);
        return quickNoteRepository.findByTripIdAndUserIdOrderByCreatedAtDesc(tripId, user.getId())
                .stream().map(this::mapNote).collect(Collectors.toList());
    }

    // ── Push Notification Registration (stub for web push) ──

    @Transactional
    public void registerPushToken(Long userId, String pushToken, String deviceType) {
        // In a full implementation, this would store push tokens for FCM/APNs
        log.info("Push token registered for user {}: {} ({})", userId, pushToken.substring(0, Math.min(10, pushToken.length())) + "...", deviceType);
    }

    // ── Helpers ──

    private List<ScheduleItem> buildTodaySchedule(Long tripId, LocalDate today) {
        List<ScheduleItem> schedule = new ArrayList<>();

        // Get itinerary items for today
        try {
            List<Itinerary> itineraries = itineraryRepository.findByTripIdOrderByVersionNumberAsc(tripId);
            for (Itinerary itin : itineraries) {
                for (ItineraryDay day : itin.getDays()) {
                    if (day.getDayDate().equals(today)) {
                        for (ItineraryItem item : day.getItems()) {
                            schedule.add(ScheduleItem.builder()
                                    .id(item.getId())
                                    .title(item.getTitle())
                                    .location(item.getLocationName())
                                    .startTime(item.getStartTime())
                                    .endTime(item.getEndTime())
                                    .orderIndex(item.getOrderIndex())
                                    .category(item.getCategory() != null ? item.getCategory().name() : null)
                                    .type("ITINERARY_ITEM")
                                    .build());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error loading itinerary for today: {}", e.getMessage());
        }

        // Add activities for today
        try {
            List<Activity> activities = activityRepository.findByTripIdOrderByActivityDateAscStartTimeAsc(tripId);
            for (Activity a : activities) {
                if (a.getActivityDate() != null && a.getActivityDate().equals(today)) {
                    schedule.add(ScheduleItem.builder()
                            .id(a.getId())
                            .title(a.getName())
                            .location(a.getLocation())
                            .startTime(a.getStartTime())
                            .endTime(a.getEndTime())
                            .category(a.getCategory() != null ? a.getCategory().name() : null)
                            .type("ACTIVITY")
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Error loading activities for today: {}", e.getMessage());
        }

        // Add transports departing today
        try {
            List<TransportDetail> transports = transportDetailRepository.findByTripIdOrderByDepartureTimeAsc(tripId);
            for (TransportDetail t : transports) {
                if (t.getDepartureTime() != null && t.getDepartureTime().toLocalDate().equals(today)) {
                    schedule.add(ScheduleItem.builder()
                            .id(t.getId())
                            .title(t.getType().name() + ": " + t.getDepartureLocation() + " → " + t.getArrivalLocation())
                            .location(t.getDepartureLocation())
                            .startTime(t.getDepartureTime().toLocalTime())
                            .endTime(t.getArrivalTime() != null ? t.getArrivalTime().toLocalTime() : null)
                            .category("TRANSPORT")
                            .type("TRANSPORT")
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Error loading transports for today: {}", e.getMessage());
        }

        // Sort by start time
        schedule.sort(Comparator.comparing(s -> s.getStartTime() != null ? s.getStartTime() : java.time.LocalTime.MAX));

        return schedule;
    }

    private List<CheckInItem> buildUpcomingCheckIns(Long tripId, LocalDate today) {
        List<CheckInItem> checkIns = new ArrayList<>();
        LocalDate threeDaysOut = today.plusDays(3);

        // Flights
        List<TransportDetail> transports = transportDetailRepository.findByTripIdOrderByDepartureTimeAsc(tripId);
        for (TransportDetail t : transports) {
            if (t.getDepartureTime() != null) {
                LocalDate dep = t.getDepartureTime().toLocalDate();
                if (!dep.isBefore(today) && !dep.isAfter(threeDaysOut)) {
                    String subtitle = t.getProvider() != null ? t.getProvider() : "";
                    if (t.getFlightNumber() != null) subtitle += " " + t.getFlightNumber();

                    checkIns.add(CheckInItem.builder()
                            .id(t.getId())
                            .type("FLIGHT")
                            .title(t.getDepartureLocation() + " → " + t.getArrivalLocation())
                            .subtitle(subtitle.trim())
                            .bookingReference(t.getBookingReference())
                            .dateTime(t.getDepartureTime())
                            .checkedIn(t.getCheckedIn() != null ? t.getCheckedIn() : false)
                            .build());
                }
            }
        }

        // Stays
        List<Stay> stays = stayRepository.findByTripIdOrderByCheckInDateAsc(tripId);
        for (Stay s : stays) {
            if (s.getCheckInDate() != null) {
                if (!s.getCheckInDate().isBefore(today) && !s.getCheckInDate().isAfter(threeDaysOut)) {
                    checkIns.add(CheckInItem.builder()
                            .id(s.getId())
                            .type("STAY")
                            .title(s.getName())
                            .subtitle(s.getType() != null ? s.getType().name() : "")
                            .bookingReference(s.getBookingReference())
                            .dateTime(s.getCheckInTime() != null
                                    ? LocalDateTime.of(s.getCheckInDate(), s.getCheckInTime())
                                    : s.getCheckInDate().atStartOfDay())
                            .checkedIn(s.getCheckedIn() != null ? s.getCheckedIn() : false)
                            .build());
                }
            }
        }

        checkIns.sort(Comparator.comparing(CheckInItem::getDateTime));
        return checkIns;
    }

    private Trip getTripWithAccess(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        boolean isOwner = trip.getCreatedBy().getId().equals(user.getId());
        boolean isCollaborator = collaboratorRepository.findByTripIdAndUserId(tripId, user.getId()).isPresent();
        if (!isOwner && !isCollaborator) {
            throw new UnauthorizedException("Access denied to this trip");
        }
        return trip;
    }

    private QuickNoteResponse mapNote(QuickNote n) {
        return QuickNoteResponse.builder()
                .id(n.getId())
                .tripId(n.getTrip().getId())
                .title(n.getTitle())
                .content(n.getContent())
                .photoUrls(n.getPhotoUrls())
                .capturedLatitude(n.getCapturedLatitude())
                .capturedLongitude(n.getCapturedLongitude())
                .capturedLocationName(n.getCapturedLocationName())
                .isSynced(n.getIsSynced())
                .isPinned(n.getIsPinned())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
