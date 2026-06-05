package com.voyagecraft.service;

import com.voyagecraft.dto.itinerary.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.ActivityCategory;
import com.voyagecraft.enums.ItineraryStatus;
import com.voyagecraft.enums.TripPace;
import com.voyagecraft.exception.BadRequestException;
import com.voyagecraft.exception.ResourceNotFoundException;
import com.voyagecraft.exception.UnauthorizedException;
import com.voyagecraft.repository.ItineraryRepository;
import com.voyagecraft.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TripRepository tripRepository;

    // ── Mock Activity Catalog ──────────────────────────────────────────────
    // Each activity: {name, category, lat-offset, lon-offset, duration-mins, openHours, bestSeason, cost}
    private static final List<Object[]> ACTIVITY_CATALOG = Arrays.asList(
        new Object[]{"Morning Guided City Walk",    ActivityCategory.CULTURAL,    0.001,  0.001,  90,  "08:00-12:00", "All year",   5.0},
        new Object[]{"Local Breakfast at Café",     ActivityCategory.FOOD,       -0.001,  0.002,  60,  "07:00-11:00", "All year",   8.0},
        new Object[]{"Historic Old Town Visit",     ActivityCategory.ATTRACTION,  0.003, -0.001, 120,  "09:00-18:00", "Spring-Fall",15.0},
        new Object[]{"Scenic Viewpoint Hike",       ActivityCategory.NATURE,      0.010,  0.010, 150,  "06:00-18:00", "Spring-Fall",0.0},
        new Object[]{"Museum of Local Heritage",    ActivityCategory.CULTURAL,   -0.002,  0.003,  90,  "10:00-17:00", "All year",  12.0},
        new Object[]{"Street Food Market Tour",     ActivityCategory.FOOD,        0.002, -0.003,  60,  "11:00-22:00", "All year",  10.0},
        new Object[]{"Adventure Zip-lining",        ActivityCategory.ADVENTURE,   0.015,  0.015, 120,  "09:00-16:00", "Summer",    45.0},
        new Object[]{"Traditional Cooking Class",  ActivityCategory.CULTURAL,   -0.003, -0.002, 180,  "10:00-14:00", "All year",  35.0},
        new Object[]{"Evening Rooftop Dinner",      ActivityCategory.FOOD,        0.001,  0.004,  90,  "18:00-23:00", "All year",  30.0},
        new Object[]{"Local Art Gallery",           ActivityCategory.CULTURAL,   -0.001, -0.001,  60,  "10:00-19:00", "All year",   8.0},
        new Object[]{"Nature Park Cycling",         ActivityCategory.NATURE,      0.020, -0.010, 120,  "08:00-17:00", "Spring-Fall",18.0},
        new Object[]{"Water Sports & Beach",        ActivityCategory.ADVENTURE,   0.008,  0.020, 180,  "09:00-18:00", "Summer",    25.0},
        new Object[]{"Night Market Exploration",    ActivityCategory.SHOPPING,   -0.002,  0.005,  90,  "18:00-23:00", "All year",   0.0},
        new Object[]{"Sunset Boat Cruise",          ActivityCategory.LEISURE,     0.005, -0.005, 120,  "17:00-20:00", "All year",  40.0},
        new Object[]{"Local Handicraft Workshop",   ActivityCategory.CULTURAL,    0.002,  0.002, 120,  "10:00-17:00", "All year",  20.0}
    );

    // ── Pace Configuration ─────────────────────────────────────────────────
    private int getActivitiesPerDay(TripPace pace) {
        return switch (pace) {
            case RELAXED  -> 3;
            case STANDARD -> 5;
            case INTENSE  -> 7;
        };
    }

    private LocalTime getDayStartTime(TripPace pace) {
        return switch (pace) {
            case RELAXED  -> LocalTime.of(9, 0);
            case STANDARD -> LocalTime.of(8, 0);
            case INTENSE  -> LocalTime.of(7, 0);
        };
    }

    // ── Generate Itinerary ─────────────────────────────────────────────────
    @Transactional
    public ItineraryResponse generateItinerary(GenerateItineraryRequest request, User user) {
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + request.getTripId()));

        if (!trip.getCreatedBy().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the trip owner can generate itineraries");
        }

        int nextVersion = itineraryRepository.findMaxVersionNumberByTripId(trip.getId())
                .map(v -> v + 1)
                .orElse(1);

        String versionName = (request.getVersionName() != null && !request.getVersionName().isBlank())
                ? request.getVersionName()
                : "Version " + nextVersion;

        Itinerary itinerary = Itinerary.builder()
                .trip(trip)
                .versionName(versionName)
                .versionNumber(nextVersion)
                .status(ItineraryStatus.DRAFT)
                .pace(request.getPace())
                .generatedBy(user)
                .isActive(false)
                .notes("Auto-generated itinerary with " + request.getPace().name().toLowerCase() + " pace")
                .build();

        List<LocalDate> tripDates = buildDateRange(trip.getStartDate(), trip.getEndDate());
        List<TripDestination> destinations = trip.getDestinations().isEmpty()
                ? List.of()
                : trip.getDestinations();

        int activitiesPerDay = getActivitiesPerDay(request.getPace());

        for (int i = 0; i < tripDates.size(); i++) {
            LocalDate date = tripDates.get(i);
            TripDestination destForDay = destinations.isEmpty()
                    ? null
                    : destinations.get(Math.min(i / Math.max(1, tripDates.size() / destinations.size()), destinations.size() - 1));

            ItineraryDay day = ItineraryDay.builder()
                    .itinerary(itinerary)
                    .destination(destForDay)
                    .dayDate(date)
                    .dayIndex(i + 1)
                    .theme(generateDayTheme(i, request.getPace()))
                    .notes("Day " + (i + 1) + " activities")
                    .build();

            double baseLat = (destForDay != null && destForDay.getLatitude() != null) ? destForDay.getLatitude() : 0.0;
            double baseLon = (destForDay != null && destForDay.getLongitude() != null) ? destForDay.getLongitude() : 0.0;

            List<Object[]> selectedActivities = selectActivitiesAvoidingBacktrack(
                    activitiesPerDay, baseLat, baseLon, request.getPreferredCategories());

            LocalTime currentTime = getDayStartTime(request.getPace());
            for (int j = 0; j < selectedActivities.size(); j++) {
                Object[] act = selectedActivities.get(j);
                int duration = (Integer) act[4];
                // Add 15-min buffer between activities
                int bufferMins = (j > 0) ? 15 : 0;
                currentTime = currentTime.plusMinutes(bufferMins);

                ItineraryItem item = ItineraryItem.builder()
                        .itineraryDay(day)
                        .title((String) act[0])
                        .category((ActivityCategory) act[1])
                        .latitude(baseLat + (Double) act[2])
                        .longitude(baseLon + (Double) act[3])
                        .durationMinutes(duration)
                        .startTime(currentTime)
                        .endTime(currentTime.plusMinutes(duration))
                        .openingHours((String) act[5])
                        .bestSeason((String) act[6])
                        .costEstimate(BigDecimal.valueOf((Double) act[7]))
                        .locationName((String) act[0])
                        .description("Enjoy " + act[0] + " during your visit. Best season: " + act[6])
                        .orderIndex(j)
                        .build();

                currentTime = currentTime.plusMinutes(duration);
                day.getItems().add(item);
            }
            itinerary.getDays().add(day);
        }

        Itinerary saved = itineraryRepository.save(itinerary);
        log.info("Generated itinerary '{}' (v{}) for trip ID={}", versionName, nextVersion, trip.getId());
        return mapToFullResponse(saved);
    }

    // ── Get All Versions (History) ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ItinerarySummaryResponse> getVersionHistory(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + tripId));
        if (!trip.getCreatedBy().getId().equals(user.getId())) {
            throw new UnauthorizedException("Access denied");
        }
        return itineraryRepository.findByTripIdOrderByVersionNumberAsc(tripId).stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());
    }

    // ── Get Single Itinerary ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ItineraryResponse getItinerary(Long itineraryId, User user) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found with id: " + itineraryId));
        if (!itinerary.getTrip().getCreatedBy().getId().equals(user.getId())) {
            throw new UnauthorizedException("Access denied");
        }
        return mapToFullResponse(itinerary);
    }

    // ── Activate a Version ─────────────────────────────────────────────────
    @Transactional
    public ItineraryResponse activateItinerary(Long itineraryId, User user) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found with id: " + itineraryId));
        if (!itinerary.getTrip().getCreatedBy().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the trip owner can activate an itinerary");
        }

        // Deactivate all versions for this trip
        itineraryRepository.findByTripIdOrderByVersionNumberAsc(itinerary.getTrip().getId())
                .forEach(it -> {
                    it.setIsActive(false);
                    it.setStatus(ItineraryStatus.ARCHIVED);
                });

        itinerary.setIsActive(true);
        itinerary.setStatus(ItineraryStatus.ACTIVE);
        Itinerary saved = itineraryRepository.save(itinerary);
        log.info("Activated itinerary v{} for trip ID={}", itinerary.getVersionNumber(), itinerary.getTrip().getId());
        return mapToFullResponse(saved);
    }

    // ── Delete a Version ──────────────────────────────────────────────────
    @Transactional
    public void deleteItinerary(Long itineraryId, User user) {
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found with id: " + itineraryId));
        if (!itinerary.getTrip().getCreatedBy().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the trip owner can delete an itinerary");
        }
        if (Boolean.TRUE.equals(itinerary.getIsActive())) {
            throw new BadRequestException("Cannot delete the active itinerary. Please activate another version first.");
        }
        itineraryRepository.delete(itinerary);
    }

    // ── Compare Two Versions ───────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ItineraryCompareResponse compareVersions(Long idA, Long idB, User user) {
        Itinerary a = itineraryRepository.findById(idA)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found: " + idA));
        Itinerary b = itineraryRepository.findById(idB)
                .orElseThrow(() -> new ResourceNotFoundException("Itinerary not found: " + idB));

        if (!a.getTrip().getId().equals(b.getTrip().getId())) {
            throw new BadRequestException("Both itineraries must belong to the same trip");
        }
        if (!a.getTrip().getCreatedBy().getId().equals(user.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        int totalA = a.getDays().stream().mapToInt(d -> d.getItems().size()).sum();
        int totalB = b.getDays().stream().mapToInt(d -> d.getItems().size()).sum();

        return ItineraryCompareResponse.builder()
                .versionA(mapToSummary(a))
                .versionB(mapToSummary(b))
                .daysA(a.getDays().stream().map(this::mapDayToResponse).collect(Collectors.toList()))
                .daysB(b.getDays().stream().map(this::mapDayToResponse).collect(Collectors.toList()))
                .paceComparison(a.getPace().name() + " vs " + b.getPace().name())
                .activityDifference(totalA - totalB)
                .build();
    }

    // ── Internal Helpers ───────────────────────────────────────────────────

    private List<LocalDate> buildDateRange(LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }

    /**
     * Selects activities using a nearest-neighbor greedy approach to avoid backtracking.
     * Activities are sorted by distance from the previous one so the route is optimized.
     */
    private List<Object[]> selectActivitiesAvoidingBacktrack(int count, double baseLat, double baseLon,
                                                              List<String> preferredCategories) {
        List<Object[]> pool = new ArrayList<>(ACTIVITY_CATALOG);
        Collections.shuffle(pool, new Random());

        // Filter by preferred categories if specified
        if (preferredCategories != null && !preferredCategories.isEmpty()) {
            List<Object[]> preferred = pool.stream()
                    .filter(a -> preferredCategories.contains(((ActivityCategory) a[1]).name()))
                    .collect(Collectors.toList());
            if (preferred.size() >= count / 2) {
                pool = preferred;
            }
        }

        // Greedy nearest-neighbor to minimize backtracking
        List<Object[]> selected = new ArrayList<>();
        double currentLat = baseLat;
        double currentLon = baseLon;

        while (selected.size() < count && !pool.isEmpty()) {
            final double cLat = currentLat;
            final double cLon = currentLon;

            Object[] nearest = pool.stream()
                    .min(Comparator.comparingDouble(a -> distance(cLat, cLon,
                            cLat + (Double) a[2], cLon + (Double) a[3])))
                    .orElseThrow();

            selected.add(nearest);
            pool.remove(nearest);
            currentLat = currentLat + (Double) nearest[2];
            currentLon = currentLon + (Double) nearest[3];
        }
        return selected;
    }

    // Simple Euclidean distance for mock map constraint
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        return Math.sqrt(Math.pow(lat2 - lat1, 2) + Math.pow(lon2 - lon1, 2));
    }

    private String generateDayTheme(int dayIndex, TripPace pace) {
        String[] relaxedThemes  = {"Slow Morning & Local Vibes", "Leisure & Culture", "Rest & Explore"};
        String[] standardThemes = {"City Highlights", "Culture & Food", "Hidden Gems", "Nature & Adventure"};
        String[] intenseThemes  = {"Full-Day Explorer", "Landmark Blitz", "Adventure & Culture Packed", "Maximum Sightseeing"};

        String[] themes = switch (pace) {
            case RELAXED  -> relaxedThemes;
            case STANDARD -> standardThemes;
            case INTENSE  -> intenseThemes;
        };
        return themes[dayIndex % themes.length];
    }

    // ── Mappers ────────────────────────────────────────────────────────────

    private ItineraryResponse mapToFullResponse(Itinerary it) {
        int totalActivities = it.getDays().stream().mapToInt(d -> d.getItems().size()).sum();
        return ItineraryResponse.builder()
                .id(it.getId())
                .tripId(it.getTrip().getId())
                .tripTitle(it.getTrip().getTitle())
                .versionName(it.getVersionName())
                .versionNumber(it.getVersionNumber())
                .status(it.getStatus())
                .pace(it.getPace())
                .isActive(it.getIsActive())
                .notes(it.getNotes())
                .createdAt(it.getCreatedAt())
                .updatedAt(it.getUpdatedAt())
                .days(it.getDays().stream().map(this::mapDayToResponse).collect(Collectors.toList()))
                .totalDays(it.getDays().size())
                .totalActivities(totalActivities)
                .build();
    }

    private ItinerarySummaryResponse mapToSummary(Itinerary it) {
        int totalActivities = it.getDays().stream().mapToInt(d -> d.getItems().size()).sum();
        return ItinerarySummaryResponse.builder()
                .id(it.getId())
                .versionName(it.getVersionName())
                .versionNumber(it.getVersionNumber())
                .status(it.getStatus())
                .pace(it.getPace())
                .isActive(it.getIsActive())
                .totalDays(it.getDays().size())
                .totalActivities(totalActivities)
                .createdAt(it.getCreatedAt())
                .build();
    }

    private ItineraryDayResponse mapDayToResponse(ItineraryDay day) {
        return ItineraryDayResponse.builder()
                .id(day.getId())
                .dayIndex(day.getDayIndex())
                .dayDate(day.getDayDate())
                .theme(day.getTheme())
                .notes(day.getNotes())
                .destinationName(day.getDestination() != null ? day.getDestination().getDestinationName() : null)
                .items(day.getItems().stream().map(this::mapItemToResponse).collect(Collectors.toList()))
                .build();
    }

    private ItineraryItemResponse mapItemToResponse(ItineraryItem item) {
        return ItineraryItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .locationName(item.getLocationName())
                .locationAddress(item.getLocationAddress())
                .latitude(item.getLatitude())
                .longitude(item.getLongitude())
                .costEstimate(item.getCostEstimate())
                .category(item.getCategory())
                .orderIndex(item.getOrderIndex())
                .openingHours(item.getOpeningHours())
                .bestSeason(item.getBestSeason())
                .durationMinutes(item.getDurationMinutes())
                .build();
    }
}
