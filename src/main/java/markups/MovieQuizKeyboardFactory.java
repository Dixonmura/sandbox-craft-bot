package markups;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

/**
 * Фабрика клавиатур для кино-квиза.
 * Строит разметку с вариантами ответов и кнопкой завершения игры.
 */
public class MovieQuizKeyboardFactory {

    /**
     * Создаёт клавиатуру с четырьмя вариантами ответа
     * и кнопкой "Завершить игру".
     *
     * @param options список из четырёх вариантов ответа
     * @return разметка клавиатуры для Telegram
     * @throws IllegalArgumentException если размер списка меньше 4
     */
    public ReplyKeyboardMarkup createOptionsKeyboard(List<String> options) {
        if (options == null || options.size() < 4) {
            throw new IllegalArgumentException("Список вариантов должен содержать минимум 4 элемента");
        }

        KeyboardButton btn1 = new KeyboardButton(options.get(0));
        KeyboardButton btn2 = new KeyboardButton(options.get(1));
        KeyboardButton btn3 = new KeyboardButton(options.get(2));
        KeyboardButton btn4 = new KeyboardButton(options.get(3));

        KeyboardRow row1 = new KeyboardRow();
        row1.add(btn1);
        row1.add(btn2);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(btn3);
        row2.add(btn4);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("Завершить игру \uD83C\uDFAC\uD83C\uDFC1"));

        List<KeyboardRow> rows = List.of(row1, row2, row3);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(rows);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);

        return keyboard;
    }
}
