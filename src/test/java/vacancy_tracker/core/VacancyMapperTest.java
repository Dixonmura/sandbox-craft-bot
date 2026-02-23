package vacancy_tracker.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.api.trudvsem.TrudVsemResults;
import vacancy_tracker.api.trudvsem.TrudVsemVacanciesResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VacancyMapperTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Records → Vacancy список")
    void fromTrudVsemResponse_ShouldMapValidResponse() throws Exception {
        TrudVsemVacanciesResponse response = createValidResponse();

        List<Vacancy> vacancies = VacancyMapper.fromTrudVsemResponse(response);

        assertThat(vacancies).hasSize(1);
        Vacancy vacancy = vacancies.get(0);
        assertThat(vacancy.getTitle()).isEqualTo("Java Developer");
        assertThat(vacancy.getSalaryFrom()).isEqualTo(150000);
    }

    @Test
    @DisplayName("Raw JSON → Vacancy список")
    void fromTrudVsemJson_ShouldMapValidJson() throws Exception {
        String json = """
            {"status": "200", "results": {"vacancies": [{"vacancy": {"id": "123", "job-name": "Java Developer"}}]}}
            """;

        List<Vacancy> vacancies = VacancyMapper.fromTrudVsemJson(json);

        assertThat(vacancies).hasSize(1);
        assertThat(vacancies.get(0).getTitle()).isEqualTo("Java Developer");
    }

    @Test
    @DisplayName("Invalid status → пустой список")
    void fromTrudVsemResponse_ShouldReturnEmptyOnInvalidStatus() throws Exception {
        TrudVsemVacanciesResponse response = createInvalidStatusResponse();

        List<Vacancy> vacancies = VacancyMapper.fromTrudVsemResponse(response);

        assertThat(vacancies).isEmpty();
    }

    @Test
    @DisplayName("Edge Case: Null results → пустой список")
    void fromTrudVsemResponse_ShouldReturnEmptyOnNullResults() throws Exception {
        TrudVsemVacanciesResponse response = createNullResultsResponse();

        List<Vacancy> vacancies = VacancyMapper.fromTrudVsemResponse(response);

        assertThat(vacancies).isEmpty();
    }

    @Test
    @DisplayName("Invalid status → IllegalArgumentException")
    void fromTrudVsemResponse_ShouldThrowOnInvalidStatus() {
        TrudVsemVacanciesResponse response = new TrudVsemVacanciesResponse("400", null, null, null);

        assertThrows(IllegalArgumentException.class,
                () -> VacancyMapper.fromTrudVsemResponse(response));
    }

    @Test
    @DisplayName("Некорректный JSON → пустой список")
    void fromTrudVsemJson_ShouldReturnEmptyOnInvalidJson() {
        String invalidJson = "{invalid}";

        List<Vacancy> vacancies = VacancyMapper.fromTrudVsemJson(invalidJson);

        assertThat(vacancies).isEmpty();
    }

    private static TrudVsemVacanciesResponse createValidResponse() throws Exception {
        String json = """
            {"status": "200", "results": {"vacancies": [{"id": "123", "job-name": "Java Developer", "salary_min": 150000}]}}
            """;
        // TODO: создать полноценный response через mapper
        return mapper.readValue(json, TrudVsemVacanciesResponse.class);
    }

    private static TrudVsemVacanciesResponse createInvalidStatusResponse() {
        return new TrudVsemVacanciesResponse("200", null, null, createValidResults());
    }

    private static TrudVsemVacanciesResponse createNullResultsResponse() {
        return new TrudVsemVacanciesResponse("200", null, null, null);
    }

    private static TrudVsemResults createValidResults() {
        return new TrudVsemResults(List.of());
    }
}