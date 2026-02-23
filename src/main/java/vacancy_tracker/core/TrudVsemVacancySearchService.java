package vacancy_tracker.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import vacancy_tracker.api.trudvsem.TrudVsemVacanciesResponse;
import vacancy_tracker.api.trudvsem.TrudVsemVacancy;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
 *     <li>Обрабатывает пагинацию с помощью параметров {@code offset} и {@code limit};</li>
 *     <li>Парсит JSON‑ответ в {@link TrudVsemVacanciesResponse} с помощью {@link ObjectMapper};</li>
 *     <li>Маппит DTO TrudVsem в доменную модель {@link Vacancy}.</li>
 * </ul>
 */
public class TrudVsemVacancySearchService implements VacancySearchService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_PAGES = 5;

    /**
     * Формат даты/времени для параметра {@code modifiedFrom} и поля {@code dateModify}
     * в ответе TrudVsem (ISO_OFFSET_DATE_TIME, например 2025-01-01T01:00:00Z).
     */
    private static final DateTimeFormatter MODIFIED_FORMATTER =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

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
    }

    /**
     * Выполняет поиск вакансий для конкретного пользователя на основе его настроек.
     * <p>
     * Метод:
     * <ol>
     *     <li>Вычисляет URL с учётом фильтров и параметров пагинации;</li>
     *     <li>Последовательно запрашивает страницы, пока не достигнут предел страниц,
     *     не закончились вакансии или не исчерпано значение {@code total};</li>
     *     <li>Маппит каждую вакансию в доменную модель {@link Vacancy}.</li>
     * </ol>
     *
     * @param userSettings настройки пользователя (регион, опыт, зарплата, ключевое слово и т.д.)
     * @return список найденных вакансий, возможно пустой, но никогда не {@code null}
     * @throws IllegalArgumentException если {@code userSettings} равен {@code null}
     */
    @Override
    public List<Vacancy> findVacancies(UserSettings userSettings) {
        if (userSettings == null) {
            throw new IllegalArgumentException("userSettings is null");
        }

        List<Vacancy> result = new ArrayList<>();

        int offset = 0;
        int pagesLoaded = 0;

        while (pagesLoaded < MAX_PAGES) {
            HttpUrl url = buildUrl(userSettings, offset, DEFAULT_LIMIT);
            TrudVsemVacanciesResponse response = executeRequest(url);
            if (response == null
                    || response.results() == null
                    || response.results().vacancies() == null
                    || response.results().vacancies().isEmpty()) {
                break;
            }

            for (TrudVsemVacancy v : response.results().vacancies()) {
                Vacancy vacancy = mapToDomain(v);
                if (vacancy != null) {
                    result.add(vacancy);
                }
            }

            int received = response.results().vacancies().size();
            offset += received;
            pagesLoaded++;

            if (response.meta() != null && offset >= response.meta().total()) {
                break;
            }
            if (received < DEFAULT_LIMIT) {
                break;
            }
        }

        return result;
    }

    /**
     * Собирает URL для запроса к TrudVsem с учётом пользовательских настроек.
     * <p>
     * Здесь добавляются:
     * <ul>
     *     <li>{@code offset} и {@code limit} для пагинации;</li>
     *     <li>{@code region} — код региона из {@link UserSettings#getRegionCode()};</li>
     *     <li>{@code experienceFrom} — минимальный опыт;</li>
     *     <li>{@code salaryFrom} — минимальная зарплата;</li>
     *     <li>{@code text} — ключевое слово, URL‑кодируется;</li>
     *     <li>{@code modifiedFrom} — только вакансии, изменённые за последние 7 дней.</li>
     * </ul>
     */
    HttpUrl buildUrl(UserSettings settings, int offset, int limit) {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(baseUrl)).newBuilder();

        builder.addQueryParameter("offset", String.valueOf(offset));
        builder.addQueryParameter("limit", String.valueOf(limit));

        if (settings.getRegionCode() != null) {
            builder.addQueryParameter("region", String.valueOf(settings.getRegionCode()));
        }

        if (settings.getExperienceFrom() != null) {
            builder.addQueryParameter("experienceFrom",
                    String.valueOf(settings.getExperienceFrom()));
        }

        if (settings.getSalaryFrom() != null) {
            builder.addQueryParameter("salaryFrom",
                    String.valueOf(settings.getSalaryFrom()));
        }

        if (settings.getWordForSearch() != null
                && !settings.getWordForSearch().isBlank()) {
            String encoded = URLEncoder.encode(
                    settings.getWordForSearch().trim(),
                    StandardCharsets.UTF_8
            );
            builder.addQueryParameter("text", encoded);
        }

        String modifiedFrom = LocalDate.now(ZoneOffset.UTC)
                .minusDays(7)
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC)
                .format(MODIFIED_FORMATTER);
        builder.addQueryParameter("modifiedFrom", modifiedFrom);

        return builder.build();
    }

    /**
     * Выполняет HTTP‑запрос и десериализует тело ответа в {@link TrudVsemVacanciesResponse}.
     * <p>
     * В случае любой ошибки (сетевой, неверный статус, пустое тело, ошибка парсинга)
     * метод возвращает {@code null}, а вызывающий код решает, что делать дальше.
     */
    TrudVsemVacanciesResponse executeRequest(HttpUrl url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            String body = response.body().string();
            return objectMapper.readValue(body, TrudVsemVacanciesResponse.class);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Маппит DTO {@link TrudVsemVacancy} в доменную модель {@link Vacancy}.
     * <p>
     * Здесь:
     * <ul>
     *     <li>Извлекается код региона (первые 2 цифры из строки);</li>
     *     <li>Разбирается поле {@code dateModify} в {@link Instant};</li>
     *     <li>Подставляется название компании и другие поля доменной модели.</li>
     * </ul>
     */
    Vacancy mapToDomain(TrudVsemVacancy v) {
        if (v == null) {
            return null;
        }

        Integer regionCode = null;
        if (v.region() != null && v.region().regionCode() != null) {
            String regionStr = v.region().regionCode();
            if (regionStr.length() >= 2) {
                try {
                    regionCode = Integer.parseInt(regionStr.substring(0, 2));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        Instant publishedAt = Instant.now();
        if (v.dateModify() != null) {
            try {
                publishedAt = Instant.from(MODIFIED_FORMATTER.parse(v.dateModify()));
            } catch (Exception ignored) {
            }
        }

        String companyName = v.company() != null ? v.company().name() : null;

        return new Vacancy(
                null,
                v.id(),
                v.jobName(),
                companyName,
                v.salaryMin(),
                v.salaryMax(),
                regionCode,
                v.vacancyUrl(),
                publishedAt,
                v.source()
        );
    }
}

