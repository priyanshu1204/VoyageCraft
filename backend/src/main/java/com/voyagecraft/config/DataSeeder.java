package com.voyagecraft.config;

import com.voyagecraft.entity.TripTemplate;
import com.voyagecraft.repository.TripTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final TripTemplateRepository templateRepository;

    @Override
    public void run(String... args) {
        if (templateRepository.count() == 0) {
            log.info("Seeding trip templates...");

            templateRepository.save(TripTemplate.builder()
                    .name("European Backpacking Adventure")
                    .description("10-day journey through Paris, Amsterdam, and Berlin with hostel stays and local experiences.")
                    .destinationsJson("[{\"name\":\"Paris\",\"country\":\"France\"},{\"name\":\"Amsterdam\",\"country\":\"Netherlands\"},{\"name\":\"Berlin\",\"country\":\"Germany\"}]")
                    .durationDays(10).budgetEstimate(new BigDecimal("2500")).category("Adventure").build());

            templateRepository.save(TripTemplate.builder()
                    .name("Southeast Asia Explorer")
                    .description("14-day tropical adventure through Thailand, Vietnam, and Bali.")
                    .destinationsJson("[{\"name\":\"Bangkok\",\"country\":\"Thailand\"},{\"name\":\"Hanoi\",\"country\":\"Vietnam\"},{\"name\":\"Bali\",\"country\":\"Indonesia\"}]")
                    .durationDays(14).budgetEstimate(new BigDecimal("1800")).category("Adventure").build());

            templateRepository.save(TripTemplate.builder()
                    .name("Japanese Culture & Food Tour")
                    .description("7-day immersive experience in Tokyo, Kyoto, and Osaka.")
                    .destinationsJson("[{\"name\":\"Tokyo\",\"country\":\"Japan\"},{\"name\":\"Kyoto\",\"country\":\"Japan\"},{\"name\":\"Osaka\",\"country\":\"Japan\"}]")
                    .durationDays(7).budgetEstimate(new BigDecimal("3000")).category("Cultural").build());

            templateRepository.save(TripTemplate.builder()
                    .name("Romantic Italian Getaway")
                    .description("5-day romantic escape through Rome, Florence, and Venice.")
                    .destinationsJson("[{\"name\":\"Rome\",\"country\":\"Italy\"},{\"name\":\"Florence\",\"country\":\"Italy\"},{\"name\":\"Venice\",\"country\":\"Italy\"}]")
                    .durationDays(5).budgetEstimate(new BigDecimal("2000")).category("Romantic").build());

            templateRepository.save(TripTemplate.builder()
                    .name("Family Beach Vacation")
                    .description("7-day family-friendly beach holiday in Maldives.")
                    .destinationsJson("[{\"name\":\"Male\",\"country\":\"Maldives\"},{\"name\":\"Maafushi\",\"country\":\"Maldives\"}]")
                    .durationDays(7).budgetEstimate(new BigDecimal("4000")).category("Family").build());

            log.info("Seeded {} trip templates", templateRepository.count());
        }
    }
}
