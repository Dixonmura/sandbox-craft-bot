package command;

import markups.BotRouterKeyboardFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Команда /start.
 * Отправляет пользовательское приветствие и краткое описание возможностей бота.
 */
public class CommandStart implements Command {

    private static final Logger log = LogManager.getLogger(CommandStart.class);
    private final BotRouterKeyboardFactory factory = new BotRouterKeyboardFactory();


    private final TelegramClient telegramClient;

    /**
     * Создаёт команду /start.
     *
     * @param telegramClient клиент Telegram для отправки приветственного сообщения
     */
    public CommandStart(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
     * Отправляет приветственное сообщение с описанием команд бота.
     *
     * @param update обновление Telegram с командой /start
     */
    @Override
    public void execute(Update update) {
        if (update == null || !update.hasMessage()) {
            log.error("CommandStart.execute вызван с некорректным update");
            return;
        }

        Long chatId = update.getMessage().getChatId();
        log.info("Обработка команды /start для chatId={}", chatId);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(CommandMessages.START_COMMAND_MESSAGE)
                .replyMarkup(factory.createMainMenuKeyboard())
                .build();

        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить приветственное сообщение для chatId={}", chatId, e);
        }
    }
}
