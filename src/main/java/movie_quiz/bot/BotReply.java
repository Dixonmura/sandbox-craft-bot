package movie_quiz.bot;

import java.util.List;

/**
 * Ответ кино-квиза для Telegram-бота.
 * Содержит текст сообщения, варианты фильмов, флаг завершения игры и путь к изображению.
 */
public record BotReply(String text,
                       List<String> movieTitles,
                       boolean isFinished,
                       String imagePath) {
}
