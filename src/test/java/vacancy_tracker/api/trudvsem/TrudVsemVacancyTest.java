package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrudVsemVacancyTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Полная корректная вакансия")
    void shouldDeserializeFullValidVacancy() throws JsonProcessingException {
        String json = """
            {
                "id": "123",
                "source": "trudvsem",
                "region": {"region_code": "77", "name": "Москва"},
                "company": {"name": "Яндекс"},
                "creation-date": "2026-02-22",
                "date_modify": "2026-02-22",
                "salary_min": 150000,
                "salary_max": 300000,
                "job-name": "Java Developer",
                "vac_url": "https://trudvsem.ru/vacancy/123"
            }
            """;
        TrudVsemVacancy vacancy = mapper.readValue(json, TrudVsemVacancy.class);

        assertThat(vacancy.id()).isEqualTo("123");
        assertThat(vacancy.jobName()).isEqualTo("Java Developer");
        assertThat(vacancy.salaryMin()).isEqualTo(150000);
        assertThat(vacancy.company().name()).isEqualTo("Яндекс");
    }

    @Test
    @DisplayName("Вакансия без зарплаты")
    void shouldDeserializeVacancyWithoutSalary() throws JsonProcessingException {
        String json = """
            {
                "id": "456",
                "source": "trudvsem",
                "region": {"region_code": "77", "name": "Москва"},
                "company": {"name": "Сбер"},
                "creation-date": "2026-02-22",
                "date_modify": "2026-02-22",
                "salary_min": null,
                "salary_max": null,
                "job-name": "Backend Developer",
                "vac_url": "https://trudvsem.ru/vacancy/456"
            }
            """;
        TrudVsemVacancy vacancy = mapper.readValue(json, TrudVsemVacancy.class);

        assertThat(vacancy.salaryMin()).isNull();
        assertThat(vacancy.salaryMax()).isNull();
    }

    @Test
    @DisplayName("Null вложенные объекты")
    void shouldDeserializeWithNullNestedObjects() throws JsonProcessingException {
        String json = """
            {
                "id": "789",
                "source": null,
                "region": null,
                "company": null,
                "creation-date": null,
                "date_modify": null,
                "salary_min": null,
                "salary_max": null,
                "job-name": null,
                "vac_url": null
            }
            """;
        TrudVsemVacancy vacancy = mapper.readValue(json, TrudVsemVacancy.class);

        assertThat(vacancy.region()).isNull();
        assertThat(vacancy.company()).isNull();
    }

    @Test
    @DisplayName("Некорректный JSON")
    void shouldThrowExceptionOnInvalidJson() {
        String invalidJson = "{id: 123}";
        assertThrows(JsonProcessingException.class,
                () -> mapper.readValue(invalidJson, TrudVsemVacancy.class));
    }
}