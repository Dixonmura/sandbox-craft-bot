package vacancy_tracker.api.trudvsem;

import java.util.List;

/**
 * Контейнер результатов поиска вакансий из API TrudVsem.
 *
 * <p>Содержит список вакансий, возвращаемый в ответе API.</p>
 */
public record TrudVsemResults(List<TrudVsemVacancy> vacancies) {
}
