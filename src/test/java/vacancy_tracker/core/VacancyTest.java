package vacancy_tracker.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class VacancyTest {

    @Test
    void constructor_ShouldFillAllFields() {
        Instant now = Instant.now();

        Vacancy vacancy = new Vacancy(
                18L,
                "id-93",
                "Java Developer",
                "SkillBox",
                100_000,
                150_000,
                65,
                "https://example.com/vacancy/1",
                now,
                "trudvsem"
        );

        assertThat(vacancy.getId()).isEqualTo(18L);
        assertThat(vacancy.getExternalId()).isEqualTo("id-93");
        assertThat(vacancy.getTitle()).isEqualTo("Java Developer");
        assertThat(vacancy.getCompany()).isEqualTo("SkillBox");
        assertThat(vacancy.getSalaryFrom()).isEqualTo(100_000);
        assertThat(vacancy.getSalaryTo()).isEqualTo(150_000);
        assertThat(vacancy.getRegionCode()).isEqualTo(65);
        assertThat(vacancy.getUrl()).isEqualTo("https://example.com/vacancy/1");
        assertThat(vacancy.getPublishedAt()).isEqualTo(now);
        assertThat(vacancy.getSource()).isEqualTo("trudvsem");
    }
}