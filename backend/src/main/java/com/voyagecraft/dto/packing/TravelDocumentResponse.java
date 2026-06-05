package com.voyagecraft.dto.packing;

import com.voyagecraft.enums.DocumentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TravelDocumentResponse {
    private Long id;
    private Long tripId;
    private Long userId;
    private String userName;
    private DocumentType documentType;
    private String title;
    private String documentNumber;
    private String issuingCountry;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String fileUrl;
    private String notes;
    private Boolean isExpired;
    private Boolean isExpiringSoon;
    private Long daysUntilExpiry;
    private LocalDateTime createdAt;
}
