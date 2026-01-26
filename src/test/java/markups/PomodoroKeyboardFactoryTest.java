package markups;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PomodoroKeyboardFactoryTest {


    @Test
    void createButtonsKeyboard() {
        PomodoroKeyboardFactory keyboardFactory = new PomodoroKeyboardFactory();
        ReplyKeyboardMarkup keyboardMarkup = keyboardFactory.createButtonsKeyboard();

        assertThat(keyboardMarkup)
                .isNotNull();
        assertThat(keyboardMarkup.getOneTimeKeyboard())
                .isFalse();
        assertThat(keyboardMarkup.getResizeKeyboard()).isTrue();

        List<KeyboardRow> rows = keyboardMarkup.getKeyboard();
        assertThat(rows)
                .isNotEmpty()
                .hasSize(2);
    }
}