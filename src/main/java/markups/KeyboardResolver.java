package markups;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public interface KeyboardResolver {
    ReplyKeyboardMarkup resolve(VacancyKeyboardKey key);
}
