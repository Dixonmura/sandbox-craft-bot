package vacancy_tracker.core;

import java.util.List;

public interface VacancySearchService {

    List<Vacancy> findVacancies(UserSettings userSettings);
}
