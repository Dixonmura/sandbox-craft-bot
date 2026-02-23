package vacancy_tracker.api.trudvsem;

/**
 * Информация о запросе из ответа API TrudVsem.
 *
 * <p>Содержит URL или идентификатор использованного API-эндпоинта.</p>
 */
public record TrudVsemRequestInfo(
        String api
) {}
