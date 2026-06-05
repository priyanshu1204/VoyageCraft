package com.voyagecraft.service;

import com.voyagecraft.dto.transport.*;
import com.voyagecraft.entity.TransportDetail;
import com.voyagecraft.entity.Trip;
import com.voyagecraft.entity.User;
import com.voyagecraft.enums.TransportType;
import com.voyagecraft.exception.BadRequestException;
import com.voyagecraft.exception.ResourceNotFoundException;
import com.voyagecraft.exception.UnauthorizedException;
import com.voyagecraft.repository.TransportDetailRepository;
import com.voyagecraft.repository.TripCollaboratorRepository;
import com.voyagecraft.repository.TripRepository;
import com.voyagecraft.enums.CollaboratorRole;
import com.voyagecraft.enums.InvitationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransportService {

    private final TransportDetailRepository transportRepository;
    private final TripRepository tripRepository;
    private final TripCollaboratorRepository collaboratorRepository;

    // ── Minimum connection times (minutes) per transport type ───────────
    private static final Map<TransportType, Long> MIN_CONNECTION_MINS = Map.of(
            TransportType.FLIGHT, 120L,
            TransportType.TRAIN, 60L,
            TransportType.BUS, 45L,
            TransportType.CAR_RENTAL, 30L
    );

    // ── CRUD ────────────────────────────────────────────────────────────

    @Transactional
    public TransportResponse addTransport(TransportRequest request, User user) {
        Trip trip = getTripWithOwnerCheck(request.getTripId(), user);

        LocalDateTime depTime = LocalDateTime.parse(request.getDepartureTime());
        LocalDateTime arrTime = LocalDateTime.parse(request.getArrivalTime());

        if (!arrTime.isAfter(depTime)) {
            throw new BadRequestException("Arrival time must be after departure time");
        }

        TransportDetail detail = TransportDetail.builder()
                .trip(trip)
                .type(request.getType())
                .provider(request.getProvider())
                .flightNumber(request.getFlightNumber())
                .departureLocation(request.getDepartureLocation())
                .arrivalLocation(request.getArrivalLocation())
                .departureTime(depTime)
                .arrivalTime(arrTime)
                .departureTimezone(request.getDepartureTimezone() != null ? request.getDepartureTimezone() : "UTC")
                .arrivalTimezone(request.getArrivalTimezone() != null ? request.getArrivalTimezone() : "UTC")
                .bookingReference(request.getBookingReference())
                .cost(request.getCost())
                .notes(request.getNotes())
                .build();

        TransportDetail saved = transportRepository.save(detail);
        log.info("Added {} segment '{}' for trip ID={}", request.getType(), request.getDepartureLocation() + " → " + request.getArrivalLocation(), trip.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public TransportResponse updateTransport(Long transportId, TransportRequest request, User user) {
        TransportDetail detail = transportRepository.findById(transportId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport segment not found: " + transportId));
        getTripWithOwnerCheck(detail.getTrip().getId(), user);

        LocalDateTime depTime = LocalDateTime.parse(request.getDepartureTime());
        LocalDateTime arrTime = LocalDateTime.parse(request.getArrivalTime());

        if (!arrTime.isAfter(depTime)) {
            throw new BadRequestException("Arrival time must be after departure time");
        }

        detail.setType(request.getType());
        detail.setProvider(request.getProvider());
        detail.setFlightNumber(request.getFlightNumber());
        detail.setDepartureLocation(request.getDepartureLocation());
        detail.setArrivalLocation(request.getArrivalLocation());
        detail.setDepartureTime(depTime);
        detail.setArrivalTime(arrTime);
        detail.setDepartureTimezone(request.getDepartureTimezone() != null ? request.getDepartureTimezone() : "UTC");
        detail.setArrivalTimezone(request.getArrivalTimezone() != null ? request.getArrivalTimezone() : "UTC");
        detail.setBookingReference(request.getBookingReference());
        detail.setCost(request.getCost());
        detail.setNotes(request.getNotes());

        return mapToResponse(transportRepository.save(detail));
    }

    @Transactional
    public void deleteTransport(Long transportId, User user) {
        TransportDetail detail = transportRepository.findById(transportId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport segment not found: " + transportId));
        getTripWithOwnerCheck(detail.getTrip().getId(), user);
        transportRepository.delete(detail);
    }

    @Transactional(readOnly = true)
    public List<TransportResponse> getTripTransports(Long tripId, User user) {
        getTripWithOwnerCheck(tripId, user);
        return transportRepository.findByTripIdOrderByDepartureTimeAsc(tripId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransportResponse getTransport(Long transportId, User user) {
        TransportDetail detail = transportRepository.findById(transportId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport segment not found: " + transportId));
        getTripWithOwnerCheck(detail.getTrip().getId(), user);
        return mapToResponse(detail);
    }

    // ── Conflict Detection ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TransportConflictResponse> detectConflicts(Long tripId, User user) {
        getTripWithOwnerCheck(tripId, user);
        List<TransportDetail> segments = transportRepository.findByTripIdOrderByDepartureTimeAsc(tripId);

        if (segments.size() < 2) return Collections.emptyList();

        List<TransportConflictResponse> conflicts = new ArrayList<>();

        for (int i = 0; i < segments.size() - 1; i++) {
            TransportDetail current = segments.get(i);
            TransportDetail next = segments.get(i + 1);

            // Convert to UTC-equivalent instants for accurate comparison across timezones
            ZonedDateTime currentArrivalUtc = current.getArrivalTime()
                    .atZone(ZoneId.of(current.getArrivalTimezone()))
                    .withZoneSameInstant(ZoneOffset.UTC);

            ZonedDateTime nextDepartureUtc = next.getDepartureTime()
                    .atZone(ZoneId.of(next.getDepartureTimezone()))
                    .withZoneSameInstant(ZoneOffset.UTC);

            long layoverMinutes = ChronoUnit.MINUTES.between(currentArrivalUtc, nextDepartureUtc);

            String labelA = formatSegmentLabel(current);
            String labelB = formatSegmentLabel(next);

            if (layoverMinutes < 0) {
                // Overlapping segments
                conflicts.add(TransportConflictResponse.builder()
                        .severity("ERROR")
                        .type("OVERLAP")
                        .message("Schedule conflict: \"" + labelA + "\" overlaps with \"" + labelB + "\" by " + Math.abs(layoverMinutes) + " minutes")
                        .segmentAId(current.getId())
                        .segmentALabel(labelA)
                        .segmentBId(next.getId())
                        .segmentBLabel(labelB)
                        .layoverMinutes(layoverMinutes)
                        .build());
            } else {
                long minRequired = MIN_CONNECTION_MINS.getOrDefault(next.getType(), 60L);
                if (layoverMinutes < minRequired) {
                    String severity = layoverMinutes < minRequired / 2 ? "ERROR" : "WARNING";
                    String type = layoverMinutes < 15 ? "IMPOSSIBLE_CONNECTION" : "TIGHT_CONNECTION";
                    conflicts.add(TransportConflictResponse.builder()
                            .severity(severity)
                            .type(type)
                            .message("Tight connection: only " + layoverMinutes + " min layover between \"" + labelA + "\" and \"" + labelB + "\" (minimum recommended: " + minRequired + " min for " + next.getType().name().toLowerCase() + ")")
                            .segmentAId(current.getId())
                            .segmentALabel(labelA)
                            .segmentBId(next.getId())
                            .segmentBLabel(labelB)
                            .layoverMinutes(layoverMinutes)
                            .build());
                }
            }
        }

        return conflicts;
    }

    // ── Mock Transport Search ───────────────────────────────────────────

    public List<MockTransportOption> searchMockTransport(
            TransportType type, String from, String to, String date,
            String fromTimezone, String toTimezone) {

        LocalDate travelDate = LocalDate.parse(date);
        String depTz = fromTimezone != null ? fromTimezone : "UTC";
        String arrTz = toTimezone != null ? toTimezone : "UTC";

        List<MockTransportOption> options = new ArrayList<>();
        Random rand = new Random(from.hashCode() + to.hashCode() + travelDate.hashCode());

        String[] flightProviders = {"VoyageAir", "SkyConnect", "AeroSwift", "GlobalWings"};
        String[] trainProviders  = {"RailExpress", "EuroRail", "SpeedTrain"};
        String[] busProviders    = {"GoBus", "TransitLink", "ComfortLine"};

        String[] providers = switch (type) {
            case FLIGHT -> flightProviders;
            case TRAIN  -> trainProviders;
            case BUS    -> busProviders;
            default     -> new String[]{"AutoRent", "DriveEasy"};
        };

        int count = type == TransportType.CAR_RENTAL ? 3 : 5;

        for (int i = 0; i < count; i++) {
            int depHour = 6 + rand.nextInt(14);     // 06:00 – 19:59
            int depMin  = rand.nextInt(4) * 15;     // 0, 15, 30, 45

            int durationMins = switch (type) {
                case FLIGHT     -> 90 + rand.nextInt(300);    // 1.5h – 6.5h
                case TRAIN      -> 120 + rand.nextInt(360);   // 2h – 8h
                case BUS        -> 180 + rand.nextInt(480);   // 3h – 11h
                case CAR_RENTAL -> 60 + rand.nextInt(300);    // 1h – 6h
            };

            LocalDateTime depTime = travelDate.atTime(depHour, depMin);
            LocalDateTime arrTime = depTime.plusMinutes(durationMins);

            double baseCost = switch (type) {
                case FLIGHT     -> 80 + rand.nextInt(400);
                case TRAIN      -> 30 + rand.nextInt(150);
                case BUS        -> 10 + rand.nextInt(60);
                case CAR_RENTAL -> 40 + rand.nextInt(100);
            };

            String fltNum = type == TransportType.FLIGHT
                    ? (providers[rand.nextInt(providers.length)].substring(0, 2).toUpperCase() + (100 + rand.nextInt(900)))
                    : null;

            int stops = type == TransportType.FLIGHT ? rand.nextInt(2) : (type == TransportType.BUS ? rand.nextInt(4) : 0);

            options.add(MockTransportOption.builder()
                    .type(type)
                    .provider(providers[rand.nextInt(providers.length)])
                    .flightNumber(fltNum)
                    .departureLocation(from)
                    .arrivalLocation(to)
                    .departureTime(depTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .arrivalTime(arrTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .departureTimezone(depTz)
                    .arrivalTimezone(arrTz)
                    .durationMinutes((long) durationMins)
                    .cost(BigDecimal.valueOf(baseCost))
                    .stops(stops)
                    .build());
        }

        options.sort(Comparator.comparing(MockTransportOption::getDepartureTime));
        return options;
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

    private String formatSegmentLabel(TransportDetail d) {
        return d.getType().name() + " " + d.getDepartureLocation() + " → " + d.getArrivalLocation()
                + (d.getFlightNumber() != null ? " (" + d.getFlightNumber() + ")" : "");
    }

    private TransportResponse mapToResponse(TransportDetail d) {
        long durationMins = ChronoUnit.MINUTES.between(d.getDepartureTime(), d.getArrivalTime());

        // Timezone-adjusted display strings
        ZonedDateTime depZoned = d.getDepartureTime().atZone(ZoneId.of(d.getDepartureTimezone()));
        ZonedDateTime arrZoned = d.getArrivalTime().atZone(ZoneId.of(d.getArrivalTimezone()));
        DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("MMM d, h:mm a (z)");

        return TransportResponse.builder()
                .id(d.getId())
                .tripId(d.getTrip().getId())
                .type(d.getType())
                .provider(d.getProvider())
                .flightNumber(d.getFlightNumber())
                .departureLocation(d.getDepartureLocation())
                .arrivalLocation(d.getArrivalLocation())
                .departureTime(d.getDepartureTime())
                .arrivalTime(d.getArrivalTime())
                .departureTimezone(d.getDepartureTimezone())
                .arrivalTimezone(d.getArrivalTimezone())
                .bookingReference(d.getBookingReference())
                .cost(d.getCost())
                .notes(d.getNotes())
                .durationMinutes(durationMins)
                .departureTimeFormatted(depZoned.format(displayFmt))
                .arrivalTimeFormatted(arrZoned.format(displayFmt))
                .createdAt(d.getCreatedAt())
                .build();
    }
}
