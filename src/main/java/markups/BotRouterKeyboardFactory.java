package markups;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import static bot.RouterOptions.*;

public class BotRouterKeyboardFactory {
    public InlineKeyboardMarkup createMainMenuKeyboard() {

        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(START_POMODORO.getTitle())
                                .callbackData(START_POMODORO.getCommand())
                                .build()))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(START_MOVIE_QUIZ.getTitle())
                                .callbackData(START_MOVIE_QUIZ.getCommand())
                                .build()))
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(START_VACANCY_TRACKER.getTitle())
                                .callbackData(START_VACANCY_TRACKER.getCommand())
                                .build()))
                .build();
    }
}
