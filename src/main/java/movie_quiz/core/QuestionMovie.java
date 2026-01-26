package movie_quiz.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Формирует вопросы киноквиза по списку фильмов.
 * Выдаёт варианты ответов и проверяет правильность ответа.
 */
public class QuestionMovie {

    private final List<Movie> listTitles;
    private Movie currentMovie;

    /**
     * Создаёт генератор вопросов на основе списка фильмов.
     *
     * @param listTitles список фильмов для построения вариантов ответа
     * @throws IllegalArgumentException если список null или пустой
     */
    public QuestionMovie(List<Movie> listTitles) {
        if (listTitles == null || listTitles.isEmpty()) {
            throw new IllegalArgumentException("Список фильмов для построения вопросов не может быть пустым");
        }
        this.listTitles = new ArrayList<>(listTitles);
    }

    /**
     * Формирует список вариантов ответа для текущего фильма из сессии.
     * Варианты включают правильный ответ и до трёх случайных других названий.
     *
     * @param session текущая игровая сессия
     * @return список вариантов ответа (обычно 4 элемента)
     * @throws IllegalStateException если в сессии нет текущего фильма
     */
    public List<String> getQuestions(GameSession session) {
        if (session.getCurrentMovie() == null) {
            throw new IllegalStateException("Нельзя формировать варианты без текущего фильма в сессии");
        }

        List<String> questions = new ArrayList<>();
        currentMovie = session.getCurrentMovie();

        questions.add(currentMovie.title());

        ThreadLocalRandom random = ThreadLocalRandom.current();

        while (questions.size() < 4 && questions.size() < listTitles.size()) {
            int index = random.nextInt(listTitles.size());
            Movie candidate = listTitles.get(index);

            String title = candidate.title();
            if (!title.equals(currentMovie.title()) && !questions.contains(title)) {
                questions.add(title);
            }
        }

        Collections.shuffle(questions);
        return questions;
    }

    /**
     * Проверяет, совпадает ли переданное название с текущим фильмом.
     *
     * @param title ответ пользователя
     * @return true, если ответ верный, иначе false
     * @throws IllegalStateException если текущий фильм не установлен
     */
    public boolean getAnswer(String title) {
        if (currentMovie == null) {
            throw new IllegalStateException("Текущий фильм не установлен, невозможно проверить ответ");
        }
        return currentMovie.title().equalsIgnoreCase(title);
    }

    /**
     * Возвращает правильное название текущего фильма.
     *
     * @return правильное название фильма
     * @throws IllegalStateException если текущий фильм не установлен
     */
    public String getCorrectTitle() {
        if (currentMovie == null) {
            throw new IllegalStateException("Текущий фильм не установлен");
        }
        return currentMovie.title();
    }

    public Movie getCurrentMovie() {
        return currentMovie;
    }
}
