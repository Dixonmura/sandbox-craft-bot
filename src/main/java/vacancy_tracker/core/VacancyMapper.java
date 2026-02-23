package vacancy_tracker.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import vacancy_tracker.api.trudvsem.TrudVsemVacanciesResponse;
import vacancy_tracker.api.trudvsem.TrudVsemVacancy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Преобразует данные TrudVsem API в доменные объекты Vacancy.
 *
 * <p>Поддерживает два сценария: типизированные Records и сырой JSON.</p>
 */
public final class VacancyMapper {

    private static final DateTimeFormatter MODIFY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd['T'][HH:mm:ss][XXX]");

    public static List<Vacancy> fromTrudVsemResponse(TrudVsemVacanciesResponse response) {
        if (!"200".equals(response.status())) {
            throw new IllegalArgumentException("Invalid status: " + response.status());
        }

        if (response.results() == null || response.results().vacancies() == null) {
            return List.of();
        }

        return response.results().vacancies().stream()
                .map(VacancyMapper::fromTrudVsemVacancy)
                .collect(Collectors.toList());
    }

    private static Vacancy fromTrudVsemVacancy(TrudVsemVacancy vacancy) {
        return new Vacancy(
                null,
                vacancy.id(),
                vacancy.jobName(),
                vacancy.company() != null ? vacancy.company().name() : null,
                vacancy.salaryMin(),
                vacancy.salaryMax(),
                vacancy.region() != null ? parseRegionCode(vacancy.region().regionCode()) : null,
                vacancy.vacancyUrl(),
                parsePublishedAt(vacancy.dateModify()),  // переименуй parsePublishedAt
                vacancy.source()
        );
    }

    public static List<Vacancy> fromTrudVsemJson(String json) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(json);

            if (!"200".equals(root.path("status").asText())) {
                return List.of();
            }

            JsonNode vacancies = root.path("results").path("vacancies");
            return StreamSupport.stream(vacancies.spliterator(), false)
                    .map(wrapper -> wrapper.path("vacancy"))  // Извлекаем vacancy
                    .map(VacancyMapper::nodeToVacancy)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return List.of();
        }
    }

    private static Vacancy nodeToVacancy(JsonNode vacancy) {
        return new Vacancy(
                null,
                vacancy.path("id").asText(null),
                vacancy.path("job-name").asText(null),
                vacancy.path("company").path("name").asText(null),
                safeInt(vacancy.path("salary_min")),
                safeInt(vacancy.path("salary_max")),
                parseRegionCode(vacancy.path("region").path("region_code").asText(null)),
                vacancy.path("vac_url").asText(null),
                parsePublishedAt(vacancy.path("date_modify").asText(null)),
                vacancy.path("source").asText("trudvsem")
        );
    }

    private static Integer safeInt(JsonNode node) {
        return node.isInt() ? node.asInt() : null;
    }

    private static Integer parseRegionCode(String regionCodeStr) {
        if (regionCodeStr == null || regionCodeStr.length() < 2) return null;
        try {
            return Integer.parseInt(regionCodeStr.substring(0, 2));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Instant parsePublishedAt(String dateModify) {
        if (dateModify == null) return Instant.now();

        try {
            return LocalDateTime.parse(dateModify, MODIFY_FORMATTER)
                    .toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return Instant.now();
        }
    }
}
