package com.voyagecraft.service;

import com.voyagecraft.dto.activity.ActivityRequest;
import com.voyagecraft.dto.activity.ActivityResponse;
import com.voyagecraft.entity.Activity;
import com.voyagecraft.entity.Trip;
import com.voyagecraft.entity.User;
import com.voyagecraft.enums.ActivityCategory;
import com.voyagecraft.enums.ReservationStatus;
import com.voyagecraft.exception.ResourceNotFoundException;
import com.voyagecraft.exception.UnauthorizedException;
import com.voyagecraft.repository.ActivityRepository;
import com.voyagecraft.repository.TripCollaboratorRepository;
import com.voyagecraft.repository.TripRepository;
import com.voyagecraft.enums.CollaboratorRole;
import com.voyagecraft.enums.InvitationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final TripRepository tripRepository;
    private final TripCollaboratorRepository collaboratorRepository;

    @Transactional
    public ActivityResponse addActivity(ActivityRequest request, User user) {
        Trip trip = getTripWithOwnerCheck(request.getTripId(), user);

        Activity activity = Activity.builder()
                .trip(trip)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .tags(request.getTags())
                .location(request.getLocation())
                .address(request.getAddress())
                .activityDate(request.getActivityDate() != null ? LocalDate.parse(request.getActivityDate()) : null)
                .startTime(request.getStartTime() != null ? LocalTime.parse(request.getStartTime()) : null)
                .endTime(request.getEndTime() != null ? LocalTime.parse(request.getEndTime()) : null)
                .openingHours(request.getOpeningHours())
                .seasonalNotes(request.getSeasonalNotes())
                .cost(request.getCost())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .reservationStatus(request.getReservationStatus() != null ? request.getReservationStatus() : ReservationStatus.PENDING)
                .bookingReference(request.getBookingReference())
                .bookingUrl(request.getBookingUrl())
                .reminderAt(request.getReminderAt() != null ? LocalDateTime.parse(request.getReminderAt()) : null)
                .notes(request.getNotes())
                .alternatives(request.getAlternatives())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .build();

        Activity saved = activityRepository.save(activity);
        log.info("Added activity '{}' ({}) for trip ID={}", request.getName(), request.getCategory(), trip.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public ActivityResponse updateActivity(Long activityId, ActivityRequest request, User user) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));
        getTripWithOwnerCheck(activity.getTrip().getId(), user);

        activity.setName(request.getName());
        activity.setDescription(request.getDescription());
        activity.setCategory(request.getCategory());
        activity.setTags(request.getTags());
        activity.setLocation(request.getLocation());
        activity.setAddress(request.getAddress());
        activity.setActivityDate(request.getActivityDate() != null ? LocalDate.parse(request.getActivityDate()) : null);
        activity.setStartTime(request.getStartTime() != null ? LocalTime.parse(request.getStartTime()) : null);
        activity.setEndTime(request.getEndTime() != null ? LocalTime.parse(request.getEndTime()) : null);
        activity.setOpeningHours(request.getOpeningHours());
        activity.setSeasonalNotes(request.getSeasonalNotes());
        activity.setCost(request.getCost());
        activity.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        activity.setReservationStatus(request.getReservationStatus() != null ? request.getReservationStatus() : ReservationStatus.PENDING);
        activity.setBookingReference(request.getBookingReference());
        activity.setBookingUrl(request.getBookingUrl());
        activity.setReminderAt(request.getReminderAt() != null ? LocalDateTime.parse(request.getReminderAt()) : null);
        activity.setNotes(request.getNotes());
        activity.setAlternatives(request.getAlternatives());
        activity.setPriority(request.getPriority() != null ? request.getPriority() : 0);

        return mapToResponse(activityRepository.save(activity));
    }

    @Transactional
    public void deleteActivity(Long activityId, User user) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + activityId));
        getTripWithOwnerCheck(activity.getTrip().getId(), user);
        activityRepository.delete(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getTripActivities(Long tripId, User user) {
        getTripWithOwnerCheck(tripId, user);
        return activityRepository.findByTripIdOrderByActivityDateAscStartTimeAsc(tripId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getByCategory(Long tripId, ActivityCategory category, User user) {
        getTripWithOwnerCheck(tripId, user);
        return activityRepository.findByTripIdAndCategory(tripId, category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getByTag(Long tripId, String tag, User user) {
        getTripWithOwnerCheck(tripId, user);
        return activityRepository.findByTripIdAndTag(tripId, tag).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getWaitlisted(Long tripId, User user) {
        getTripWithOwnerCheck(tripId, user);
        return activityRepository.findByTripIdAndReservationStatus(tripId, ReservationStatus.WAITLISTED).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getReminders(Long tripId, User user) {
        getTripWithOwnerCheck(tripId, user);
        return activityRepository.findByTripIdOrderByActivityDateAscStartTimeAsc(tripId).stream()
                .filter(a -> a.getReminderAt() != null)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Mock Catalog ────────────────────────────────────────────────────

    public List<ActivityResponse> searchCatalog(String destination, ActivityCategory category) {
        // Mock catalog - generates sample activities for any destination
        String dest = destination != null ? destination : "City";
        ActivityCategory cat = category != null ? category : ActivityCategory.SIGHTSEEING;

        return generateMockCatalog(dest, cat);
    }

    private List<ActivityResponse> generateMockCatalog(String dest, ActivityCategory cat) {
        switch (cat) {
            case ADVENTURE:
                return List.of(
                    mockActivity(dest + " Zip Line Adventure", cat, dest, "Thrilling zipline over scenic canyon", "adventure,outdoor,thrill", "8:00 AM - 5:00 PM", "Best Oct-Mar. Closed during heavy rain & storms.", 75.00, 2),
                    mockActivity(dest + " Kayaking Tour", cat, dest, "Guided kayaking through coastal waters", "adventure,water,outdoor", "6:00 AM - 12:00 PM (morning slot) / 2:00 PM - 6:00 PM (afternoon)", "Year-round. Peak waves Jun-Aug, calmer waters Nov-Feb.", 55.00, 1),
                    mockActivity(dest + " Mountain Hiking", cat, dest, "Half-day guided mountain trek", "adventure,hiking,nature", "5:30 AM - 1:00 PM (early start required)", "Trails open Sep-May. CLOSED Jun-Aug monsoon. Snow gear needed Dec-Jan.", 40.00, 0),
                    mockActivity(dest + " Bungee Jumping", cat, dest, "160ft bungee jump from suspension bridge", "adventure,extreme,thrill", "9:00 AM - 4:30 PM (last jump 4:00 PM)", "Open all year. Wind restrictions apply — check day-of status.", 95.00, 2),
                    mockActivity(dest + " Rock Climbing", cat, dest, "Outdoor climbing with certified instructors", "adventure,climbing,fitness", "7:00 AM - 11:00 AM / 3:00 PM - 6:30 PM (avoid midday heat)", "Best Sep-Apr. Too hot May-Aug. Beginners welcome year-round.", 60.00, 1)
                );
            case FAMILY:
                return List.of(
                    mockActivity(dest + " Aquarium", cat, dest, "World-class aquarium with marine exhibits", "family,indoor,educational", "9:00 AM - 7:00 PM (last entry 6:00 PM)", "Open year-round. Extended hours 9 AM-9 PM during Jul-Aug school holidays.", 30.00, 2),
                    mockActivity(dest + " Theme Park", cat, dest, "Full-day family theme park with rides", "family,outdoor,fun", "10:00 AM - 9:00 PM (weekends till 10 PM)", "Peak Jun-Aug with all rides open. Reduced hours & some closures Nov-Feb.", 85.00, 1),
                    mockActivity(dest + " Zoo & Safari", cat, dest, "Walk-through zoo with safari experience", "family,animals,outdoor", "8:30 AM - 5:30 PM daily", "Open year-round. Baby animal season Mar-May. Night safari available Fri-Sat.", 25.00, 0),
                    mockActivity(dest + " Science Museum", cat, dest, "Interactive science exhibits for all ages", "family,indoor,learning", "10:00 AM - 6:00 PM (closed Mondays)", "Special holiday exhibits Dec-Jan. Planetarium shows hourly from 11 AM.", 18.00, 1),
                    mockActivity(dest + " Botanical Garden", cat, dest, "Lush gardens with children's play area", "family,nature,outdoor", "6:00 AM - 8:00 PM (summer) / 7:00 AM - 6:00 PM (winter)", "Cherry blossoms Mar-Apr. Tulip festival May. Autumn colors Oct-Nov.", 12.00, 0)
                );
            case CULTURAL:
                return List.of(
                    mockActivity(dest + " Heritage Walking Tour", cat, dest, "Expert-guided old city walking tour", "culture,history,walking", "9:00 AM - 12:00 PM / 4:00 PM - 7:00 PM", "Daily. Skip Jul-Aug heat — take evening slot. Festival specials during Diwali.", 20.00, 1),
                    mockActivity(dest + " Museum of Art", cat, dest, "Extensive collection of local and international art", "culture,art,indoor", "10:00 AM - 6:00 PM (Thu till 9 PM for late night)", "Closed Mondays. Free entry first Sunday of month. Temporary exhibits rotate quarterly.", 15.00, 0),
                    mockActivity(dest + " Traditional Dance Show", cat, dest, "Evening performance of regional dance", "culture,entertainment,evening", "7:30 PM - 9:30 PM", "Shows Thu-Sun only. Special Navratri performances Oct. Book 2 weeks ahead for peak season.", 35.00, 0),
                    mockActivity(dest + " Temple & Architecture Tour", cat, dest, "Visit ancient temples and colonial architecture", "culture,history,architecture", "7:00 AM - 11:00 AM (temples) / 3:00 PM - 6:00 PM (colonial district)", "Temples have dress code. Avoid noon heat. Special ceremonies during full moon days.", 22.00, 2),
                    mockActivity(dest + " Local Artisan Workshop", cat, dest, "Hands-on craft session with local artisans", "culture,craft,workshop", "2:00 PM - 5:00 PM (Mon/Wed/Fri only)", "Limited to 8 participants. Pottery in winter, weaving in summer. Book 3 days ahead.", 45.00, 1)
                );
            case FOOD:
                return List.of(
                    mockActivity(dest + " Street Food Tour", cat, dest, "Guided tasting tour of local street food", "food,walking,local", "11:00 AM - 2:00 PM (lunch tour) / 6:00 PM - 9:00 PM (dinner tour)", "Daily, rain or shine. Ramadan hours may vary. Spice level adjustable.", 45.00, 1),
                    mockActivity(dest + " Cooking Class", cat, dest, "Learn to cook 3 traditional dishes", "food,cooking,indoor", "10:00 AM - 1:00 PM (includes market visit)", "Advance booking required — 48h minimum. Vegetarian/vegan options available. Market closed Sundays.", 60.00, 0),
                    mockActivity(dest + " Wine & Cheese Tasting", cat, dest, "Visit local vineyard with tasting session", "food,wine,outdoor", "2:00 PM - 5:30 PM", "Harvest season Sep-Nov is best. Vineyard closed Jan-Feb for pruning. Must be 21+.", 50.00, 0),
                    mockActivity(dest + " Farm-to-Table Experience", cat, dest, "Pick ingredients and cook a meal at organic farm", "food,farm,organic", "8:00 AM - 2:00 PM (full morning experience)", "Best Mar-Oct when farm is in full bloom. Limited availability — weekends only in winter.", 75.00, 2),
                    mockActivity(dest + " Coffee Roasting Workshop", cat, dest, "Bean-to-cup journey with professional roaster", "food,coffee,workshop", "9:30 AM - 11:30 AM / 3:00 PM - 5:00 PM", "Year-round. Fresh harvest beans available Oct-Dec. Take home your own roasted batch.", 35.00, 1)
                );
            default:
                return List.of(
                    mockActivity(dest + " City Sightseeing Bus", cat, dest, "Hop-on hop-off bus tour of top attractions", "sightseeing,bus,landmarks", "8:30 AM - 6:30 PM (buses every 20 min)", "Daily. Open-top bus — bring sunscreen in summer, jacket in winter. Night tour available Fri-Sat.", 35.00, 1),
                    mockActivity(dest + " Sunset Cruise", cat, dest, "Scenic sunset boat cruise with drinks", "sightseeing,boat,evening", "Departure 1.5h before sunset (varies by season)", "Weather dependent. Best Apr-Oct. Cancelled if wind > 25 knots. Complimentary drink included.", 65.00, 0),
                    mockActivity(dest + " Photography Walk", cat, dest, "Guided photo walk through scenic spots", "sightseeing,photography,walking", "6:00 AM - 9:00 AM (golden hour) / 4:30 PM - 7:00 PM (blue hour)", "Golden hour timing shifts by season. Bring your own camera. All skill levels welcome.", 30.00, 0),
                    mockActivity(dest + " Hot Air Balloon Ride", cat, dest, "Aerial views of the city at sunrise", "sightseeing,aerial,sunrise", "5:30 AM - 7:30 AM (weather permitting)", "Best Oct-Mar for calm winds. No flights during monsoon. Must book 1 week in advance.", 150.00, 2),
                    mockActivity(dest + " Night Market Tour", cat, dest, "Explore vibrant night bazaars", "sightseeing,shopping,nightlife", "7:00 PM - 11:00 PM (peak hours 8-10 PM)", "Markets run Thu-Sun. Best Oct-Dec holiday season. Bring cash — many vendors don't take cards.", 15.00, 1)
                );
        }
    }

    private ActivityResponse mockActivity(String name, ActivityCategory cat, String location,
                                           String desc, String tags, String hours, String seasonal, double cost, int priority) {
        List<String> tagList = Arrays.asList(tags.split(","));
        return ActivityResponse.builder()
                .name(name)
                .description(desc)
                .category(cat)
                .tags(tags)
                .tagList(tagList)
                .location(location)
                .openingHours(hours)
                .seasonalNotes(seasonal)
                .cost(java.math.BigDecimal.valueOf(cost))
                .currency("USD")
                .reservationStatus(ReservationStatus.PENDING)
                .priority(priority)
                .alternativeList(Collections.emptyList())
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Trip getTripWithOwnerCheck(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        boolean isOwner = trip.getCreatedBy().getId().equals(user.getId());
        boolean isEditor = collaboratorRepository.findByTripIdAndUserId(tripId, user.getId())
                .map(c -> c.getInvitationStatus() == InvitationStatus.ACCEPTED
                        && (c.getRole() == CollaboratorRole.OWNER || c.getRole() == CollaboratorRole.EDITOR))
                .orElse(false);
        if (!isOwner && !isEditor) {
            throw new UnauthorizedException("Access denied to this trip");
        }
        return trip;
    }

    private ActivityResponse mapToResponse(Activity a) {
        List<String> tagList = a.getTags() != null && !a.getTags().isBlank()
                ? Arrays.asList(a.getTags().split(","))
                : Collections.emptyList();
        List<String> altList = a.getAlternatives() != null && !a.getAlternatives().isBlank()
                ? Arrays.asList(a.getAlternatives().split(","))
                : Collections.emptyList();

        return ActivityResponse.builder()
                .id(a.getId())
                .tripId(a.getTrip().getId())
                .name(a.getName())
                .description(a.getDescription())
                .category(a.getCategory())
                .tags(a.getTags())
                .tagList(tagList)
                .location(a.getLocation())
                .address(a.getAddress())
                .activityDate(a.getActivityDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .openingHours(a.getOpeningHours())
                .seasonalNotes(a.getSeasonalNotes())
                .cost(a.getCost())
                .currency(a.getCurrency())
                .reservationStatus(a.getReservationStatus())
                .bookingReference(a.getBookingReference())
                .bookingUrl(a.getBookingUrl())
                .reminderAt(a.getReminderAt())
                .notes(a.getNotes())
                .alternatives(a.getAlternatives())
                .alternativeList(altList)
                .priority(a.getPriority())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
