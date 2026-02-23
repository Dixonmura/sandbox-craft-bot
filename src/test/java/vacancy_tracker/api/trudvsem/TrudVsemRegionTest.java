package vacancy_tracker.api.trudvsem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrudVsemRegionTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Корректный регион Москвы")
    void shouldDeserializeValidMoscowRegion() throws JsonProcessingException {
        String json = """
                {"region_code": "77", "name": "Москва"}""";
        TrudVsemRegion region = mapper.readValue(json, TrudVsemRegion.class);
        assertThat(region.regionCode()).isEqualTo("77");
        assertThat(region.name()).isEqualTo("Москва");
    }

    @Test
    @DisplayName("Регион с длинным названием")
    void shouldDeserializeLongRegionName() throws JsonProcessingException {
        String json = """
                {"region_code": "1", "name": "Республика Адыгея (Адыgea)"}""";
        TrudVsemRegion region = mapper.readValue(json, TrudVsemRegion.class);
        assertThat(region.regionCode()).hasSize(1).isEqualTo("1");
        assertThat(region.name()).hasSizeGreaterThan(10);
    }

    @Test
    @DisplayName("Пустые значения")
    void shouldDeserializeEmptyValues() throws JsonProcessingException {
        String json = """
                {"region_code": "", "name": ""}""";
        TrudVsemRegion region = mapper.readValue(json, TrudVsemRegion.class);
        assertThat(region.regionCode()).isEmpty();
        assertThat(region.name()).isEmpty();
    }

    @Test
    @DisplayName("Null значения")
    void shouldDeserializeNullValues() throws JsonProcessingException {
        String json = """
                {"region_code": null, "name": null}""";
        TrudVsemRegion region = mapper.readValue(json, TrudVsemRegion.class);
        assertThat(region.regionCode()).isNull();
        assertThat(region.name()).isNull();
    }

    @Test
    @DisplayName("Некорректный JSON")
    void shouldThrowExceptionOnInvalidJson() {
        String invalidJson = "{region_code: 77}";
        assertThrows(JsonProcessingException.class,
                () -> mapper.readValue(invalidJson, TrudVsemRegion.class));
    }
}