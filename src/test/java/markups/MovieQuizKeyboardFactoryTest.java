package markups;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MovieQuizKeyboardFactoryTest {

    @Test
    @DisplayName("Проверка корректности созданной клавиатуры")
    void createOptionsKeyboard_shouldCreateCorrectLayout() {
        MovieQuizKeyboardFactory keyboardFactory = new MovieQuizKeyboardFactory();
        ReplyKeyboardMarkup keyboardMarkup = keyboardFactory.createOptionsKeyboard(
                List.of("Фильм 1",
                        "Фильм 2",
                        "Фильм 3",
                        "Фильм 4")
        );
        List<KeyboardRow> keyboardRows = keyboardMarkup.getKeyboard();

        assertThat(keyboardRows)
                .isNotNull()
                .hasSize(3);

        assertThat(keyboardRows.get(0)).hasSize(2);
        assertThat(keyboardRows.get(1)).hasSize(2);
        assertThat(keyboardRows.get(2)).hasSize(1);

        assertThat(keyboardRows.get(0).getFirst().getText()).isEqualTo("Фильм 1");
        assertThat(keyboardRows.get(0).get(1).getText()).isEqualTo("Фильм 2");
        assertThat(keyboardRows.get(1).getFirst().getText()).isEqualTo("Фильм 3");
        assertThat(keyboardRows.get(1).get(1).getText()).isEqualTo("Фильм 4");

        assertThat(keyboardRows.get(2).getFirst().getText()).contains("Завершить игру");

        assertThat(keyboardMarkup.getResizeKeyboard())
                .isNotNull()
                .isTrue();
        assertThat(keyboardMarkup.getOneTimeKeyboard())
                .isNotNull()
                .isTrue();
    }
}