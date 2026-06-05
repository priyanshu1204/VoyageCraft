package com.voyagecraft.repository;

import com.voyagecraft.entity.WeatherForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherForecastRepository extends JpaRepository<WeatherForecast, Long> {

    List<WeatherForecast> findByTripIdOrderByForecastDateAsc(Long tripId);

    List<WeatherForecast> findByTripIdAndLocationNameOrderByForecastDateAsc(Long tripId, String locationName);

    Optional<WeatherForecast> findByTripIdAndLocationNameAndForecastDate(Long tripId, String locationName, LocalDate forecastDate);

    List<WeatherForecast> findByTripIdAndForecastDate(Long tripId, LocalDate forecastDate);

    @Query("SELECT w FROM WeatherForecast w WHERE w.trip.id = :tripId AND w.isAlert = true ORDER BY w.forecastDate ASC")
    List<WeatherForecast> findAlertsByTripId(@Param("tripId") Long tripId);

    @Query("SELECT w FROM WeatherForecast w WHERE w.trip.id = :tripId AND w.forecastDate BETWEEN :startDate AND :endDate ORDER BY w.forecastDate ASC")
    List<WeatherForecast> findByTripIdAndDateRange(@Param("tripId") Long tripId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    void deleteByTripId(Long tripId);
}
