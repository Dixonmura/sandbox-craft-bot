package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrudVsemMetaTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Корректные значения пагинации")
    void shouldDeserializeValidMeta() throws JsonProcessingException {
        String json = """
                {"total": 1500, "limit": 50}""";
        TrudVsemMeta meta = mapper.readValue(json, TrudVsemMeta.class);
        assertThat(meta.total()).isEqualTo(1500L);
        assertThat(meta.limit()).isEqualTo(50);
    }

    @Test
    @DisplayName("Нулевые значения")
    void shouldDeserializeZeroValues() throws JsonProcessingException {
        String json = """
                {"total": 0, "limit": 0}""";
        TrudVsemMeta meta = mapper.readValue(json, TrudVsemMeta.class);
        assertThat(meta.total()).isZero();
        assertThat(meta.limit()).isZero();
    }

    @Test
    @DisplayName("Максимальные значения")
    void shouldDeserializeMaxValues() throws JsonProcessingException {
        String json = """
                {"total": 999999999999, "limit": 10000}""";
        TrudVsemMeta meta = mapper.readValue(json, TrudVsemMeta.class);
        assertThat(meta.total()).isEqualTo(999999999999L);
        assertThat(meta.limit()).isEqualTo(10000);
    }

    @Test
    @DisplayName("Некорректный JSON")
    void shouldThrowExceptionOnInvalidJson() {
        String invalidJson = "{total: 12}";
        assertThrows(JsonProcessingException.class,
                () -> mapper.readValue(invalidJson, TrudVsemMeta.class));
    }
}