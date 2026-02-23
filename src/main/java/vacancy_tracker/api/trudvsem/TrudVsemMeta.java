package vacancy_tracker.api.trudvsem;

/**
 * Метаданные пагинации из ответа API TrudVsem.
 *
 * <p>Содержит общее количество вакансий и размер текущей страницы.</p>
 */
public record TrudVsemMeta(
        long total,
        int limit
) {}