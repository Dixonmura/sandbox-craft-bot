package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vacancy_tracker.core.UserSettings;
import vacancy_tracker.core.Vacancy;
import vacancy_tracker.core.VacancySearchService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Реализация {@link VacancySearchService}, которая обращается к публичному API TrudVsem.
 * <p>
 * Класс:
 * <ul>
 *     <li>Собирает HTTP‑запрос с учётом пользовательских настроек ({@link UserSettings});</li>
 *     <li>Выполняет один запрос (лимит 100 вакансий);</li>
 *     <li>Парсит JSON‑ответ с помощью {@link ObjectMapper};</li>
 *     <li>Маппит DTO TrudVsem в доменную модель {@link Vacancy};</li>
 *     <li>Фильтрует результаты по зарплате на стороне клиента.</li>
 * </ul>
 */
public class TrudVsemVacancySearchService implements VacancySearchService {

    private static final Logger log = LogManager.getLogger(TrudVsemVacancySearchService.class);

    /**
     * Формат даты/времени для параметра {@code modifiedFrom} и поля {@code dateModify}
     * в ответе TrudVsem (ISO_OFFSET_DATE_TIME, например 2025-01-01T01:00:00Z).
     */
    private static final DateTimeFormatter MODIFIED_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final TrudVsemMapper trudVsemMapper;
    private final TrudVsemUrlBuilder urlBuilder;


    /**
     * Создаёт сервис поиска вакансий по API TrudVsem.
     *
     * @param httpClient   клиент OkHttp, используемый для HTTP‑запросов
     * @param objectMapper ObjectMapper для десериализации JSON
     * @param baseUrl      базовый URL API (обычно
     *                     {@code https://opendata.trudvsem.ru/api/v1/vacancies});
     *                     вынесен в параметр для удобства тестирования
     */
    public TrudVsemVacancySearchService(OkHttpClient httpClient,
                                        ObjectMapper objectMapper,
                                        String baseUrl) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient is null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is null");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl is null");
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.trudVsemMapper = new TrudVsemMapper();
        this.urlBuilder = new TrudVsemUrlBuilder();
    }

    /**
     * Выполняет поиск вакансий на основе настроек пользователя.
     * <p>
     * Метод:
     * <ol>
     *     <li>Строит URL с помощью {@link TrudVsemUrlBuilder};</li>
     *     <li>Выполняет один запрос к API (максимум 100 вакансий);</li>
     *     <li>Фильтрует результаты по зарплате, если фильтр задан;</li>
     *     <li>Маппит каждую вакансию в доменную модель {@link Vacancy}.</li>
     * </ol>
     *
     * @param userSettings настройки пользователя (регион, опыт, зарплата, ключевое слово и т.д.)
     * @return список найденных вакансий (максимум 100), никогда не {@code null}
     * @throws IllegalArgumentException если {@code userSettings} равен {@code null}
     */
    @Override
    public List<Vacancy> findVacancies(UserSettings userSettings) {
        if (userSettings == null) {
            log.error("findVacancies вызван с null settings");
            throw new IllegalArgumentException("userSettings не может быть null");
        }

        List<Vacancy> result = new ArrayList<>();

        String url = urlBuilder.buildQuery(baseUrl, userSettings);
        log.debug("Запрос к API: {}", url);

        TrudVsemResponse response = executeRequest(url);

        if (response == null || !"200".equals(response.getStatus())) {
            log.warn("Не удалось получить данные или статус не 200");
            return result;
        }

        if (response.getResults() == null ||
                response.getResults().getVacancies() == null) {
            return result;
        }

        for (TrudVsemResponse.VacancyWrapper wrapper : response.getResults().getVacancies()) {
            if (wrapper.getVacancy() != null) {
                Vacancy vacancy = trudVsemMapper.mapToDomain(wrapper.getVacancy());
                if (vacancy != null && passesSalaryFilter(vacancy, userSettings)) {
                    result.add(vacancy);
                }
            }
        }

        log.info("Найдено {} вакансий из 100", result.size());
        return result;
    }

    /**
     * Выполняет HTTP-запрос к API и десериализует ответ.
     *
     * @param url полный URL для запроса
     * @return объект ответа или null при ошибке
     */
    private TrudVsemResponse executeRequest(String url) {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get()
                .build();

        try (okhttp3.Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.error("Ошибка HTTP: {}", response.code());
                return null;
            }

            String body = response.body().string();
            return objectMapper.readValue(body, TrudVsemResponse.class);

        } catch (Exception e) {
            log.error("Ошибка при выполнении запроса: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Проверяет, проходит ли вакансия фильтр по минимальной зарплате.
     *
     * @param vacancy   вакансия для проверки
     * @param settings  настройки пользователя
     * @return true если вакансия подходит под фильтр или фильтр отключён
     */
    private boolean passesSalaryFilter(Vacancy vacancy, UserSettings settings) {
        if (settings.getSalaryFrom() == null || settings.getSalaryFrom() == 0) {
            return true;
        }
        if (vacancy.salaryFrom() == null || vacancy.salaryFrom() == 0) {
            return true;
        }
        return vacancy.salaryFrom() >= settings.getSalaryFrom();
    }
}