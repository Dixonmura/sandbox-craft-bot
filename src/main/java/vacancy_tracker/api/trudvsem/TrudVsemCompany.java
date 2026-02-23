package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO для десериализации данных о компании из JSON-ответа API TrudVsem.
 *
 * <p>Содержит название компании-работодателя из вакансии.</p>
 */
public record TrudVsemCompany(
        @JsonProperty("name") String name
) {}
