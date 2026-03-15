package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO для десериализации ответа от API TrudVsem.
 * <p>
 * Структура полностью соответствует JSON-ответу:
 * <pre>
 * {
 *   "status": "200",
 *   "meta": { "total": 1000, "limit": 100 },
 *   "results": {
 *     "vacancies": [
 *       { "vacancy": { ... } }
 *     ]
 *   }
 * }
 * </pre>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrudVsemResponse {

    private String status;
    private Meta meta;
    private Results results;

    /**
     * Мета-информация о результатах поиска.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        private int total;
        private int limit;
    }

    /**
     * Контейнер для списка вакансий.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Results {
        private List<VacancyWrapper> vacancies;
    }

    /**
     * Обёртка для вакансии (из-за вложенности в JSON).
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VacancyWrapper {
        private VacancyDto vacancy;
    }

    /**
     * DTO вакансии со всеми полями, которые использует приложение.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VacancyDto {
        private String id;
        private Region region;
        private Company company;

        @JsonProperty("salary_min")
        private Integer salaryMin;

        @JsonProperty("salary_max")
        private Integer salaryMax;

        @JsonProperty("job-name")
        private String jobName;

        @JsonProperty("vac_url")
        private String vacancyUrl;

        private Requirement requirement;
    }

    /**
     * Информация о регионе вакансии.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Region {
        @JsonProperty("region_code")
        private String regionCode;
        private String name;
    }

    /**
     * Информация о компании-работодателе.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Company {
        private String name;
    }

    /**
     * Требования к кандидату.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Requirement {
        private Integer experience;
    }
}