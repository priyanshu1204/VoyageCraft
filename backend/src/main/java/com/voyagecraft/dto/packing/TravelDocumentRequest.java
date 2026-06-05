package com.voyagecraft.dto.packing;

import com.voyagecraft.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TravelDocumentRequest {
    @NotNull private Long tripId;
    @NotNull private DocumentType documentType;
    @NotBlank private String title;
    private String documentNumber;
    private String issuingCountry;
    private String issueDate;
    private String expiryDate;
    private String fileUrl;
    private String notes;
}
