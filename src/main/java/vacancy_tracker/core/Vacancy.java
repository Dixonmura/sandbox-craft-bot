package vacancy_tracker.core;

/**
 * Модель вакансии, приведённая к удобному для бота виду.
 *
 * @param company        наименование компании вакансии
 * @param salaryFrom     минимальное значение заработной платы (в рублях), может быть null
 * @param salaryTo       максимальное значение заработной платы (в рублях), может быть null
 * @param experienceFrom минимальный ожидаемый от претендента опыт (в годах), может быть null
 * @param url            ссылка на вакансию
 */
public record Vacancy(
        String company,
        Integer salaryFrom,
        Integer salaryTo,
        Integer experienceFrom,
        String url
) {
}
