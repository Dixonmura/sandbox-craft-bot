package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrudVsemVacanciesResponseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Полный корректный ответ API")
    void shouldDeserializeFullValidResponse() throws JsonProcessingException {
        String json = """
                {
                    "status": "success",
                    "request": {"api": "/api/v1/vacancies"},
                    "meta": {"total": 1500, "limit": 50},
                    "results": {"vacancies": [{"id": "123", "job-name": "Java Developer"}]}
                }
                """;
        TrudVsemVacanciesResponse response = mapper.readValue(json, TrudVsemVacanciesResponse.class);

        assertThat(response.status()).isEqualTo("success");
        assertThat(response.request().api()).isEqualTo("/api/v1/vacancies");
        assertThat(response.meta().total()).isEqualTo(1500L);
        assertThat(response.results().vacancies()).hasSize(1);
    }

    @Test
    @DisplayName("Минимальный ответ с пустыми результатами")
    void shouldDeserializeMinimalResponse() throws JsonProcessingException {
        String json = """
                {
                    "status": "success",
                    "request": {"api": "/api/v1/vacancies"},
                    "meta": {"total": 0, "limit": 50},
                    "results": {"vacancies": []}
                }
                """;
        TrudVsemVacanciesResponse response = mapper.readValue(json, TrudVsemVacanciesResponse.class);

        assertThat(response.status()).isEqualTo("success");
        assertThat(response.results().vacancies()).isEmpty();
    }

    @Test
    @DisplayName("Null значения в полях")
    void shouldDeserializeWithNullFields() throws JsonProcessingException {
        String json = """
                {
                    "status": null,
                    "request": null,
                    "meta": null,
                    "results": null
                }
                """;
        TrudVsemVacanciesResponse response = mapper.readValue(json, TrudVsemVacanciesResponse.class);

        assertThat(response.status()).isNull();
        assertThat(response.request()).isNull();
        assertThat(response.meta()).isNull();
        assertThat(response.results()).isNull();
    }

    @Test
    @DisplayName("Некорректный JSON")
    void shouldThrowExceptionOnInvalidJson() {
        String invalidJson = "{status: success}";
        assertThrows(JsonProcessingException.class,
                () -> mapper.readValue(invalidJson, TrudVsemVacanciesResponse.class));
    }
}