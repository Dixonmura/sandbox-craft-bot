package vacancy_tracker.bot;

import lombok.Data;
import vacancy_tracker.core.Vacancy;
import java.util.List;

/**
 * Модель страницы для постраничного просмотра вакансий в Telegram.
 * <p>
 * Хранит все вакансии пользователя и текущую страницу.
 * Позволяет получать вакансии текущей страницы, общее количество страниц
 * и создавать объекты для перехода на следующую/предыдущую страницу.
 * <p>
 * Размер страницы фиксирован - {@value #PAGE_SIZE} вакансий.
 */
@Data
public class VacancyPage {

    /** Количество вакансий на одной странице */
    private static final int PAGE_SIZE = 10;

    /** ID пользователя Telegram */
    private final Long userId;

    /** Все вакансии пользователя (полный список) */
    private final List<Vacancy> allVacancies;

    /** Номер текущей страницы (начиная с 0) */
    private final int currentPage;

    /**
     * Создаёт первую страницу (currentPage = 0) для пользователя.
     *
     * @param userId        ID пользователя
     * @param allVacancies  полный список вакансий
     */
    public VacancyPage(Long userId, List<Vacancy> allVacancies) {
        this(userId, allVacancies, 0);
    }

    /**
     * Создаёт страницу с указанным номером.
     *
     * @param userId        ID пользователя
     * @param allVacancies  полный список вакансий
     * @param currentPage   номер текущей страницы
     */
    public VacancyPage(Long userId, List<Vacancy> allVacancies, int currentPage) {
        this.userId = userId;
        this.allVacancies = allVacancies;
        this.currentPage = currentPage;
    }

    /**
     * Возвращает общее количество страниц.
     *
     * @return количество страниц (минимум 1)
     */
    public int getTotalPages() {
        return (int) Math.ceil((double) allVacancies.size() / PAGE_SIZE);
    }

    /**
     * Возвращает список вакансий для текущей страницы.
     *
     * @return список из {@value #PAGE_SIZE} или меньше вакансий
     */
    public List<Vacancy> getCurrentVacancies() {
        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, allVacancies.size());
        return allVacancies.subList(from, to);
    }

    /**
     * Проверяет, существует ли следующая страница.
     *
     * @return true если есть следующая страница
     */
    public boolean hasNext() {
        return (currentPage + 1) * PAGE_SIZE < allVacancies.size();
    }

    /**
     * Проверяет, существует ли предыдущая страница.
     *
     * @return true если есть предыдущая страница
     */
    public boolean hasPrev() {
        return currentPage > 0;
    }

    /**
     * Создаёт новый объект для следующей страницы.
     *
     * @return объект следующей страницы
     */
    public VacancyPage nextPage() {
        return new VacancyPage(userId, allVacancies, currentPage + 1);
    }

    /**
     * Создаёт новый объект для предыдущей страницы.
     *
     * @return объект предыдущей страницы
     */
    public VacancyPage prevPage() {
        return new VacancyPage(userId, allVacancies, currentPage - 1);
    }
}