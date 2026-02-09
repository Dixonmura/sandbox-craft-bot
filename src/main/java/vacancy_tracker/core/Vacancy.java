package vacancy_tracker.core;

import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

/**
 * Модель вакансии, приведённая к удобному для бота виду.
 */

@Getter
@ToString
public class Vacancy {

    private final Long id;
    private final String externalId;
    private final String title;
    private final String company;
    private final Integer salaryFrom;
    private final Integer salaryTo;
    private final Integer regionCode;
    private final String url;
    private final Instant publishedAt;
    private final String source;

    /**
     * Конструктор класса вакансии.
     *
     * @param id          идентификатор вакансии внутри системы бота
     * @param externalId  идентификатор вакансии во внешнем API
     * @param title       название вакансии
     * @param company     название компании
     * @param salaryFrom  минимальное значение заработной платы (в рублях), может быть null
     * @param salaryTo    максимальное значение заработной платы (в рублях), может быть null
     * @param regionCode  код региона
     * @param url         ссылка на вакансию
     * @param publishedAt дата публикации вакансии
     * @param source      наименование площадки, откуда вакансия
     */
    public Vacancy(
            Long id,
            String externalId,
            String title,
            String company,
            Integer salaryFrom,
            Integer salaryTo,
            Integer regionCode,
            String url,
            Instant publishedAt,
            String source
    ) {
        this.id = id;
        this.externalId = externalId;
        this.title = title;
        this.company = company;
        this.salaryFrom = salaryFrom;
        this.salaryTo = salaryTo;
        this.regionCode = regionCode;
        this.url = url;
        this.publishedAt = publishedAt;
        this.source = source;
    }
}