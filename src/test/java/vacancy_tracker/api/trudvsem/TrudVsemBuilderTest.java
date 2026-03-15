package vacancy_tracker.api.trudvsem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.core.UserSettings;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrudVsemUrlBuilderTest {

    private TrudVsemUrlBuilder urlBuilder;
    private final String BASE_URL = "https://opendata.trudvsem.ru/api/v1/vacancies";

    @BeforeEach
    void setUp() {
        urlBuilder = new TrudVsemUrlBuilder();
    }

    private UserSettings createSettings(Integer region, Integer experience, String keyword) {
        return new UserSettings(
                region,
                experience,
                null,
                keyword,
                LocalTime.parse("09:00")
        );
    }

    @Test
    @DisplayName("Базовый URL без фильтров")
    void buildQuery_shouldReturnBaseUrlWithPagination_whenNoFilters() {
        UserSettings settings = createSettings(null, null, null);

        String url = urlBuilder.buildQuery(BASE_URL, settings);

        assertThat(url).isEqualTo(BASE_URL + "?offset=0&limit=100");
    }

    @Test
    @DisplayName("URL с регионом")
    void buildQuery_shouldAddRegionPath_whenRegionIsPresent() {
        UserSettings settings = createSettings(77, null, null);

        String url = urlBuilder.buildQuery(BASE_URL, settings);

        assertThat(url).isEqualTo(BASE_URL + "/region/77?offset=0&limit=100");
    }

    @Test
    @DisplayName("URL с опытом работы")
    void buildQuery_shouldAddExperienceParam_whenExperienceIsPresent() {
        UserSettings settings = createSettings(null, 3, null);

        String url = urlBuilder.buildQuery(BASE_URL, settings);

        assertThat(url).isEqualTo(BASE_URL + "?offset=0&limit=100&experienceFrom=3");
    }

    @Test
    @DisplayName("URL с ключевым словом")
    void buildQuery_shouldAddKeywordParam_whenKeywordIsPresent() {
        UserSettings settings = createSettings(null, null, "Java Developer");

        String url = urlBuilder.buildQuery(BASE_URL, settings);

        assertThat(url).isEqualTo(BASE_URL + "?offset=0&limit=100&text=Java Developer");
    }

    @Test
    @DisplayName("URL со всеми фильтрами")
    void buildQuery_shouldAddAllParams_whenAllFiltersArePresent() {
        UserSettings settings = createSettings(77, 3, "Java Developer");

        String url = urlBuilder.buildQuery(BASE_URL, settings);

        assertThat(url).isEqualTo(BASE_URL + "/region/77?offset=0&limit=100&experienceFrom=3&text=Java Developer");
    }

    @Test
    @DisplayName("Ключевое слово с пробелами не кодируется (API принимает как есть)")
    void buildQuery_shouldNotEncodeKeywordWithSpaces() {
        UserSettings settings = createSettings(null, null, "Java Developer Senior");

        String url = urlBuilder.buildQuery(BASE_URL, settings);

        assertThat(url).contains("text=Java Developer Senior");
        assertThat(url).doesNotContain("%20");
        assertThat(url).doesNotContain("+");
    }

    @Test
    @DisplayName("Пустое ключевое слово игнорируется")
    void buildQuery_shouldIgnoreBlankKeyword() {
        UserSettings settings = createSettings(null, null, "   ");

        String url = urlBuilder.buildQuery(BASE_URL, settings);

        assertThat(url).isEqualTo(BASE_URL + "?offset=0&limit=100");
    }

    @Test
    @DisplayName("Регион 0 добавляется в URL")
    void buildQuery_shouldAddRegion_whenRegionIsZero() {
        UserSettings settings = createSettings(0, null, null);

        String url = urlBuilder.buildQuery(BASE_URL, settings);

        assertThat(url).isEqualTo(BASE_URL + "/region/0?offset=0&limit=100");
    }

    @Test
    @DisplayName("Опыт 0 добавляется в URL")
    void buildQuery_shouldAddExperience_whenExperienceIsZero() {
        UserSettings settings = createSettings(null, 0, null);

        String url = urlBuilder.buildQuery(BASE_URL, settings);

        assertThat(url).isEqualTo(BASE_URL + "?offset=0&limit=100&experienceFrom=0");
    }

    @Test
    @DisplayName("Бросает исключение при null baseUrl")
    void buildQuery_shouldThrowException_whenBaseUrlIsNull() {
        UserSettings settings = createSettings(null, null, null);

        assertThatThrownBy(() -> urlBuilder.buildQuery(null, settings))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("baseUrl не может быть null");
    }

    @Test
    @DisplayName("Бросает исключение при пустом baseUrl")
    void buildQuery_shouldThrowException_whenBaseUrlIsEmpty() {
        UserSettings settings = createSettings(null, null, null);

        assertThatThrownBy(() -> urlBuilder.buildQuery("", settings))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("baseUrl не может быть null");
    }

    @Test
    @DisplayName("Бросает исключение при null userSettings")
    void buildQuery_shouldThrowException_whenUserSettingsIsNull() {
        assertThatThrownBy(() -> urlBuilder.buildQuery(BASE_URL, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userSettings не может быть null");
    }
}