package vacancy_tracker.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.api.trudvsem.*;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrudVsemVacancySearchServiceTest {

    private MockWebServer mockWebServer;
    private TrudVsemVacancySearchService service;
    ObjectMapper mapper;

    @BeforeEach
    @DisplayName("Подготовка MockWebServer и сервиса поиска вакансий")
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        OkHttpClient client = new OkHttpClient();
        mapper = new ObjectMapper();

        String baseUrl = mockWebServer.url("/api/v1/vacancies").toString();
        service = new TrudVsemVacancySearchService(client, mapper, baseUrl);
    }

    @AfterEach
    @DisplayName("Остановка MockWebServer после теста")
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("buildUrl: все фильтры попадают в query-параметры")
    void buildUrl_shouldIncludeAllFilters_whenAllSettingsPresent() {
        UserSettings settings = new UserSettings(
                78,
                1,
                50_000,
                "Java Developer",
                LocalTime.of(10, 0)
        );

        HttpUrl url = service.buildUrl(settings, 100, 20);

        assertThat(url.queryParameter("offset")).isEqualTo("100");
        assertThat(url.queryParameter("limit")).isEqualTo("20");
        assertThat(url.queryParameter("region")).isEqualTo("78");
        assertThat(url.queryParameter("experienceFrom")).isEqualTo("1");
        assertThat(url.queryParameter("salaryFrom")).isEqualTo("50000");
        assertThat(url.queryParameter("text")).isNotBlank();
        assertThat(url.queryParameter("modifiedFrom")).isNotBlank();
    }

    @Test
    @DisplayName("findVacancies: возвращает корректно замапленные вакансии при успешном ответе")
    void findVacancies_shouldReturnMappedVacancies_whenResponseIsSuccessful() throws Exception {

        String jsonResponse = createValidTrudVsemJsonResponse();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse)
                .addHeader("Content-Type", "application/json"));

        UserSettings settings = new UserSettings(
                78, 1, 50_000, "Java Developer", LocalTime.of(10, 0)
        );

        List<Vacancy> vacancies = service.findVacancies(settings);

        assertThat(vacancies).hasSize(1);
        Vacancy v = vacancies.get(0);

        assertThat(v.getExternalId()).isEqualTo("123");
        assertThat(v.getTitle()).isEqualTo("Java Developer");
        assertThat(v.getCompany()).isEqualTo("Cool Company");  // или v.company()
        assertThat(v.getSalaryFrom()).isEqualTo(60_000);
        assertThat(v.getSalaryTo()).isEqualTo(90_000);
        assertThat(v.getRegionCode()).isEqualTo(78);
        assertThat(v.getUrl()).isEqualTo("https://example.com/vacancy/123");
        assertThat(v.getSource()).isEqualTo("trudvsem");
    }

    @Test
    @DisplayName("buildUrl: не добавляет text, когда ключевое слово пустое или состоит из пробелов")
    void buildUrl_shouldNotIncludeText_whenKeywordBlank() {
        UserSettings settings = new UserSettings(
                78,
                1,
                50_000,
                "   ",
                LocalTime.of(10, 0)
        );

        HttpUrl url = service.buildUrl(settings, 0, 50);

        assertThat(url.queryParameter("text")).isNull();
    }

    @Test
    @DisplayName("buildUrl: не добавляет region, experienceFrom и salaryFrom, когда они null")
    void buildUrl_shouldSkipNullNumericFilters_whenSettingsFieldsNull() {
        UserSettings settings = new UserSettings(
                null,
                null,
                null,
                "Java",
                LocalTime.of(10, 0)
        );

        HttpUrl url = service.buildUrl(settings, 0, 50);

        assertThat(url.queryParameter("region")).isNull();
        assertThat(url.queryParameter("experienceFrom")).isNull();
        assertThat(url.queryParameter("salaryFrom")).isNull();
        assertThat(url.queryParameter("text")).isNotBlank();
    }

    private String createValidTrudVsemJsonResponse() throws Exception {
        TrudVsemVacancy vacancy = new TrudVsemVacancy(
                "123", "trudvsem",
                new TrudVsemRegion("78000000", "Санкт-Петербург"),
                new TrudVsemCompany("Cool Company"),
                "2025-01-01T01:00:00Z", "2025-01-01T02:00:00Z",
                60000, 90000, "Java Developer",
                "https://example.com/vacancy/123"
        );

        TrudVsemResults results = new TrudVsemResults(List.of(vacancy));
        TrudVsemMeta meta = new TrudVsemMeta(200L, 1);
        TrudVsemVacanciesResponse response = new TrudVsemVacanciesResponse("200", null, meta, results);

        return mapper.writeValueAsString(response);
    }
}
