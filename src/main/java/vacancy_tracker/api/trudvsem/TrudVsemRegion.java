package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO для информации о регионе из API TrudVsem.
 *
 * <p>Содержит код региона и его название для фильтрации вакансий.</p>
 */
public record TrudVsemRegion(
        @JsonProperty("region_code")
        String regionCode,
        String name
) {}
