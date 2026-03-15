package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrudVsemResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    @DisplayName("Десериализация полного ответа с одной вакансией")
    void shouldDeserializeFullResponse() throws Exception {
        String json = """
                {
                    "status": "200",
                    "meta": {
                        "total": 1000,
                        "limit": 100
                    },
                    "results": {
                        "vacancies": [
                            {
                                "vacancy": {
                                    "id": "12345",
                                    "job-name": "Java Developer",
                                    "salary_min": 150000,
                                    "salary_max": 250000,
                                    "vac_url": "https://trudvsem.ru/vacancy/12345",
                                    "region": {
                                        "region_code": "77",
                                        "name": "Москва"
                                    },
                                    "company": {
                                        "name": "ООО Ромашка"
                                    },
                                    "requirement": {
                                        "experience": 3
                                    }
                                }
                            }
                        ]
                    }
                }
                """;

        TrudVsemResponse response = objectMapper.readValue(json, TrudVsemResponse.class);

        assertThat(response.getStatus()).isEqualTo("200");
        assertThat(response.getMeta().getTotal()).isEqualTo(1000);
        assertThat(response.getMeta().getLimit()).isEqualTo(100);

        assertThat(response.getResults()).isNotNull();
        assertThat(response.getResults().getVacancies()).hasSize(1);

        TrudVsemResponse.VacancyDto vacancy = response.getResults().getVacancies().get(0).getVacancy();
        assertThat(vacancy.getId()).isEqualTo("12345");
        assertThat(vacancy.getJobName()).isEqualTo("Java Developer");
        assertThat(vacancy.getSalaryMin()).isEqualTo(150000);
        assertThat(vacancy.getSalaryMax()).isEqualTo(250000);
        assertThat(vacancy.getVacancyUrl()).isEqualTo("https://trudvsem.ru/vacancy/12345");

        assertThat(vacancy.getRegion()).isNotNull();
        assertThat(vacancy.getRegion().getRegionCode()).isEqualTo("77");
        assertThat(vacancy.getRegion().getName()).isEqualTo("Москва");

        assertThat(vacancy.getCompany()).isNotNull();
        assertThat(vacancy.getCompany().getName()).isEqualTo("ООО Ромашка");

        assertThat(vacancy.getRequirement()).isNotNull();
        assertThat(vacancy.getRequirement().getExperience()).isEqualTo(3);
    }

    @Test
    @DisplayName("Десериализация ответа с несколькими вакансиями")
    void shouldDeserializeMultipleVacancies() throws Exception {
        String json = """
                {
                    "status": "200",
                    "meta": {
                        "total": 2,
                        "limit": 100
                    },
                    "results": {
                        "vacancies": [
                            {
                                "vacancy": {
                                    "id": "1",
                                    "job-name": "Java Dev",
                                    "company": {"name": "Company A"}
                                }
                            },
                            {
                                "vacancy": {
                                    "id": "2",
                                    "job-name": "Python Dev",
                                    "company": {"name": "Company B"}
                                }
                            }
                        ]
                    }
                }
                """;

        TrudVsemResponse response = objectMapper.readValue(json, TrudVsemResponse.class);

        assertThat(response.getResults().getVacancies()).hasSize(2);

        TrudVsemResponse.VacancyDto first = response.getResults().getVacancies().get(0).getVacancy();
        assertThat(first.getId()).isEqualTo("1");
        assertThat(first.getJobName()).isEqualTo("Java Dev");

        TrudVsemResponse.VacancyDto second = response.getResults().getVacancies().get(1).getVacancy();
        assertThat(second.getId()).isEqualTo("2");
        assertThat(second.getJobName()).isEqualTo("Python Dev");
    }

    @Test
    @DisplayName("Десериализация с минимальными полями")
    void shouldDeserializeWithMinimalFields() throws Exception {
        String json = """
                {
                    "status": "200",
                    "results": {
                        "vacancies": [
                            {
                                "vacancy": {
                                    "id": "123"
                                }
                            }
                        ]
                    }
                }
                """;

        TrudVsemResponse response = objectMapper.readValue(json, TrudVsemResponse.class);

        assertThat(response.getStatus()).isEqualTo("200");
        assertThat(response.getMeta()).isNull();
        assertThat(response.getResults().getVacancies()).hasSize(1);

        TrudVsemResponse.VacancyDto vacancy = response.getResults().getVacancies().get(0).getVacancy();
        assertThat(vacancy.getId()).isEqualTo("123");
        assertThat(vacancy.getJobName()).isNull();
        assertThat(vacancy.getSalaryMin()).isNull();
        assertThat(vacancy.getRegion()).isNull();
    }

    @Test
    @DisplayName("Игнорирует неизвестные поля")
    void shouldIgnoreUnknownFields() throws Exception {
        String json = """
                {
                    "status": "200",
                    "unknown_field": "something",
                    "meta": {
                        "total": 100,
                        "unknown_meta": "ignore"
                    },
                    "results": {
                        "vacancies": [
                            {
                                "vacancy": {
                                    "id": "123",
                                    "unknown_vacancy": "field"
                                }
                            }
                        ],
                        "extra_data": "ignored"
                    }
                }
                """;

        TrudVsemResponse response = objectMapper.readValue(json, TrudVsemResponse.class);

        assertThat(response.getStatus()).isEqualTo("200");
        assertThat(response.getMeta().getTotal()).isEqualTo(100);
        assertThat(response.getResults().getVacancies()).hasSize(1);
        assertThat(response.getResults().getVacancies().get(0).getVacancy().getId()).isEqualTo("123");
    }

    @Test
    @DisplayName("Обрабатывает null значения")
    void shouldHandleNullValues() throws Exception {
        String json = """
                {
                    "status": null,
                    "meta": null,
                    "results": null
                }
                """;

        TrudVsemResponse response = objectMapper.readValue(json, TrudVsemResponse.class);

        assertThat(response.getStatus()).isNull();
        assertThat(response.getMeta()).isNull();
        assertThat(response.getResults()).isNull();
    }

    @Test
    @DisplayName("Обрабатывает пустой список вакансий")
    void shouldHandleEmptyVacanciesList() throws Exception {
        String json = """
                {
                    "status": "200",
                    "results": {
                        "vacancies": []
                    }
                }
                """;

        TrudVsemResponse response = objectMapper.readValue(json, TrudVsemResponse.class);

        assertThat(response.getResults().getVacancies()).isEmpty();
    }
}