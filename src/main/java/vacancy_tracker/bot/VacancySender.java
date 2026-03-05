package vacancy_tracker.bot;

import vacancy_tracker.core.Vacancy;

import java.util.List;

public interface VacancySender {
    void sendVacancies(Long chatId, List<Vacancy> vacancies);
}
