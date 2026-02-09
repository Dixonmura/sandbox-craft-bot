package vacancy_tracker.core;

import java.util.List;

public interface VacancySearchService {

    public List<Vacancy> findVacancies(UserSettings userSettings);
}
