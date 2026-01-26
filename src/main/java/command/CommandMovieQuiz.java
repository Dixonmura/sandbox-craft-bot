package command;

import bot.utils.ReplyUtils;
import movie_quiz.bot.BotReply;
import movie_quiz.bot.MovieQuizBot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Команда запуска киноквиза.
 * Инициализирует новую игру и отправляет пользователю первый вопрос.
 */
public class CommandMovieQuiz implements Command {

    private static final Logger log = LogManager.getLogger(CommandMovieQuiz.class);

    private final TelegramClient telegramClient;
    private final MovieQuizBot quizBot;

    /**
     * Создаёт команду запуска киноквиза.
     *
     * @param client  Telegram-клиент для отправки сообщений
     * @param quizBot экземпляр квиз-бота
     */
    public CommandMovieQuiz(TelegramClient client, MovieQuizBot quizBot) {
        this.telegramClient = client;
        this.quizBot = quizBot;
    }

    /**
     * Запускает новую игру кино-квиза для пользователя.
     * Создаёт игровую сессию и отправляет первый вопрос и, при наличии, изображение.
     *
     * @param update обновление Telegram с командой пользователя
     */
    @Override
    public void execute(Update update) {
        if (update == null || !update.hasMessage()) {
            log.error("CommandMovieQuiz.execute вызван с некорректным update");
            return;
        }

        Long chatId = update.getMessage().getChatId();
        log.info("Запуск новой игры MovieQuiz для chatId={}", chatId);

        BotReply reply = quizBot.startGame(update);

        SendPhoto sendPhoto = ReplyUtils.sendPhotoQuiz(reply, chatId, getClass().getClassLoader());
        SendMessage sendMessage = ReplyUtils.sendMessageQuiz(reply, chatId);

        try {
            if (sendPhoto != null) {
                telegramClient.execute(sendPhoto);
            }
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке стартового сообщения киноквиза для chatId={}", chatId, e);
        }
    }
}

