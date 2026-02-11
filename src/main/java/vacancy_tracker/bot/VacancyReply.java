package vacancy_tracker.bot;

/**
 * Ответ пользователю на его действие в Telegram
 */
public record VacancyReply(Long userId, String text) {
}
