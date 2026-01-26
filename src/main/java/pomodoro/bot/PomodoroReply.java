package pomodoro.bot;

/**
 * Запрос/ответ для Телеграм-бота
 *
 * @param text       текст сообщения
 * @param imagePath  мотивационное фото
 * @param isFinished проверяет активна ли сессия (для управления клавиатурой)
 */
public record PomodoroReply(String text, String imagePath, boolean isFinished
) {
}
