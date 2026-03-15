package vacancy_tracker.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VacancyTest {

    @Test
    void constructor_ShouldFillAllFields() {

        Vacancy vacancy = new Vacancy(
                "id-93",
                "Java Developer",
                "SkillBox",
                100_000,
                150_000,
                65,
                2,
                "https://example.com/vacancy/1"
        );

        assertThat(vacancy.externalId()).isEqualTo("id-93");
        assertThat(vacancy.title()).isEqualTo("Java Developer");
        assertThat(vacancy.company()).isEqualTo("SkillBox");
        assertThat(vacancy.salaryFrom()).isEqualTo(100_000);
        assertThat(vacancy.salaryTo()).isEqualTo(150_000);
        assertThat(vacancy.regionCode()).isEqualTo(65);
        assertThat(vacancy.experience()).isEqualTo(2);
        assertThat(vacancy.url()).isEqualTo("https://example.com/vacancy/1");
    }
}