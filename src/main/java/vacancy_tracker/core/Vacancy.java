package vacancy_tracker.core;

/**
 * Модель вакансии, приведённая к удобному для бота виду.
 */

public record Vacancy(
        String externalId,
        String title,
        String company,
        Integer salaryFrom,
        Integer salaryTo,
        Integer regionCode,
        Integer experience,
        String url) {

    /**
     * Конструктор класса вакансии.
     *
     * @param externalId  идентификатор вакансии во внешнем API
     * @param title       название вакансии
     * @param company     название компании
     * @param salaryFrom  минимальное значение заработной платы (в рублях), может быть null
     * @param salaryTo    максимальное значение заработной платы (в рублях), может быть null
     * @param regionCode  код региона
     * @param experience  необходимый опыт
     * @param url         ссылка на вакансию
     */
    public Vacancy {
    }
}