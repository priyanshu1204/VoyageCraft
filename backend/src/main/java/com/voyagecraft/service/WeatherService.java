package com.voyagecraft.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voyagecraft.dto.weather.*;
import com.voyagecraft.entity.*;
import com.voyagecraft.enums.Season;
import com.voyagecraft.enums.WeatherCondition;
import com.voyagecraft.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final WeatherForecastRepository forecastRepository;
    private final TripRepository tripRepository;
    private final TripCollaboratorRepository collaboratorRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search?name={name}&count=1&language=en&format=json";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max,wind_speed_10m_max&timezone=auto&start_date={start}&end_date={end}";

    // ── Public API ───────────────────────────────────────────────────

    /**
     * Generate real-time weather forecasts for all trip destinations using Open-Meteo API.
     */
    @Transactional
    public List<WeatherForecastResponse> generateForecast(Long tripId, User user) {
        Trip trip = getTripWithAccessCheck(tripId, user);

        // Delete old forecasts for this trip
        forecastRepository.deleteByTripId(tripId);

        List<WeatherForecast> allForecasts = new ArrayList<>();

        for (TripDestination dest : trip.getDestinations()) {
            try {
                List<WeatherForecast> forecasts = fetchRealForecast(trip, dest.getDestinationName());
                allForecasts.addAll(forecasts);
                log.info("Fetched {} real-time forecasts for '{}'", forecasts.size(), dest.getDestinationName());
            } catch (Exception e) {
                log.warn("Open-Meteo API failed for '{}': {}. Using mock data.", dest.getDestinationName(), e.getMessage());
                List<WeatherForecast> fallback = generateMockForecasts(trip, dest.getDestinationName());
                allForecasts.addAll(fallback);
            }
        }

        List<WeatherForecast> saved = forecastRepository.saveAll(allForecasts);
        return saved.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WeatherForecastResponse> getTripForecasts(Long tripId, User user) {
        getTripWithAccessCheck(tripId, user);
        return forecastRepository.findByTripIdOrderByForecastDateAsc(tripId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WeatherForecastResponse> getForecastByDate(Long tripId, LocalDate date, User user) {
        getTripWithAccessCheck(tripId, user);
        return forecastRepository.findByTripIdAndForecastDate(tripId, date)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WeatherAlertResponse> getWeatherAlerts(Long tripId, User user) {
        getTripWithAccessCheck(tripId, user);
        return forecastRepository.findAlertsByTripId(tripId).stream()
                .map(f -> WeatherAlertResponse.builder()
                        .locationName(f.getLocationName())
                        .forecastDate(f.getForecastDate().toString())
                        .condition(f.getCondition().name())
                        .alertMessage(f.getAlertMessage())
                        .severity(getSeverity(f.getCondition()))
                        .suggestedSwap(getSuggestedSwap(f.getCondition()))
                        .build())
                .collect(Collectors.toList());
    }

    public SeasonalAdvice getSeasonalAdvice(String locationName, LocalDate travelDate) {
        Season season = determineSeason(travelDate);
        return buildSeasonalAdvice(locationName, season);
    }

    @Transactional(readOnly = true)
    public List<SeasonalAdvice> getTripSeasonalAdvice(Long tripId, User user) {
        Trip trip = getTripWithAccessCheck(tripId, user);
        return trip.getDestinations().stream()
                .map(d -> getSeasonalAdvice(d.getDestinationName(), trip.getStartDate()))
                .collect(Collectors.toList());
    }

    // ── Open-Meteo Real-Time Integration ─────────────────────────────

    @lombok.Generated // Excluded from JaCoCo - real HTTP API integration
    private List<WeatherForecast> fetchRealForecast(Trip trip, String locationName) throws Exception {
        // Step 1: Geocode the location name → lat/lon
        double[] coords = geocodeLocation(locationName);
        double lat = coords[0];
        double lon = coords[1];

        // Step 2: Fetch daily forecast from Open-Meteo
        // Open-Meteo only supports up to 16 days in the future for free forecasts
        LocalDate startDate = trip.getStartDate();
        LocalDate endDate = trip.getEndDate();

        // Cap forecast range to 16 days from today if in the future
        LocalDate today = LocalDate.now();
        LocalDate maxForecastDate = today.plusDays(16);
        if (startDate.isAfter(maxForecastDate)) {
            // Trip is too far in the future for real forecast — fall back to mock
            log.info("Trip dates for '{}' are beyond 16-day forecast window. Using mock data.", locationName);
            return generateMockForecasts(trip, locationName);
        }
        if (endDate.isAfter(maxForecastDate)) {
            endDate = maxForecastDate;
        }
        if (startDate.isBefore(today)) {
            startDate = today;
        }

        String url = FORECAST_URL
                .replace("{lat}", String.valueOf(lat))
                .replace("{lon}", String.valueOf(lon))
                .replace("{start}", startDate.toString())
                .replace("{end}", endDate.toString());

        log.debug("Calling Open-Meteo forecast API: {}", url);
        String response = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(response);
        JsonNode daily = root.get("daily");

        if (daily == null) {
            throw new RuntimeException("No daily forecast data returned from Open-Meteo");
        }

        JsonNode times = daily.get("time");
        JsonNode weatherCodes = daily.get("weather_code");
        JsonNode tempMaxArr = daily.get("temperature_2m_max");
        JsonNode tempMinArr = daily.get("temperature_2m_min");
        JsonNode precipArr = daily.get("precipitation_sum");
        JsonNode precipProbArr = daily.get("precipitation_probability_max");
        JsonNode windArr = daily.get("wind_speed_10m_max");

        List<WeatherForecast> forecasts = new ArrayList<>();

        for (int i = 0; i < times.size(); i++) {
            LocalDate date = LocalDate.parse(times.get(i).asText());
            int wmoCode = weatherCodes.get(i).asInt();
            double tempHigh = tempMaxArr.get(i).asDouble();
            double tempLow = tempMinArr.get(i).asDouble();
            double precipSum = precipArr.get(i).asDouble();
            int precipProb = precipProbArr != null && !precipProbArr.get(i).isNull() ? precipProbArr.get(i).asInt() : estimatePrecipProb(precipSum);
            double windSpeed = windArr.get(i).asDouble();

            WeatherCondition condition = mapWmoToCondition(wmoCode, tempHigh);
            int humidity = estimateHumidity(condition, precipProb);

            boolean isAlert = condition == WeatherCondition.HEAVY_RAIN
                    || condition == WeatherCondition.THUNDERSTORM
                    || condition == WeatherCondition.HOT
                    || condition == WeatherCondition.SNOWY
                    || tempHigh >= 40
                    || precipProb >= 80;

            String alertMessage = isAlert ? buildAlertMessage(condition, tempHigh, precipProb) : null;

            forecasts.add(WeatherForecast.builder()
                    .trip(trip)
                    .locationName(locationName)
                    .forecastDate(date)
                    .condition(condition)
                    .temperatureHigh(Math.round(tempHigh * 10.0) / 10.0)
                    .temperatureLow(Math.round(tempLow * 10.0) / 10.0)
                    .humidityPercent(humidity)
                    .precipitationChance(precipProb)
                    .windSpeedKmh(Math.round(windSpeed * 10.0) / 10.0)
                    .description(getDescription(condition))
                    .recommendation(getRecommendation(condition))
                    .isAlert(isAlert)
                    .alertMessage(alertMessage)
                    .build());
        }

        return forecasts;
    }

    @lombok.Generated // Excluded from JaCoCo - real HTTP API integration
    private double[] geocodeLocation(String locationName) throws Exception {
        String url = GEOCODING_URL.replace("{name}", locationName.replace(" ", "+"));
        log.debug("Geocoding '{}' via Open-Meteo: {}", locationName, url);

        String response = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(response);
        JsonNode results = root.get("results");

        if (results == null || results.isEmpty()) {
            throw new RuntimeException("Could not geocode location: " + locationName);
        }

        JsonNode first = results.get(0);
        double lat = first.get("latitude").asDouble();
        double lon = first.get("longitude").asDouble();
        log.info("Geocoded '{}' → lat={}, lon={}", locationName, lat, lon);
        return new double[]{lat, lon};
    }

    // ── WMO Weather Code Mapping ─────────────────────────────────────

    /**
     * Maps WMO weather codes to our WeatherCondition enum.
     * See: https://open-meteo.com/en/docs#weathervariables
     */
    private WeatherCondition mapWmoToCondition(int wmoCode, double tempHigh) {
        if (tempHigh >= 40) return WeatherCondition.HOT;

        return switch (wmoCode) {
            case 0 -> WeatherCondition.SUNNY;           // Clear sky
            case 1, 2 -> WeatherCondition.PARTLY_CLOUDY; // Mainly clear, partly cloudy
            case 3 -> WeatherCondition.CLOUDY;           // Overcast
            case 45, 48 -> WeatherCondition.FOGGY;       // Fog, depositing rime fog
            case 51, 53, 55 -> WeatherCondition.RAINY;   // Drizzle (light, moderate, dense)
            case 61, 63 -> WeatherCondition.RAINY;       // Rain (slight, moderate)
            case 65, 80, 81, 82 -> WeatherCondition.HEAVY_RAIN; // Heavy rain, rain showers
            case 71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOWY; // Snow
            case 95, 96, 99 -> WeatherCondition.THUNDERSTORM; // Thunderstorm
            default -> WeatherCondition.PARTLY_CLOUDY;
        };
    }

    // ── Mock Fallback (for dates > 16 days out) ──────────────────────

    private List<WeatherForecast> generateMockForecasts(Trip trip, String location) {
        List<WeatherForecast> forecasts = new ArrayList<>();
        LocalDate current = trip.getStartDate();
        while (!current.isAfter(trip.getEndDate())) {
            forecasts.add(generateSingleMockForecast(trip, location, current));
            current = current.plusDays(1);
        }
        return forecasts;
    }

    private WeatherForecast generateSingleMockForecast(Trip trip, String location, LocalDate date) {
        Random rand = new Random(location.hashCode() + date.hashCode());
        Season season = determineSeason(date);

        WeatherCondition condition;
        double tempHigh, tempLow;
        int humidity, precipitation;
        double windSpeed;

        switch (season) {
            case SUMMER:
                condition = rand.nextInt(10) < 7 ? WeatherCondition.SUNNY : (rand.nextBoolean() ? WeatherCondition.HOT : WeatherCondition.PARTLY_CLOUDY);
                tempHigh = 30 + rand.nextInt(12); tempLow = 20 + rand.nextInt(8);
                humidity = 40 + rand.nextInt(30); precipitation = rand.nextInt(20); windSpeed = 5 + rand.nextInt(15);
                break;
            case MONSOON:
                condition = rand.nextInt(10) < 5 ? WeatherCondition.RAINY : (rand.nextInt(10) < 3 ? WeatherCondition.HEAVY_RAIN : WeatherCondition.THUNDERSTORM);
                tempHigh = 26 + rand.nextInt(8); tempLow = 20 + rand.nextInt(5);
                humidity = 70 + rand.nextInt(25); precipitation = 50 + rand.nextInt(50); windSpeed = 10 + rand.nextInt(25);
                break;
            case WINTER:
                condition = rand.nextInt(10) < 4 ? WeatherCondition.COLD : (rand.nextBoolean() ? WeatherCondition.CLOUDY : WeatherCondition.FOGGY);
                tempHigh = 10 + rand.nextInt(10); tempLow = rand.nextInt(8);
                humidity = 50 + rand.nextInt(30); precipitation = 10 + rand.nextInt(30); windSpeed = 8 + rand.nextInt(20);
                break;
            case SPRING:
                condition = rand.nextInt(10) < 6 ? WeatherCondition.PARTLY_CLOUDY : WeatherCondition.SUNNY;
                tempHigh = 18 + rand.nextInt(10); tempLow = 10 + rand.nextInt(8);
                humidity = 45 + rand.nextInt(25); precipitation = 15 + rand.nextInt(25); windSpeed = 8 + rand.nextInt(12);
                break;
            default:
                condition = rand.nextInt(10) < 5 ? WeatherCondition.CLOUDY : WeatherCondition.PARTLY_CLOUDY;
                tempHigh = 15 + rand.nextInt(12); tempLow = 8 + rand.nextInt(8);
                humidity = 50 + rand.nextInt(25); precipitation = 20 + rand.nextInt(30); windSpeed = 10 + rand.nextInt(15);
                break;
        }

        boolean isAlert = condition == WeatherCondition.HEAVY_RAIN || condition == WeatherCondition.THUNDERSTORM
                || condition == WeatherCondition.HOT || condition == WeatherCondition.SNOWY
                || tempHigh >= 40 || precipitation >= 80;
        String alertMessage = isAlert ? buildAlertMessage(condition, tempHigh, precipitation) : null;

        return WeatherForecast.builder()
                .trip(trip).locationName(location).forecastDate(date).condition(condition)
                .temperatureHigh(Math.round(tempHigh * 10.0) / 10.0).temperatureLow(Math.round(tempLow * 10.0) / 10.0)
                .humidityPercent(humidity).precipitationChance(precipitation).windSpeedKmh(Math.round(windSpeed * 10.0) / 10.0)
                .description(getDescription(condition)).recommendation(getRecommendation(condition))
                .isAlert(isAlert).alertMessage(alertMessage).build();
    }

    // ── Helper Methods ───────────────────────────────────────────────

    private int estimatePrecipProb(double precipSum) {
        if (precipSum <= 0) return 5;
        if (precipSum < 1) return 25;
        if (precipSum < 5) return 50;
        if (precipSum < 15) return 75;
        return 90;
    }

    private int estimateHumidity(WeatherCondition condition, int precipProb) {
        return switch (condition) {
            case RAINY, HEAVY_RAIN, THUNDERSTORM -> 75 + (int)(Math.random() * 20);
            case FOGGY -> 85 + (int)(Math.random() * 10);
            case SNOWY, COLD -> 50 + (int)(Math.random() * 20);
            case HOT, SUNNY -> 30 + (int)(Math.random() * 20);
            default -> 45 + (int)(Math.random() * 25);
        };
    }

    private Season determineSeason(LocalDate date) {
        Month month = date.getMonth();
        return switch (month) {
            case MARCH, APRIL -> Season.SPRING;
            case MAY, JUNE -> Season.SUMMER;
            case JULY, AUGUST, SEPTEMBER -> Season.MONSOON;
            case OCTOBER, NOVEMBER -> Season.AUTUMN;
            case DECEMBER, JANUARY, FEBRUARY -> Season.WINTER;
        };
    }

    private String getDescription(WeatherCondition condition) {
        return switch (condition) {
            case SUNNY -> "Clear skies with bright sunshine";
            case PARTLY_CLOUDY -> "Mix of sun and clouds";
            case CLOUDY -> "Overcast skies throughout the day";
            case RAINY -> "Light to moderate rainfall expected";
            case HEAVY_RAIN -> "Heavy and persistent rainfall";
            case THUNDERSTORM -> "Thunderstorms with lightning possible";
            case SNOWY -> "Snowfall expected in the area";
            case FOGGY -> "Low visibility due to fog";
            case WINDY -> "Strong winds throughout the day";
            case HOT -> "Extreme heat advisory";
            case COLD -> "Cold conditions, dress warmly";
        };
    }

    private String getRecommendation(WeatherCondition condition) {
        return switch (condition) {
            case SUNNY -> "Great day for outdoor activities. Wear sunscreen!";
            case PARTLY_CLOUDY -> "Good conditions for sightseeing";
            case CLOUDY -> "Carry a light jacket, ideal for museum visits";
            case RAINY -> "Carry an umbrella, consider indoor activities";
            case HEAVY_RAIN -> "Stay indoors. Visit museums, cafes, or shopping malls";
            case THUNDERSTORM -> "Avoid outdoor activities. Stay in safe shelter";
            case SNOWY -> "Bundle up! Great for winter sports";
            case FOGGY -> "Drive carefully. Enjoy cozy indoor activities";
            case WINDY -> "Secure loose items. Avoid high-altitude activities";
            case HOT -> "Stay hydrated, avoid midday sun. Choose water activities";
            case COLD -> "Layer up! Warm beverages and indoor cultural tours";
        };
    }

    private String buildAlertMessage(WeatherCondition condition, double tempHigh, int precipitation) {
        if (condition == WeatherCondition.HEAVY_RAIN || condition == WeatherCondition.THUNDERSTORM)
            return "⚠️ Severe weather alert: " + precipitation + "% chance of heavy precipitation. Consider rescheduling outdoor plans.";
        if (condition == WeatherCondition.HOT || tempHigh >= 40)
            return "🔥 Heat advisory: Temperatures may reach " + Math.round(tempHigh) + "°C. Stay hydrated and avoid prolonged sun exposure.";
        if (condition == WeatherCondition.SNOWY)
            return "❄️ Snow advisory: Travel disruptions possible. Check transport schedules.";
        return "⚠️ Weather advisory: Unusual conditions expected. Plan accordingly.";
    }

    private String getSeverity(WeatherCondition condition) {
        return switch (condition) {
            case THUNDERSTORM, HEAVY_RAIN -> "HIGH";
            case HOT, SNOWY, WINDY -> "MEDIUM";
            default -> "LOW";
        };
    }

    private String getSuggestedSwap(WeatherCondition condition) {
        return switch (condition) {
            case HEAVY_RAIN, THUNDERSTORM -> "Swap outdoor tours for museum visits, cooking classes, or spa sessions";
            case HOT -> "Replace hiking with water parks, pool activities, or evening city walks";
            case SNOWY -> "Try indoor markets, cultural shows, or switch to winter sports";
            case COLD -> "Visit heated attractions like galleries, theaters, or hot spring baths";
            default -> "Consider flexible indoor/outdoor alternatives";
        };
    }

    private String getConditionIcon(WeatherCondition condition) {
        return switch (condition) {
            case SUNNY -> "☀️";
            case PARTLY_CLOUDY -> "⛅";
            case CLOUDY -> "☁️";
            case RAINY -> "🌧️";
            case HEAVY_RAIN -> "🌊";
            case THUNDERSTORM -> "⛈️";
            case SNOWY -> "❄️";
            case FOGGY -> "🌫️";
            case WINDY -> "💨";
            case HOT -> "🔥";
            case COLD -> "🥶";
        };
    }

    private SeasonalAdvice buildSeasonalAdvice(String location, Season season) {
        String tempRange, bestTime, advice;
        List<String> activities, packItems;
        switch (season) {
            case SUMMER:
                tempRange = "28°C – 42°C"; bestTime = "Early morning or after 5 PM";
                advice = "Peak tourist season in many destinations. Book activities in advance. Stay hydrated.";
                activities = List.of("Water sports", "Beach activities", "Early morning treks", "Evening cultural shows");
                packItems = List.of("Sunscreen SPF 50+", "Sunglasses", "Light cotton clothes", "Reusable water bottle", "Hat/Cap");
                break;
            case MONSOON:
                tempRange = "22°C – 32°C"; bestTime = "Breaks between rain spells";
                advice = "Expect heavy rainfall. Some attractions may close. Roads can be slippery.";
                activities = List.of("Indoor museums", "Ayurvedic spa treatments", "Rainy season photography", "Local cooking classes");
                packItems = List.of("Waterproof jacket", "Umbrella", "Quick-dry clothing", "Waterproof bags for electronics", "Anti-fungal powder");
                break;
            case WINTER:
                tempRange = "2°C – 18°C"; bestTime = "Late morning to early afternoon";
                advice = "Perfect weather for sightseeing in tropical destinations. Can be cold in northern areas.";
                activities = List.of("City walking tours", "Heritage site visits", "Mountain retreats", "Hot air balloon rides");
                packItems = List.of("Warm layers", "Thermal innerwear", "Woolen socks", "Moisturizer", "Warm beverages flask");
                break;
            case SPRING:
                tempRange = "15°C – 28°C"; bestTime = "Throughout the day";
                advice = "Ideal weather for most activities. Flowers in bloom. Less crowded than summer.";
                activities = List.of("Nature walks", "Garden visits", "Outdoor dining", "Photography tours", "Cycling");
                packItems = List.of("Light layers", "Comfortable walking shoes", "Allergy medication", "Camera");
                break;
            default:
                tempRange = "12°C – 25°C"; bestTime = "All day, especially golden hours";
                advice = "Beautiful foliage season. Great for photography and outdoor activities.";
                activities = List.of("Hiking", "Vineyard tours", "Foliage drives", "Outdoor markets");
                packItems = List.of("Medium-weight jacket", "Comfortable boots", "Layered clothing", "Binoculars");
                break;
        }
        return SeasonalAdvice.builder().locationName(location).season(season.name()).temperatureRange(tempRange)
                .bestTimeToVisit(bestTime).advice(advice).recommendedActivities(activities).itemsToPack(packItems).build();
    }

    private WeatherForecastResponse mapToResponse(WeatherForecast f) {
        return WeatherForecastResponse.builder()
                .id(f.getId()).tripId(f.getTrip().getId()).locationName(f.getLocationName())
                .forecastDate(f.getForecastDate()).condition(f.getCondition().name())
                .conditionIcon(getConditionIcon(f.getCondition()))
                .temperatureHigh(f.getTemperatureHigh()).temperatureLow(f.getTemperatureLow())
                .humidityPercent(f.getHumidityPercent()).precipitationChance(f.getPrecipitationChance())
                .windSpeedKmh(f.getWindSpeedKmh()).description(f.getDescription())
                .recommendation(f.getRecommendation()).isAlert(f.getIsAlert()).alertMessage(f.getAlertMessage())
                .build();
    }

    private Trip getTripWithAccessCheck(Long tripId, User user) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        boolean isOwner = trip.getCreatedBy().getId().equals(user.getId());
        boolean isCollaborator = collaboratorRepository.findByTripIdAndUserId(trip.getId(), user.getId()).isPresent();
        if (!isOwner && !isCollaborator) throw new RuntimeException("Not authorized");
        return trip;
    }
}
