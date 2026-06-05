package com.voyagecraft.dto.budget;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DailySpendResponse {
    private LocalDate date;
    private BigDecimal amount;
}
