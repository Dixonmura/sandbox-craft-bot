package markups;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import pomodoro.bot.PomodoroMessages;

import java.util.List;

/**
 * Фабрика клавиатур для Pomodoro-бота.
 * Строит разметку с вариантами ответов и кнопкой завершения игры.
 */
public class PomodoroKeyboardFactory {

    /**
     * Создает клавиатуру с двумя вариантами ответа и
     * кнопкой завершить игру
     *
     * @return разметка клавиатуры для Telegram
     */
    public ReplyKeyboardMarkup createButtonsKeyboard() {

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();

        row1.add(new KeyboardButton(PomodoroMessages.START_MESSAGE));
        row1.add(new KeyboardButton(PomodoroMessages.PAUSE_MESSAGE));
        row2.add(new KeyboardButton(PomodoroMessages.END_SEANCE_MESSAGE));

        List<KeyboardRow> rows = List.of(row1, row2);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(rows);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        return keyboardMarkup;
    }

    public ReplyKeyboardMarkup createFinalAskKeyboard() {
        KeyboardButton yes = new KeyboardButton(PomodoroMessages.YES_ANSWER_MESSAGE);
        KeyboardButton no = new KeyboardButton(PomodoroMessages.NO_ANSWER_MESSAGE);

        KeyboardRow row = new KeyboardRow();
        row.add(yes);
        row.add(no);

        List<KeyboardRow> rows = List.of(row);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(rows);
        keyboard.setKeyboard(List.of(row));
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);

        return keyboard;
    }
}
