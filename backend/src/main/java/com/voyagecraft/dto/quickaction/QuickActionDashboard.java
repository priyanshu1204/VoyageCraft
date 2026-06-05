package com.voyagecraft.dto.quickaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickActionDashboard {

    private Long tripId;
    private String tripTitle;

    // Today's schedule items
    private List<ScheduleItem> todaySchedule;

    // Upcoming check-ins
    private List<CheckInItem> upcomingCheckIns;

    // Quick notes
    private List<QuickNoteResponse> recentNotes;

    // Unread notification count
    private int unreadNotifications;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleItem {
        private Long id;
        private String title;
        private String location;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer orderIndex;
        private String category;
        private String type; // ITINERARY_ITEM, ACTIVITY, TRANSPORT
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckInItem {
        private Long id;
        private String type; // FLIGHT, STAY
        private String title;
        private String subtitle;
        private String bookingReference;
        private LocalDateTime dateTime;
        private Boolean checkedIn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReorderRequest {
        private Long dayId;
        private List<Long> itemIds; // ordered list of itinerary item IDs
    }
}
