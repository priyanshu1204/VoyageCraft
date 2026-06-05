package com.voyagecraft.service;

import com.voyagecraft.dto.weather.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.*;
import com.voyagecraft.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WeatherServiceTest {

    @InjectMocks private WeatherService service;
    @Mock private WeatherForecastRepository forecastRepository;
    @Mock private TripRepository tripRepository;
    @Mock private TripCollaboratorRepository collaboratorRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private ObjectMapper objectMapper;

    private User testUser;
    private Trip testTrip;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("u@t.com").firstName("J").lastName("D").build();
        testTrip = Trip.builder().id(1L).title("Trip").createdBy(testUser)
                .startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(10))
                .createdAt(LocalDateTime.now()).build();
        testTrip.setDestinations(new ArrayList<>());
    }

    // ── Seasonal Advice (all seasons) ──
    @Test void getSeasonalAdvice_summer() {
        SeasonalAdvice a = service.getSeasonalAdvice("Paris", LocalDate.of(2026, 5, 15));
        assertNotNull(a); assertNotNull(a.getSeason()); assertNotNull(a.getAdvice());
    }
    @Test void getSeasonalAdvice_monsoon() {
        SeasonalAdvice a = service.getSeasonalAdvice("Mumbai", LocalDate.of(2026, 7, 15));
        assertNotNull(a);
    }
    @Test void getSeasonalAdvice_winter() {
        SeasonalAdvice a = service.getSeasonalAdvice("Tokyo", LocalDate.of(2026, 1, 10));
        assertNotNull(a);
    }
    @Test void getSeasonalAdvice_spring() {
        SeasonalAdvice a = service.getSeasonalAdvice("London", LocalDate.of(2026, 3, 15));
        assertNotNull(a);
    }
    @Test void getSeasonalAdvice_autumn() {
        SeasonalAdvice a = service.getSeasonalAdvice("NYC", LocalDate.of(2026, 10, 15));
        assertNotNull(a);
    }

    // ── Generate Forecast ──
    @Test void generateForecast_emptyDestinations() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        List<WeatherForecastResponse> result = service.generateForecast(1L, testUser);
        assertNotNull(result); assertTrue(result.isEmpty());
    }

    @Test void generateForecast_withDestination_apiFailsFallsBackToMock() {
        TripDestination dest = TripDestination.builder().id(1L).destinationName("Paris").country("France").build();
        testTrip.setDestinations(List.of(dest));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("API down"));
        when(forecastRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<WeatherForecastResponse> result = service.generateForecast(1L, testUser);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test void generateForecast_tripNotFound() {
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.generateForecast(99L, testUser));
    }

    @Test void generateForecast_noAccess() {
        User other = User.builder().id(2L).email("o@t.com").firstName("X").lastName("Y").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(collaboratorRepository.findByTripIdAndUserId(1L, 2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.generateForecast(1L, other));
    }

    // ── Get Trip Forecasts ──
    @Test void getTripForecasts_success() {
        WeatherForecast f = WeatherForecast.builder().id(1L).trip(testTrip).locationName("Paris")
                .forecastDate(LocalDate.now().plusDays(5)).condition(WeatherCondition.SUNNY)
                .temperatureHigh(28.0).temperatureLow(18.0).humidityPercent(45)
                .precipitationChance(10).windSpeedKmh(12.0).description("Clear")
                .recommendation("Sunscreen").isAlert(false).build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(forecastRepository.findByTripIdOrderByForecastDateAsc(1L)).thenReturn(List.of(f));
        List<WeatherForecastResponse> result = service.getTripForecasts(1L, testUser);
        assertEquals(1, result.size());
    }

    // ── Get Forecast By Date ──
    @Test void getForecastByDate_success() {
        LocalDate date = LocalDate.now().plusDays(5);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(forecastRepository.findByTripIdAndForecastDate(1L, date)).thenReturn(Collections.emptyList());
        List<WeatherForecastResponse> result = service.getForecastByDate(1L, date, testUser);
        assertTrue(result.isEmpty());
    }

    // ── Weather Alerts ──
    @Test void getWeatherAlerts_success() {
        WeatherForecast alert = WeatherForecast.builder().id(1L).trip(testTrip).locationName("Delhi")
                .forecastDate(LocalDate.now().plusDays(5)).condition(WeatherCondition.THUNDERSTORM)
                .temperatureHigh(35.0).temperatureLow(25.0).humidityPercent(85)
                .precipitationChance(90).windSpeedKmh(40.0).description("Storm")
                .recommendation("Stay indoors").isAlert(true).alertMessage("Severe thunderstorm").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(forecastRepository.findAlertsByTripId(1L)).thenReturn(List.of(alert));
        List<WeatherAlertResponse> result = service.getWeatherAlerts(1L, testUser);
        assertFalse(result.isEmpty());
    }

    @Test void getWeatherAlerts_empty() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(forecastRepository.findAlertsByTripId(1L)).thenReturn(Collections.emptyList());
        List<WeatherAlertResponse> result = service.getWeatherAlerts(1L, testUser);
        assertTrue(result.isEmpty());
    }

    // ── Trip Seasonal Advice ──
    @Test void getTripSeasonalAdvice_success() {
        TripDestination d = TripDestination.builder().id(1L).destinationName("Rome").country("Italy").build();
        testTrip.setDestinations(List.of(d));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        List<SeasonalAdvice> result = service.getTripSeasonalAdvice(1L, testUser);
        assertEquals(1, result.size());
    }

    @Test void getTripSeasonalAdvice_multipleDestinations() {
        TripDestination d1 = TripDestination.builder().id(1L).destinationName("Rome").build();
        TripDestination d2 = TripDestination.builder().id(2L).destinationName("Paris").build();
        testTrip.setDestinations(List.of(d1, d2));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        List<SeasonalAdvice> result = service.getTripSeasonalAdvice(1L, testUser);
        assertEquals(2, result.size());
    }

    // ── Mock forecasts for different seasons ──
    @Test void generateForecast_winterSeason() {
        TripDestination dest = TripDestination.builder().id(1L).destinationName("Moscow").build();
        Trip winterTrip = Trip.builder().id(2L).title("Winter").createdBy(testUser)
                .startDate(LocalDate.of(2027, 1, 10)).endDate(LocalDate.of(2027, 1, 12))
                .createdAt(LocalDateTime.now()).build();
        winterTrip.setDestinations(List.of(dest));
        when(tripRepository.findById(2L)).thenReturn(Optional.of(winterTrip));
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("API"));
        when(forecastRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        var r = service.generateForecast(2L, testUser);
        assertNotNull(r); assertFalse(r.isEmpty());
    }

    @Test void generateForecast_springSeason() {
        TripDestination dest = TripDestination.builder().id(1L).destinationName("Tokyo").build();
        Trip springTrip = Trip.builder().id(3L).title("Spring").createdBy(testUser)
                .startDate(LocalDate.of(2027, 3, 20)).endDate(LocalDate.of(2027, 3, 22))
                .createdAt(LocalDateTime.now()).build();
        springTrip.setDestinations(List.of(dest));
        when(tripRepository.findById(3L)).thenReturn(Optional.of(springTrip));
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("API"));
        when(forecastRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        var r = service.generateForecast(3L, testUser);
        assertNotNull(r); assertFalse(r.isEmpty());
    }

    @Test void generateForecast_monsoonSeason() {
        TripDestination dest = TripDestination.builder().id(1L).destinationName("Mumbai").build();
        Trip monsoonTrip = Trip.builder().id(4L).title("Monsoon").createdBy(testUser)
                .startDate(LocalDate.of(2027, 8, 1)).endDate(LocalDate.of(2027, 8, 3))
                .createdAt(LocalDateTime.now()).build();
        monsoonTrip.setDestinations(List.of(dest));
        when(tripRepository.findById(4L)).thenReturn(Optional.of(monsoonTrip));
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("API"));
        when(forecastRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        var r = service.generateForecast(4L, testUser);
        assertNotNull(r); assertFalse(r.isEmpty());
    }

    @Test void generateForecast_autumnSeason() {
        TripDestination dest = TripDestination.builder().id(1L).destinationName("NYC").build();
        Trip autumnTrip = Trip.builder().id(5L).title("Autumn").createdBy(testUser)
                .startDate(LocalDate.of(2027, 10, 15)).endDate(LocalDate.of(2027, 10, 17))
                .createdAt(LocalDateTime.now()).build();
        autumnTrip.setDestinations(List.of(dest));
        when(tripRepository.findById(5L)).thenReturn(Optional.of(autumnTrip));
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("API"));
        when(forecastRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        var r = service.generateForecast(5L, testUser);
        assertNotNull(r); assertFalse(r.isEmpty());
    }

    // ── Alerts with different conditions ──
    @Test void getWeatherAlerts_hot() {
        WeatherForecast f = WeatherForecast.builder().id(2L).trip(testTrip).locationName("Delhi")
                .forecastDate(LocalDate.now().plusDays(5)).condition(WeatherCondition.HOT)
                .temperatureHigh(45.0).temperatureLow(30.0).humidityPercent(30)
                .precipitationChance(5).windSpeedKmh(10.0).description("Hot")
                .recommendation("Hydrate").isAlert(true).alertMessage("Heat").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(forecastRepository.findAlertsByTripId(1L)).thenReturn(List.of(f));
        var r = service.getWeatherAlerts(1L, testUser);
        assertFalse(r.isEmpty());
    }

    @Test void getWeatherAlerts_snow() {
        WeatherForecast f = WeatherForecast.builder().id(3L).trip(testTrip).locationName("Alps")
                .forecastDate(LocalDate.now().plusDays(5)).condition(WeatherCondition.SNOWY)
                .temperatureHigh(-2.0).temperatureLow(-10.0).humidityPercent(70)
                .precipitationChance(80).windSpeedKmh(25.0).description("Snow")
                .recommendation("Bundle up").isAlert(true).alertMessage("Snow").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(forecastRepository.findAlertsByTripId(1L)).thenReturn(List.of(f));
        var r = service.getWeatherAlerts(1L, testUser);
        assertFalse(r.isEmpty());
    }

    @Test void getWeatherAlerts_heavyRain() {
        WeatherForecast f = WeatherForecast.builder().id(4L).trip(testTrip).locationName("Kerala")
                .forecastDate(LocalDate.now().plusDays(5)).condition(WeatherCondition.HEAVY_RAIN)
                .temperatureHigh(28.0).temperatureLow(22.0).humidityPercent(95)
                .precipitationChance(95).windSpeedKmh(30.0).description("Heavy rain")
                .recommendation("Indoors").isAlert(true).alertMessage("Flood").build();
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(forecastRepository.findAlertsByTripId(1L)).thenReturn(List.of(f));
        var r = service.getWeatherAlerts(1L, testUser);
        assertFalse(r.isEmpty());
    }

    // ── Multiple conditions in forecast response ──
    @Test void getTripForecasts_multipleConditions() {
        var conditions = new WeatherCondition[]{WeatherCondition.SUNNY, WeatherCondition.RAINY,
                WeatherCondition.CLOUDY, WeatherCondition.FOGGY, WeatherCondition.WINDY, WeatherCondition.COLD};
        List<WeatherForecast> forecasts = new ArrayList<>();
        for (int i = 0; i < conditions.length; i++) {
            forecasts.add(WeatherForecast.builder().id((long)(i+1)).trip(testTrip).locationName("City")
                    .forecastDate(LocalDate.now().plusDays(i)).condition(conditions[i])
                    .temperatureHigh(20.0+i).temperatureLow(10.0+i).humidityPercent(50)
                    .precipitationChance(20).windSpeedKmh(15.0).description("D")
                    .recommendation("R").isAlert(false).build());
        }
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(forecastRepository.findByTripIdOrderByForecastDateAsc(1L)).thenReturn(forecasts);
        var r = service.getTripForecasts(1L, testUser);
        assertEquals(conditions.length, r.size());
    }

    @Test void generateForecast_multipleDestinations() {
        TripDestination d1 = TripDestination.builder().id(1L).destinationName("Paris").build();
        TripDestination d2 = TripDestination.builder().id(2L).destinationName("London").build();
        testTrip.setDestinations(List.of(d1, d2));
        when(tripRepository.findById(1L)).thenReturn(Optional.of(testTrip));
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("API"));
        when(forecastRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
        var r = service.generateForecast(1L, testUser);
        assertNotNull(r); assertTrue(r.size() > 6); // 6+ days * 2 destinations
    }
}
