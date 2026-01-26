package command;

import bot.utils.ReplyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pomodoro.bot.PomodoroBot;
import pomodoro.bot.PomodoroReply;

public class CommandPomodoro implements Command {

    private static final Logger log = LogManager.getLogger(CommandPomodoro.class);


    private final TelegramClient telegramClient;
    private final PomodoroBot pomodoroBot;

    /**
     * Создает команду запуска Pomodoro-бота.
     *
     * @param telegramClient Telegram-клиент для отправки сообщений
     * @param pomodoroBot    экземпляр Pomodoro-бота
     */
    public CommandPomodoro(TelegramClient telegramClient, PomodoroBot pomodoroBot) {
        this.telegramClient = telegramClient;
        this.pomodoroBot = pomodoroBot;
    }

    /**
     * Запускает бота Pomodoro впервые, отправляет приветственное сообщение и
     * объяснение своего назначения, а так же кнопок для управления.
     *
     * @param update входящее обновление Telegram
     */
    @Override
    public void execute(Update update) {
        if (update == null || !update.hasMessage()) {
            log.error("CommandPomodoro вызван с некорректным update");
            return;
        }

        Long chatId = update.getMessage().getChatId();
        log.info("Запуск новой сессии Pomodoro для chatId={}", chatId);

        PomodoroReply reply = pomodoroBot.startPomodoro(update);

        SendPhoto photo = null;
        SendMessage message = null;

        if (reply.imagePath() != null) {
            photo = ReplyUtils.sendPhotoPomodoro(reply, chatId, getClass().getClassLoader());
        }

        message = ReplyUtils.sendMessagePomodoro(reply, chatId);

        try {
            if (photo != null) {
                telegramClient.execute(photo);
            }
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке стартового сообщения Pomodoro для chatId={}", chatId, e);
        }
    }
}
