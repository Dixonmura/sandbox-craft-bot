package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrudVsemCompanyTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Корректный JSON с названием компании")
    void shouldDeserializeValidCompanyName() throws JsonProcessingException {
        String json = """
                {"name": "Яндекс"}""";
        TrudVsemCompany company = mapper.readValue(json, TrudVsemCompany.class);
        assertThat(company.name()).isEqualTo("Яндекс");
    }

    @Test
    @DisplayName("Пустая строка в качестве названия")
    void shouldDeserializeEmptyString() throws JsonProcessingException {
        String json = """
                {"name": ""}""";
        TrudVsemCompany company = mapper.readValue(json, TrudVsemCompany.class);
        assertThat(company.name()).isEmpty();
    }

    @Test
    @DisplayName("Null значение в JSON")
    void shouldDeserializeNullName() throws JsonProcessingException {
        String json = """
                {"name": null}""";
        TrudVsemCompany company = mapper.readValue(json, TrudVsemCompany.class);
        assertThat(company.name()).isNull();
    }

    @Test
    @DisplayName("Проверка пограничного сценария, когда название со пробелами")
    void shouldPreserveWhitespaceInName() throws JsonProcessingException {
        String json = """
                {"name": "  СберТех  "}""";
        TrudVsemCompany company = mapper.readValue(json, TrudVsemCompany.class);
        assertThat(company.name()).isEqualTo("  СберТех  ");
    }

    @Test
    @DisplayName("Проверка выброса исключения при некорректном JSON")
    void shouldThrowExceptionOnInvalidJson() {
        String invalidJson = """
                {"name": }""";
        assertThrows(JsonProcessingException.class,
                () -> mapper.readValue(invalidJson, TrudVsemCompany.class));
    }
}