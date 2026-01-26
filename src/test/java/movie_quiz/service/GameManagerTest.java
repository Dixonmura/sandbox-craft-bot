package movie_quiz.service;

import movie_quiz.core.Movie;
import movie_quiz.core.QuestionView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameManagerTest {

    @Test
    @DisplayName("Проверка получения списка вопросов, когда игра не закончена")
    void getNextQuestion_shouldReturnOptions_whenGameNotFinished() {
        List<Movie> movies = List.of(
                new Movie("Titanic.jpg", "Титаник"),
                new Movie("The_Matrix.png", "Матрица")
        );
        GameManager manager = new GameManager(movies);

        Optional<QuestionView> viewOpt = manager.getNextQuestion();
        assertThat(viewOpt).isPresent();

        List<String> question = viewOpt.get().movieTitles();
        assertThat(question)
                .isNotNull()
                .isNotEmpty()
                .hasSizeBetween(1, 4);
    }

    @Test
    @DisplayName("Корректный ответ увеличивает счёт и переводит к следующему вопросу")
    void checkAnswer_shouldIncreaseScoreAndMoveNext_whenAnswerIsCorrect() {
        List<Movie> movies = List.of(
                new Movie("Titanic.jpg", "Титаник"),
                new Movie("The_Matrix.png", "Матрица")
        );
        GameManager manager = new GameManager(movies);

        QuestionView firstView = manager.getNextQuestion()
                .orElseThrow();
        List<String> firstQuestion = firstView.movieTitles();
        String correctFirst = manager.getRightAnswer();

        boolean firstResult = manager.checkAnswer(correctFirst);

        assertThat(firstResult).isTrue();
        assertThat(manager.getScore()).isEqualTo(1);

        List<String> secondQuestion = manager.getNextQuestion().get().movieTitles();

        assertThat(firstQuestion).isNotEmpty();
        assertThat(secondQuestion).isNotEmpty();
    }

    @Test
    @DisplayName("После последнего вопроса getNextQuestion возвращает empty Optional")
    void getNextQuestion_shouldReturnEmptyOptional_whenGameFinished() {
        List<Movie> movies = List.of(new Movie("Titanic.jpg", "Титаник"));
        GameManager manager = new GameManager(movies);

        assertThat(manager.getNextQuestion())
                .isPresent();

        manager.checkAnswer(manager.getRightAnswer());

        assertThat(manager.getNextQuestion())
                .isEmpty();
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения при проверке ответа до выдачи вопроса")
    void checkAnswer_shouldThrowException_whenCalledBeforeQuestion() {
        List<Movie> movies = List.of(
                new Movie("Titanic.jpg", "Титаник")
        );
        GameManager manager = new GameManager(movies);

        assertThatThrownBy(() -> manager.checkAnswer("Титаник"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Нельзя проверять ответ до выдачи вопроса");
    }
}