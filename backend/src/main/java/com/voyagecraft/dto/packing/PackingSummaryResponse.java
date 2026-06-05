package com.voyagecraft.dto.packing;

import com.voyagecraft.enums.PackingCategory;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PackingSummaryResponse {
    private Long tripId;
    private long totalItems;
    private long packedItems;
    private double packedPercent;
    private List<CategorySummary> categoryBreakdown;

    @Data
    @Builder
    public static class CategorySummary {
        private PackingCategory category;
        private long total;
        private long packed;
    }
}
