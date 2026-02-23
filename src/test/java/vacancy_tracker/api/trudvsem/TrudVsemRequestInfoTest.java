package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrudVsemRequestInfoTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Корректный API URL")
    void shouldDeserializeValidApiUrl() throws JsonProcessingException {
        String json = """
                {"api": "https://opendata.trudvsem.ru/api/v1/vacancies"}""";
        TrudVsemRequestInfo info = mapper.readValue(json, TrudVsemRequestInfo.class);
        assertThat(info.api()).isEqualTo("https://opendata.trudvsem.ru/api/v1/vacancies");
    }

    @Test
    @DisplayName("Простой API путь")
    void shouldDeserializeSimpleApiPath() throws JsonProcessingException {
        String json = """
                {"api": "/api/v1/vacancies/region/77"}""";
        TrudVsemRequestInfo info = mapper.readValue(json, TrudVsemRequestInfo.class);
        assertThat(info.api()).isEqualTo("/api/v1/vacancies/region/77");
    }

    @Test
    @DisplayName("Пустая строка")
    void shouldDeserializeEmptyString() throws JsonProcessingException {
        String json = """
                {"api": ""}""";
        TrudVsemRequestInfo info = mapper.readValue(json, TrudVsemRequestInfo.class);
        assertThat(info.api()).isEmpty();
    }

    @Test
    @DisplayName("Null значение")
    void shouldDeserializeNullValue() throws JsonProcessingException {
        String json = """
                {"api": null}""";
        TrudVsemRequestInfo info = mapper.readValue(json, TrudVsemRequestInfo.class);
        assertThat(info.api()).isNull();
    }

    @Test
    @DisplayName("Некорректный JSON")
    void shouldThrowExceptionOnInvalidJson() {
        String invalidJson = "{api: /vacancies}";
        assertThrows(JsonProcessingException.class,
                () -> mapper.readValue(invalidJson, TrudVsemRequestInfo.class));
    }
}