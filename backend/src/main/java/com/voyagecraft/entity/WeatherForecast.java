package com.voyagecraft.entity;

import com.voyagecraft.enums.WeatherCondition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_forecasts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"trip_id", "location_name", "forecast_date"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WeatherForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false, length = 200)
    private String locationName;

    @Column(nullable = false)
    private LocalDate forecastDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "weather_condition", nullable = false, length = 30)
    private WeatherCondition condition;

    private Double temperatureHigh; // Celsius

    private Double temperatureLow;

    private Integer humidityPercent;

    private Integer precipitationChance; // 0-100

    private Double windSpeedKmh;

    @Column(length = 500)
    private String description;

    @Column(length = 300)
    private String recommendation; // e.g., "Carry an umbrella"

    private Boolean isAlert; // true if extreme weather

    @Column(length = 500)
    private String alertMessage;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
