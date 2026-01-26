package movie_quiz.core;

import java.util.List;

/**
 * Представление вопроса кино-квиза.
 * Содержит список вариантов названий фильмов для ответа.
 */
public record QuestionView(List<String> movieTitles) {
}
