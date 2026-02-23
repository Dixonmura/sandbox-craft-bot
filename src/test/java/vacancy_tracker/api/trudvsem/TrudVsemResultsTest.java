package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrudVsemResultsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Корректный список вакансий")
    void shouldDeserializeValidVacanciesList() throws JsonProcessingException {
        String json = """
                {"vacancies": [{"id": "123", "job-name": "Java Developer"}]}
                """;
        TrudVsemResults results = mapper.readValue(json, TrudVsemResults.class);
        assertThat(results.vacancies()).hasSize(1);
        assertThat(results.vacancies().get(0).id()).isEqualTo("123");
    }

    @Test
    @DisplayName("Пустой список вакансий")
    void shouldDeserializeEmptyVacanciesList() throws JsonProcessingException {
        String json = """
                {"vacancies": []}""";
        TrudVsemResults results = mapper.readValue(json, TrudVsemResults.class);
        assertThat(results.vacancies()).isEmpty();
    }

    @Test
    @DisplayName("Null список")
    void shouldDeserializeNullVacanciesList() throws JsonProcessingException {
        String json = """
                {"vacancies": null}""";
        TrudVsemResults results = mapper.readValue(json, TrudVsemResults.class);
        assertThat(results.vacancies()).isNull();
    }

    @Test
    @DisplayName("Некорректный JSON")
    void shouldThrowExceptionOnInvalidJson() {
        String invalidJson = "{vacancies: []}";
        assertThrows(JsonProcessingException.class,
                () -> mapper.readValue(invalidJson, TrudVsemResults.class));
    }
}