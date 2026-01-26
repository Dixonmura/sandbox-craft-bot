package movie_quiz.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionMovieTest {

    @Test
    @DisplayName("Проверка метода получения списка вопросов при введении корректных данных")
    void getQuestions_shouldReturnListStringTitles_whenDataIsValid() {
        List<Movie> listMovies = new ArrayList<>(List.of(
                new Movie("Titanic.jpg", "Титаник"),
                new Movie("The_Matrix.png", "Матрица"),
                new Movie("Avatar.jpg", "Аватар")
        ));
        GameSession gameSession = new GameSession(listMovies);
        QuestionMovie questionMovie = new QuestionMovie(listMovies);


        List<String> returnedList = questionMovie.getQuestions(gameSession);
        assertThat(returnedList)
                .isNotNull()
                .hasSize(3)
                .contains(gameSession.getCurrentMovie().title());


        gameSession.nextMovie();
        List<String> returnedListAfterPoll = questionMovie.getQuestions(gameSession);

        assertThat(returnedListAfterPoll)
                .isNotNull()
                .hasSize(3)
                .contains(gameSession.getCurrentMovie().title());
    }

    @Test
    @DisplayName("Проверка возврата только 4 вариантов ответа и без дубликатов")
    void getQuestions_shouldReturnAtMostFour_whenMoreMoviesProvided() {
        List<Movie> listMovies = List.of(
                new Movie("Titanic.jpg", "Титаник"),
                new Movie("The_Matrix.png", "Матрица"),
                new Movie("Avatar.jpg", "Аватар"),
                new Movie("Inception.jpg", "Начало"),
                new Movie("Interstellar.jpg", "Интерстеллар")
        );
        GameSession gameSession = new GameSession(listMovies);
        QuestionMovie questionMovie = new QuestionMovie(listMovies);

        List<String> questions = questionMovie.getQuestions(gameSession);
        String correctTitle = gameSession.getCurrentMovie().title();

        assertThat(questions)
                .isNotNull()
                .hasSizeBetween(1, 4)
                .contains(correctTitle)
                .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Проверка корректной работы метода получения ответа")
    void getAnswer_shouldReturnedRightAnswer_whenDataIsValid() {
        List<Movie> listMovies = new ArrayList<>(List.of(
                new Movie("Titanic.jpg", "Титаник")
        ));
        GameSession gameSession = new GameSession(listMovies);
        QuestionMovie questionMovie = new QuestionMovie(listMovies);
        questionMovie.getQuestions(gameSession);

        assertThat(questionMovie.getAnswer("титаник")).isTrue();
        assertThat(questionMovie.getAnswer("матрица")).isFalse();
    }

    @Test
    @DisplayName("Проверка корректной работы метода получения правильного ответа")
    void getCorrectTitle_shouldReturnCorrectTitle_whenDataIsValid() {
        List<Movie> listMovies = new ArrayList<>(List.of(
                new Movie("Titanic.jpg", "Титаник")
        ));
        GameSession gameSession = new GameSession(listMovies);
        QuestionMovie questionMovie = new QuestionMovie(listMovies);
        questionMovie.getQuestions(gameSession);

        assertThat(questionMovie.getCorrectTitle()).isEqualTo("Титаник");
    }

    @Test
    @DisplayName("Проверка выбрасывания IllegalStateException если нет текущего фильма")
    void getQuestions_shouldThrowIllegalStateException_whenNoCurrentMovie() {
        List<Movie> listMovies = new ArrayList<>(List.of(
                new Movie("Titanic.jpg", "Титаник")
        ));
        GameSession gameSession = new GameSession(listMovies);
        QuestionMovie questionMovie = new QuestionMovie(listMovies);

        gameSession.nextMovie();
        gameSession.nextMovie();

        assertThatThrownBy(() -> questionMovie.getQuestions(gameSession))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Нельзя формировать варианты без текущего фильма в сессии");
    }

    @Test
    @DisplayName("Проверка выбрасывания IllegalStateException в getAnswer если нет текущего фильма")
    void getAnswer_shouldThrowIllegalStateException_whenNoCurrentMovie() {
        List<Movie> listMovies = new ArrayList<>(List.of(
                new Movie("Titanic.jpg", "Титаник")
        ));
        QuestionMovie questionMovie = new QuestionMovie(listMovies);

        assertThatThrownBy(() -> questionMovie.getAnswer("титаник"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Текущий фильм не установлен, невозможно проверить ответ");
    }

    @Test
    @DisplayName("Проверка выбрасывания IllegalStateException в getCorrectTitle если нет текущего фильма")
    void getCorrectTitle_shouldThrowIllegalStateException_whenNoCurrentMovie() {
        List<Movie> listMovies = new ArrayList<>(List.of(
                new Movie("Titanic.jpg", "Титаник")
        ));
        QuestionMovie questionMovie = new QuestionMovie(listMovies);

        assertThatThrownBy(questionMovie::getCorrectTitle)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Текущий фильм не установлен");
    }

    @Test
    @DisplayName("проверка выбрасывания исключения, если список фильмов null")
    void constructor_shouldThrowIllegalArgumentException_whenListMovieIsNull() {
        assertThatThrownBy(() ->
                new QuestionMovie(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Список фильмов для построения вопросов не может быть пустым");
    }

    @Test
    @DisplayName("проверка выбрасывания исключения, если список фильмов пуст")
    void constructor_shouldThrowIllegalArgumentException_whenListMovieIsEmpty() {
        assertThatThrownBy(() ->
                new QuestionMovie(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Список фильмов для построения вопросов не может быть пустым");
    }
}