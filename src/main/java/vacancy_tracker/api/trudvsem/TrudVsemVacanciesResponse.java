package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Полный ответ API TrudVsem на запрос вакансий.
 *
 * <p>Содержит статус, метаданные, результаты и информацию о запросе.</p>
 */
public record TrudVsemVacanciesResponse(
        String status,
        @JsonProperty("request") TrudVsemRequestInfo request,
        TrudVsemMeta meta,
        TrudVsemResults results
) {}
