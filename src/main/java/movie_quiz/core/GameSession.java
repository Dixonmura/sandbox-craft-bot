package movie_quiz.core;

import lombok.Data;

import java.util.*;

/**
 * Сессия игры кино-квиза.
 * Хранит очередь фильмов, текущий фильм, счёт и признак завершения.
 */
@Data
public class GameSession {

    private final Queue<Movie> movies;
    private Movie currentMovie;
    private int score;
    private boolean isFinished = false;

    /**
     * Создаёт новую игровую сессию, перемешивая список фильмов.
     *
     * @param listMovies список доступных фильмов
     * @throws IllegalArgumentException если список null или пустой
     */
    public GameSession(List<Movie> listMovies) {
        if (listMovies == null || listMovies.isEmpty()) {
            throw new IllegalArgumentException("Список фильмов не может быть пустым");
        }

        List<Movie> shuffledMovies = new ArrayList<>(listMovies);
        Collections.shuffle(shuffledMovies);

        this.movies = new ArrayDeque<>(shuffledMovies);
        nextMovie();
    }

    /**
     * Переходит к следующему фильму.
     * Помечает сессию завершённой, если фильмы закончились.
     */
    public void nextMovie() {
        currentMovie = movies.poll();
        if (currentMovie == null) {
            isFinished = true;
        }
    }

    /**
     * Увеличивает счёт на единицу.
     */
    public void incrementScore() {
        score++;
    }
}
