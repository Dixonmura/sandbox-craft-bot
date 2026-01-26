package movie_quiz.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameSessionTest {

    @Test
    @DisplayName("Проверка работы класса при введении корректных данных")
    void session_shouldBeNotNullAndNotFinished_whenDataIsValid() {

        List<Movie> movies = List.of(
                new Movie("Piter_Pen.jpg", "Питер Пэн"),
                new Movie("Titanic.png", "Титаник"));

        GameSession session = new GameSession(movies);

        assertThat(session.isFinished()).isFalse();
        assertThat(session.getScore()).isZero();
        assertThat(session.getCurrentMovie()).isNotNull();
        assertThat(session.getMovies()).hasSize(1);
    }

    @Test
    @DisplayName("После последнего фильма очередь пустая и сессия завершена")
    void session_shouldBeFinished_afterLastMovie() {
        List<Movie> movies = List.of(
                new Movie("Piter_Pen.jpg", "Питер Пэн"),
                new Movie("Titanic.png", "Титаник")
        );

        GameSession session = new GameSession(movies);

        session.nextMovie();
        assertThat(session.getCurrentMovie()).isNotNull();
        assertThat(session.isFinished()).isFalse();

        session.nextMovie();
        assertThat(session.getCurrentMovie()).isNull();
        assertThat(session.getMovies()).isEmpty();
        assertThat(session.isFinished()).isTrue();
    }

    @Test
    @DisplayName("Проверка конструктора на выбрасывание исключения, если список пустой")
    void constructor_shouldThrowIllegalArgumentException_whenListMovieIsEmpty() {

        List<Movie> emptyMovieList = new ArrayList<>();

        assertThatThrownBy(() ->
                new GameSession(emptyMovieList))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Список фильмов не может быть пустым");
    }

    @Test
    @DisplayName("Проверка конструктора на выбрасывание исключения, если на вход подаётся null")
    void constructor_shouldThrowIllegalArgumentException_whenListMovieIsNull() {
        assertThatThrownBy(() ->
                new GameSession(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Список фильмов не может быть пустым");
    }
}