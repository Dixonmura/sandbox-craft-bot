package movie_quiz.service;

import movie_quiz.core.QuestionMovie;
import movie_quiz.core.GameSession;
import movie_quiz.core.Movie;
import movie_quiz.core.QuestionView;

import java.util.List;
import java.util.Optional;

/**
 * Управляет игровой сессией кино-квиза:
 * выдаёт вопросы, проверяет ответы и считает очки.
 */
public class GameManager {

    private final GameSession gameSession;
    private final QuestionMovie questionMovie;

    /**
     * Создаёт менеджер игры на основе списка фильмов.
     *
     * @param movieList список доступных фильмов для вопросов
     */
    public GameManager(List<Movie> movieList) {
        this.gameSession = new GameSession(movieList);
        this.questionMovie = new QuestionMovie(movieList);
    }

    /**
     * Возвращает следующий вопрос или пустой {@link Optional},
     * если игра завершена.
     *
     * @return следующий вопрос или пустой Optional при завершении игры
     */
    public Optional<QuestionView> getNextQuestion() {
        if (gameSession.isFinished()) {
            return Optional.empty();
        }
        return Optional.of(new QuestionView(questionMovie.getQuestions(gameSession)));
    }

    /**
     * Проверяет ответ пользователя на текущий вопрос.
     * Увеличивает счёт и переходит к следующему фильму при верном ответе.
     *
     * @param answer ответ пользователя
     * @return true, если ответ верный, иначе false
     * @throws IllegalStateException если вопрос ещё не был выдан
     */
    public boolean checkAnswer(String answer) {
        if (questionMovie.getCurrentMovie() == null) {
            throw new IllegalStateException("Нельзя проверять ответ до выдачи вопроса");
        }
        boolean currentAnswer = questionMovie.getAnswer(answer);
        if (currentAnswer) {
            gameSession.incrementScore();
        }
        gameSession.nextMovie();
        return currentAnswer;
    }

    /**
     * Возвращает текущий фильм в игре.
     *
     * @return текущий фильм
     */
    public Movie getCurrentMovie() {
        return gameSession.getCurrentMovie();
    }

    /**
     * Возвращает правильный ответ для текущего вопроса.
     *
     * @return правильное название фильма
     */
    public String getRightAnswer() {
        return questionMovie.getCorrectTitle();
    }

    /**
     * Возвращает текущий счёт игрока.
     *
     * @return количество правильных ответов
     */
    public int getScore() {
        return gameSession.getScore();
    }
}
