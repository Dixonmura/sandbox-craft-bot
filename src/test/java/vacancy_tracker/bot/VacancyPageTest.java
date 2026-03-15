package vacancy_tracker.bot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.core.Vacancy;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class VacancyPageTest {

    private Vacancy createVacancy(String id) {
        return new Vacancy(
                id,
                "Title " + id,
                "Company " + id,
                100000,
                150000,
                77,
                3,
                "https://test.ru/" + id
        );
    }

    private List<Vacancy> createVacancies(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createVacancy("id" + i))
                .toList();
    }

    @Test
    @DisplayName("Создание первой страницы")
    void constructor_shouldCreateFirstPage() {
        List<Vacancy> vacancies = createVacancies(25);
        VacancyPage page = new VacancyPage(123L, vacancies);

        assertThat(page.getUserId()).isEqualTo(123L);
        assertThat(page.getAllVacancies()).hasSize(25);
        assertThat(page.getCurrentPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Расчёт количества страниц")
    void getTotalPages_shouldCalculateCorrectly() {
        assertThat(new VacancyPage(1L, createVacancies(0)).getTotalPages()).isEqualTo(0);
        assertThat(new VacancyPage(1L, createVacancies(5)).getTotalPages()).isEqualTo(1);
        assertThat(new VacancyPage(1L, createVacancies(10)).getTotalPages()).isEqualTo(1);
        assertThat(new VacancyPage(1L, createVacancies(11)).getTotalPages()).isEqualTo(2);
        assertThat(new VacancyPage(1L, createVacancies(20)).getTotalPages()).isEqualTo(2);
        assertThat(new VacancyPage(1L, createVacancies(21)).getTotalPages()).isEqualTo(3);
        assertThat(new VacancyPage(1L, createVacancies(25)).getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("Получение вакансий текущей страницы")
    void getCurrentVacancies_shouldReturnCorrectSlice() {
        List<Vacancy> vacancies = createVacancies(25);

        VacancyPage page0 = new VacancyPage(1L, vacancies, 0);
        assertThat(page0.getCurrentVacancies()).hasSize(10);
        assertThat(page0.getCurrentVacancies().get(0).externalId()).isEqualTo("id0");
        assertThat(page0.getCurrentVacancies().get(9).externalId()).isEqualTo("id9");

        VacancyPage page1 = new VacancyPage(1L, vacancies, 1);
        assertThat(page1.getCurrentVacancies()).hasSize(10);
        assertThat(page1.getCurrentVacancies().get(0).externalId()).isEqualTo("id10");
        assertThat(page1.getCurrentVacancies().get(9).externalId()).isEqualTo("id19");

        VacancyPage page2 = new VacancyPage(1L, vacancies, 2);
        assertThat(page2.getCurrentVacancies()).hasSize(5);
        assertThat(page2.getCurrentVacancies().get(0).externalId()).isEqualTo("id20");
        assertThat(page2.getCurrentVacancies().get(4).externalId()).isEqualTo("id24");
    }

    @Test
    @DisplayName("Получение вакансий при пустом списке")
    void getCurrentVacancies_shouldReturnEmptyList_whenNoVacancies() {
        VacancyPage page = new VacancyPage(1L, List.of(), 0);

        assertThat(page.getCurrentVacancies()).isEmpty();
        assertThat(page.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("Проверка наличия следующей страницы")
    void hasNext_shouldWorkCorrectly() {
        VacancyPage page0 = new VacancyPage(1L, createVacancies(25), 0);
        assertThat(page0.hasNext()).isTrue();

        VacancyPage page1 = new VacancyPage(1L, createVacancies(25), 1);
        assertThat(page1.hasNext()).isTrue();

        VacancyPage page2 = new VacancyPage(1L, createVacancies(25), 2);
        assertThat(page2.hasNext()).isFalse();

        VacancyPage lastPage = new VacancyPage(1L, createVacancies(10), 0);
        assertThat(lastPage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("Проверка наличия предыдущей страницы")
    void hasPrev_shouldWorkCorrectly() {
        VacancyPage page0 = new VacancyPage(1L, createVacancies(25), 0);
        assertThat(page0.hasPrev()).isFalse();

        VacancyPage page1 = new VacancyPage(1L, createVacancies(25), 1);
        assertThat(page1.hasPrev()).isTrue();

        VacancyPage page2 = new VacancyPage(1L, createVacancies(25), 2);
        assertThat(page2.hasPrev()).isTrue();
    }

    @Test
    @DisplayName("Переход на следующую страницу")
    void nextPage_shouldCreateNextPage() {
        VacancyPage page = new VacancyPage(123L, createVacancies(25), 1);
        VacancyPage next = page.nextPage();

        assertThat(next.getUserId()).isEqualTo(123L);
        assertThat(next.getAllVacancies()).isSameAs(page.getAllVacancies());
        assertThat(next.getCurrentPage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Переход на предыдущую страницу")
    void prevPage_shouldCreatePrevPage() {
        VacancyPage page = new VacancyPage(123L, createVacancies(25), 1);
        VacancyPage prev = page.prevPage();

        assertThat(prev.getUserId()).isEqualTo(123L);
        assertThat(prev.getAllVacancies()).isSameAs(page.getAllVacancies());
        assertThat(prev.getCurrentPage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Граничные значения: ровно 10 вакансий")
    void shouldHandleExactlyTenVacancies() {
        VacancyPage page = new VacancyPage(1L, createVacancies(10));

        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getCurrentVacancies()).hasSize(10);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.hasPrev()).isFalse();
    }

    @Test
    @DisplayName("Граничные значения: 1 вакансия")
    void shouldHandleSingleVacancy() {
        VacancyPage page = new VacancyPage(1L, createVacancies(1));

        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getCurrentVacancies()).hasSize(1);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.hasPrev()).isFalse();
    }
}