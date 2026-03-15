package vacancy_tracker.api.trudvsem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vacancy_tracker.core.UserSettings;

/**
 * Построитель URL-запросов к API TrudVsem.
 * <p>
 * Формирует корректный URL с учётом фильтров пользователя:
 * <ul>
 *     <li>регион (добавляется в путь /region/{code})</li>
 *     <li>минимальный опыт (параметр experienceFrom)</li>
 *     <li>ключевое слово (параметр text)</li>
 * </ul>
 * Все запросы используют offset=0 и limit=100 (первая страница).
 */
public class TrudVsemUrlBuilder {

    private static final Logger log = LogManager.getLogger(TrudVsemUrlBuilder.class);

    private static final String DEFAULT_OFFSET = "0";
    private static final String DEFAULT_LIMIT = "100";
    private static final String REGION_PARAMS = "/region/";
    private static final String OFFSET_PARAMS = "offset=";
    private static final String LIMIT_PARAMS = "limit=";
    private static final String EXPERIENCE_PARAMS = "experienceFrom=";
    private static final String KEY_WORD_PARAMS = "text=";

    /**
     * Строит URL для запроса к API TrudVsem.
     *
     * @param baseUrl      базовый URL API (например, https://opendata.trudvsem.ru/api/v1/vacancies)
     * @param userSettings настройки пользователя с фильтрами
     * @return готовый URL для HTTP-запроса
     * @throws IllegalArgumentException если baseUrl или userSettings равны null
     */
    public String buildQuery(String baseUrl, UserSettings userSettings) {

        log.debug("Построение URL с параметрами: baseUrl={}, region={}, experience={}, keyword={}",
                baseUrl,
                userSettings != null ? userSettings.getRegionCode() : null,
                userSettings != null ? userSettings.getExperienceFrom() : null,
                userSettings != null ? userSettings.getWordForSearch() : null
        );

        if (baseUrl == null || baseUrl.isBlank()) {
            log.error("Попытка построить URL с невалидным baseUrl: {}", baseUrl);
            throw new IllegalArgumentException("baseUrl не может быть null");
        }

        if (userSettings == null) {
            log.error("Попытка построить URL с null userSettings");
            throw new IllegalArgumentException("userSettings не может быть null");
        }

        StringBuilder builder = new StringBuilder();
        builder.append(baseUrl);

        Integer region = userSettings.getRegionCode();
        Integer experience = userSettings.getExperienceFrom();
        String keyWord = userSettings.getWordForSearch();

        if (region != null) {
            builder.append(REGION_PARAMS)
                    .append(region);
        }

        builder.append("?")
                .append(OFFSET_PARAMS)
                .append(DEFAULT_OFFSET)
                .append("&")
                .append(LIMIT_PARAMS)
                .append(DEFAULT_LIMIT);

        if (experience != null) {
            builder.append("&")
                    .append(EXPERIENCE_PARAMS)
                    .append(experience);
        }

        if (keyWord != null && !keyWord.isBlank()) {
            builder.append("&")
                    .append(KEY_WORD_PARAMS)
                    .append(keyWord);
        }

        return builder.toString();
    }
}
