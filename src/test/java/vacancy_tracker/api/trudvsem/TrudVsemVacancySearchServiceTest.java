package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.core.UserSettings;
import vacancy_tracker.core.Vacancy;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrudVsemVacancySearchServiceTest {

    private MockWebServer mockWebServer;
    private TrudVsemVacancySearchService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        OkHttpClient client = new OkHttpClient.Builder().build();
        service = new TrudVsemVacancySearchService(
                client,
                objectMapper,
                mockWebServer.url("/").toString()
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private UserSettings createSettings() {
        return new UserSettings(
                77,
                3,
                100000,
                "Java",
                LocalTime.parse("09:00")
        );
    }

    private String createMockResponse(String... salaries) {
        StringBuilder vacanciesJson = new StringBuilder();
        for (String salary : salaries) {
            vacanciesJson.append("""
                    {
                        "vacancy": {
                            "id": "123",
                            "job-name": "Java Developer",
                            "salary_min": %s,
                            "salary_max": 200000,
                            "vac_url": "https://test.ru",
                            "company": {"name": "Test Company"},
                            "region": {"region_code": "77"},
                            "requirement": {"experience": 3}
                        }
                    },""".formatted(salary));
        }
        if (vacanciesJson.length() > 0) {
            vacanciesJson.setLength(vacanciesJson.length() - 1);
        }

        return """
                {
                    "status": "200",
                    "meta": {"total": 10, "limit": 100},
                    "results": {
                        "vacancies": [%s]
                    }
                }
                """.formatted(vacanciesJson.toString());
    }

    @Test
    @DisplayName("Успешный поиск возвращает список вакансий")
    void findVacancies_shouldReturnVacancies_whenResponseIsValid() {
        String jsonResponse = createMockResponse("150000", "120000", "80000");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse));

        List<Vacancy> vacancies = service.findVacancies(createSettings());

        assertThat(vacancies).hasSize(2);
        assertThat(vacancies.get(0).salaryFrom()).isEqualTo(150000);
        assertThat(vacancies.get(1).salaryFrom()).isEqualTo(120000);
    }

    @Test
    @DisplayName("Возвращает пустой список при статусе не 200")
    void findVacancies_shouldReturnEmptyList_whenStatusIsNot200() {
        String jsonResponse = """
                {
                    "status": "400",
                    "meta": {"total": 0, "limit": 100},
                    "results": {"vacancies": []}
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse));

        List<Vacancy> vacancies = service.findVacancies(createSettings());

        assertThat(vacancies).isEmpty();
    }

    @Test
    @DisplayName("Возвращает пустой список при ошибке HTTP")
    void findVacancies_shouldReturnEmptyList_whenHttpError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        List<Vacancy> vacancies = service.findVacancies(createSettings());

        assertThat(vacancies).isEmpty();
    }

    @Test
    @DisplayName("Возвращает пустой список при пустом ответе")
    void findVacancies_shouldReturnEmptyList_whenNoVacancies() {
        String jsonResponse = """
                {
                    "status": "200",
                    "meta": {"total": 0, "limit": 100},
                    "results": {"vacancies": []}
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse));

        List<Vacancy> vacancies = service.findVacancies(createSettings());

        assertThat(vacancies).isEmpty();
    }

    @Test
    @DisplayName("Фильтр по зарплате работает корректно")
    void findVacancies_shouldApplySalaryFilter() {
        String jsonResponse = createMockResponse("150000", "90000", "110000", "50000");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse));

        UserSettings settings = createSettings();
        List<Vacancy> vacancies = service.findVacancies(settings);

        assertThat(vacancies).hasSize(2);
        assertThat(vacancies.stream().map(Vacancy::salaryFrom))
                .containsExactlyInAnyOrder(150000, 110000);
    }

    @Test
    @DisplayName("Фильтр отключён при salaryFrom = 0")
    void findVacancies_shouldNotFilter_whenSalaryFromIsZero() {
        String jsonResponse = createMockResponse("150000", "90000", "50000");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse));

        UserSettings settings = createSettings();
        settings.updateSalaryFrom(0);
        List<Vacancy> vacancies = service.findVacancies(settings);

        assertThat(vacancies).hasSize(3);
    }

    @Test
    @DisplayName("Вакансии с null зарплатой проходят фильтр")
    void findVacancies_shouldIncludeVacanciesWithNullSalary() {
        String jsonResponse = """
                {
                    "status": "200",
                    "meta": {"total": 3, "limit": 100},
                    "results": {
                        "vacancies": [
                            {
                                "vacancy": {
                                    "id": "1",
                                    "job-name": "Job 1",
                                    "company": {"name": "Test"},
                                    "region": {"region_code": "77"},
                                    "requirement": {"experience": 3}
                                }
                            },
                            {
                                "vacancy": {
                                    "id": "2",
                                    "job-name": "Job 2",
                                    "salary_min": 150000,
                                    "company": {"name": "Test"},
                                    "region": {"region_code": "77"},
                                    "requirement": {"experience": 3}
                                }
                            }
                        ]
                    }
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(jsonResponse));

        UserSettings settings = createSettings();
        List<Vacancy> vacancies = service.findVacancies(settings);

        assertThat(vacancies).hasSize(2);
    }

    @Test
    @DisplayName("Бросает исключение при null settings")
    void findVacancies_shouldThrowException_whenSettingsIsNull() {
        assertThatThrownBy(() -> service.findVacancies(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userSettings не может быть null");
    }

    @Test
    @DisplayName("Обрабатывает некорректный JSON без ошибок")
    void findVacancies_shouldHandleInvalidJson() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("Это не JSON"));

        List<Vacancy> vacancies = service.findVacancies(createSettings());

        assertThat(vacancies).isEmpty();
    }
}