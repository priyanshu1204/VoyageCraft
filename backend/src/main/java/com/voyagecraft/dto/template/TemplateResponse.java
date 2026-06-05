package com.voyagecraft.dto.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TemplateResponse {
    private Long id;
    private String name;
    private String description;
    private String destinationsJson;
    private Integer durationDays;
    private BigDecimal budgetEstimate;
    private String category;
    private String coverImageUrl;
}
