package markups;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

public class VacancyTrackerKeyboardFactory {

    public ReplyKeyboardMarkup createStartKeyboard() {

        KeyboardButton changeUtcOffset = new KeyboardButton("Поменять часовой пояс");
        KeyboardButton continueButton = new KeyboardButton("Продолжить");

        KeyboardRow row = new KeyboardRow();
        row.add(changeUtcOffset);
        row.add(continueButton);

        List<KeyboardRow> rows = List.of(row);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(rows);
        keyboardMarkup.setKeyboard(List.of(row));
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        return keyboardMarkup;
    }

    public ReplyKeyboardMarkup createSettingsKeyboard() {

        KeyboardButton regionCode = new KeyboardButton("Регион");
        KeyboardButton minimalExperience = new KeyboardButton("Минимальный опыт");
        KeyboardButton minimalSalary = new KeyboardButton("Минимальная зарплата");
        KeyboardButton keyWord = new KeyboardButton("Слово для поиска");
        KeyboardButton notificationSetting = new KeyboardButton("Настройки нотификации");
        KeyboardButton complete = new KeyboardButton("Готово");

        List<KeyboardRow> rows = List.of(
                new KeyboardRow(regionCode),
                new KeyboardRow(minimalExperience),
                new KeyboardRow(minimalSalary),
                new KeyboardRow(keyWord),
                new KeyboardRow(notificationSetting),
                new KeyboardRow(complete)
        );

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(rows);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        return keyboardMarkup;
    }
}
