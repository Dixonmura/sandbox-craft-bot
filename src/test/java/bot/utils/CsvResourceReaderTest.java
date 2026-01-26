package bot.utils;

import movie_quiz.core.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvResourceReaderTest {

    CsvResourceReader reader;

    @BeforeEach
    void setUp() {
        reader = new CsvResourceReader();
    }

    @Test
    @DisplayName("Проверка корректной работы класса и возврата списка Movie")
    void read_shouldRedFileAndReturnListMovies_whenDataCorrectly() {
        String csv = """
                Titanic.png,Титаник
                Piter_Pen.png,Питер Пэн""";

        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        List<Movie> movies = reader.read(is, ',', row -> new Movie(row[0], row[1]));
        assertThat(movies)
                .isNotNull()
                .hasSize(2)
                .first()
                .extracting(Movie::title, Movie::imageFileName)
                .containsExactly("Титаник", "Titanic.png");
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения при чтении некорректного CSV-файла")
    void read_shouldThrowRuntimeException_whenCsvIsInvalid() {
        String csv = """
                Titanic.png, """;

        InputStream is = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> reader.read(is, ',', row -> new Movie(row[0], row[1])))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Некорректный формат в строке 1: 'Titanic.png,'");
    }
}