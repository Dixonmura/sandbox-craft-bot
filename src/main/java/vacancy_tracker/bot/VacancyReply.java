package vacancy_tracker.bot;

import markups.VacancyKeyboardKey;

/**
 * Ответ пользователю на его действие в Telegram
 */
public record VacancyReply(Long userId, String text, VacancyKeyboardKey keyboardKey) {
}
