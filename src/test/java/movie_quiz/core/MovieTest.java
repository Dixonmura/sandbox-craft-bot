package movie_quiz.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MovieTest {

    @Test
    @DisplayName("Успешное создание Movie при валидных данных")
    void constructor_shouldCreateMovie_whenDataIsValid() {
        Movie movie = new Movie("The test file name", "The test title");

        assertThat(movie.imageFileName()).isEqualTo("The test file name");
        assertThat(movie.title()).isEqualTo("The test title");
    }

    @Test
    @DisplayName("Бросает IllegalArgumentException, если имя файла null")
    void constructor_shouldThrowException_whenFileNameIsNull() {
        assertThatThrownBy(() -> new Movie(null, "The test title"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Имя файла картинки не может быть пустым");
    }

    @Test
    @DisplayName("Бросает IllegalArgumentException, если имя файла пустое")
    void constructor_shouldThrowException_whenFileNameIsBlank() {
        assertThatThrownBy(() -> new Movie("   ", "The test title"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Имя файла картинки не может быть пустым");
    }

    @Test
    @DisplayName("Бросает IllegalArgumentException, если название null")
    void constructor_shouldThrowException_whenTitleIsNull() {
        assertThatThrownBy(() -> new Movie("The test file name", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Название фильма не может быть пустым");
    }

    @Test
    @DisplayName("Бросает IllegalArgumentException, если название пустое")
    void constructor_shouldThrowException_whenTitleIsBlank() {
        assertThatThrownBy(() -> new Movie("The test file name", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Название фильма не может быть пустым");
    }
}