package com.voyagecraft.dto.transport;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransportConflictResponse {
    private String severity;  // WARNING, ERROR
    private String type;      // OVERLAP, TIGHT_CONNECTION, IMPOSSIBLE_CONNECTION
    private String message;
    private Long segmentAId;
    private String segmentALabel;
    private Long segmentBId;
    private String segmentBLabel;
    private Long layoverMinutes; // negative means overlap
}
