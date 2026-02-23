package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Полная вакансия из API TrudVsem.
 *
 * <p>Содержит все данные о вакансии: ID, зарплата, компания, регион, даты и ссылку.</p>
 */
public record TrudVsemVacancy(
        String id,
        String source,
        TrudVsemRegion region,
        TrudVsemCompany company,
        @JsonProperty("creation-date") String creationDate,
        @JsonProperty("date_modify") String dateModify,
        @JsonProperty("salary_min") Integer salaryMin,
        @JsonProperty("salary_max") Integer salaryMax,
        @JsonProperty("job-name") String jobName,
        @JsonProperty("vac_url") String vacancyUrl
) {}