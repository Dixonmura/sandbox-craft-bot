package command;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Базовый контракт для команд Telegram-бота.
 * Реализация отвечает за обработку конкретной команды
 * на основе данных из {@link Update}.
 */
public interface Command {

    /**
     * Выполняет логику команды для переданного обновления.
     *
     * @param update входящее обновление Telegram
     */
    void execute(Update update);
}


