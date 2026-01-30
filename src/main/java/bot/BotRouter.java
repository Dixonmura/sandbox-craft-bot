package bot;

import bot.utils.ReplyUtils;
import command.CommandDispatcher;
import markups.PomodoroKeyboardFactory;
import movie_quiz.bot.BotReply;
import movie_quiz.bot.MovieQuizBot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pomodoro.bot.PomodoroBot;
import pomodoro.bot.PomodoroReply;
import pomodoro.bot.PomodoroSender;

/**
 * Маршрутизатор обновлений Telegram:
 * принимает {@link Update}, определяет тип сообщения
 * и перенаправляет его в обработчики команд и квиза.
 */
public class BotRouter implements LongPollingSingleThreadUpdateConsumer, PomodoroSender {

    private static final Logger log = LogManager.getLogger(BotRouter.class);

    private final TelegramClient client;
    private final CommandDispatcher commandDispatcher;
    private MovieQuizBot movieQuizBot;
    private PomodoroBot pomodoroBot;

    /**
     * Создаёт маршрутизатор с новым экземпляром квиз-бота.
     *
     * @param client Telegram-клиент для отправки сообщений
     */
    public BotRouter(TelegramClient client) {
        this.client = client;
        this.movieQuizBot = new MovieQuizBot();
        this.pomodoroBot = new PomodoroBot(this);
        this.commandDispatcher = new CommandDispatcher(client, movieQuizBot, pomodoroBot);
    }

    /**
     * Специальный конструктор для удобного тестирования
     */
    BotRouter(TelegramClient client,
              CommandDispatcher commandDispatcher,
              MovieQuizBot quizBot,
              PomodoroBot pomodoroBot) {
        this.client = client;
        this.commandDispatcher = commandDispatcher;
        this.movieQuizBot = quizBot;
        this.pomodoroBot = pomodoroBot;
    }

    /**
     * Обрабатывает входящее обновление Telegram.
     * <ul>
     *     <li>Игнорирует обновления без текстового сообщения.</li>
     *     <li>Команды (начинаются с '/') отправляет в {@link CommandDispatcher}.</li>
     *     <li>Остальные сообщения либо отклоняет без активной сессии квиза,
     *     либо передаёт в {@link MovieQuizBot}.</li>
     * </ul>
     *
     * @param update обновление от Telegram
     */
    @Override
    public void consume(Update update) {
        if (update == null) {
            log.error("Получено null update");
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            log.debug("Пропуск обновления без текстового сообщения: updateId={}", update.getUpdateId());
            return;
        }

        String messageText = update.getMessage().getText().stripLeading();
        Long chatId = update.getMessage().getChatId();

        if (messageText.startsWith("/")) {
            if (movieQuizBot.hasSession(chatId)) {
                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text("\uD83C\uDFAC Квиз уже запущен.\n" +
                                "\n" +
                                "❌ Нельзя одновременно запускать два бота.\n" +
                                "\uD83D\uDCFD Сначала завершите работу с ботом Movie Quiz, а потом попробуйте запустить другого.\n")
                        .build();
                log.warn("Попытка запустить commandText={} для chatId={}, когда movieQuizBot уже запущен.", messageText, chatId);
                try {
                    client.execute(message);
                } catch (TelegramApiException e) {
                    log.error("Ошибка при отправке ответа movieQuizBot в чат chatId={}", chatId, e);
                }
            }
            if (pomodoroBot.hasSession(chatId)) {
                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text("\uD83E\uDD16 У вас уже запущен бот Pomodoro.\n" +
                                "\uD83C\uDF45 Пожалуйста, сначала завершите текущую сессию, а затем запускайте другого бота.\n")
                        .build();
                log.warn("Попытка запустить commandText={} для chatId={}, когда PomodoroBot уже запущен.", messageText, chatId);

                try {
                    client.execute(message);
                } catch (TelegramApiException e) {
                    log.error("Ошибка при отправке ответа Pomodoro в чат chatId={}", chatId, e);
                }
                return;
            }
            if (!movieQuizBot.hasSession(chatId) && !pomodoroBot.hasSession(chatId)) {
                log.info("Получена команда '{}' от chatId={}", messageText, chatId);
                commandDispatcher.dispatch(messageText, update);
                return;
            }
        }

        if (!movieQuizBot.hasSession(chatId) && !pomodoroBot.hasSession(chatId)) {

            var from = update.getMessage().getFrom();
            String firstName = from != null ? from.getFirstName() : "unknown";
            String userName = from != null ? from.getUserName() : "unknown";
            log.info("Получено обычное сообщение без активной сессии, chatId={}, firstName={}, userName={}, text={}",
                    chatId, firstName, userName, messageText);

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text("Сейчас я понимаю только команды, выберите в меню новую команду или введите вручную.\n")
                    .build();

            try {
                client.execute(message);
            } catch (TelegramApiException e) {
                log.error("Не удалось отправить системное сообщение пользователю, chatId={}", chatId, e);
            }
        }

        if (movieQuizBot.hasSession(chatId)) {
            log.info("Обработка ответа квиза от chatId={}", chatId);

            BotReply reply = movieQuizBot.handleAnswer(update);
            SendPhoto sendPhoto = ReplyUtils.sendPhotoQuiz(reply, chatId, getClass().getClassLoader());
            SendMessage sendMessage = ReplyUtils.sendMessageQuiz(reply, chatId);

            try {
                if (sendPhoto != null) {
                    client.execute(sendPhoto);
                }
                client.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке ответа квиза в чат chatId={}", chatId, e);
                SendMessage fallback = SendMessage.builder()
                        .chatId(chatId)
                        .text(sendMessage.getText() + "\n\n(⚠️ Картинку отправить не удалось из-за ошибки соединения.)")
                        .build();
                try {
                    client.execute(fallback);
                } catch (TelegramApiException ex) {
                    log.error("Ошибка при отправке fallback-сообщения квиза в чат chatId={}", chatId, ex);
                }
            }
        }

        if (pomodoroBot.hasSession(chatId)) {
            log.info("Обработка ответа Pomodoro от chatId={}", chatId);

            PomodoroReply reply = pomodoroBot.handleAnswer(update);
            SendPhoto sendPhoto = null;

            if (reply.imagePath() != null) {
                sendPhoto = ReplyUtils.sendPhotoPomodoro(reply, chatId, getClass().getClassLoader());
            }

            SendMessage sendMessage = ReplyUtils.sendMessagePomodoro(reply, chatId);

            try {
                if (sendPhoto != null) {
                    client.execute(sendPhoto);
                }
                if (sendMessage == null || sendMessage.getText().isBlank()) {
                    log.warn("Пропуск отправки пустого сообщения Pomodoro для chatId={}", chatId);
                    return;
                }
                client.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке ответа Pomodoro в чат chatId={}", chatId, e);
                SendMessage fallback = SendMessage.builder()
                        .chatId(chatId)
                        .text(sendMessage.getText() + "\n\n(⚠️ Мотивашку с картинкой отправить не удалось из-за ошибки соединения.)")
                        .build();
                try {
                    client.execute(fallback);
                } catch (TelegramApiException ex) {
                    log.error("Ошибка при отправке fallback-сообщения Pomodoro в чат chatId={}", chatId, ex);
                }
            }
        }
    }

    @Override
    public void sendPomodoroReply(Long chatId, PomodoroReply reply) {
        log.info("Обработка сигнала планировщика Pomodoro для chatId={}", chatId);

        SendPhoto sendPhoto = null;

        if (reply.imagePath() != null) {
            sendPhoto = ReplyUtils.sendPhotoPomodoro(reply, chatId, getClass().getClassLoader());
        }

        SendMessage sendMessage = ReplyUtils.sendMessagePomodoro(reply, chatId);

        try {
            if (sendPhoto != null) {
                client.execute(sendPhoto);
            }
            if (sendMessage == null || sendMessage.getText().isBlank()) {
                log.warn("Пропуск отправки пустого сообщения Pomodoro для chatId={}", chatId);
                return;
            }
            client.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа Pomodoro в чат chatId={}", chatId, e);
            SendMessage fallback = SendMessage.builder()
                    .chatId(chatId)
                    .text(sendMessage.getText() + "\n\n(⚠️ Мотивашку с картинкой отправить не удалось из-за ошибки соединения.)")
                    .build();
            try {
                client.execute(fallback);
            } catch (TelegramApiException ex) {
                log.error("Ошибка при отправке fallback-сообщения Pomodoro в чат chatId={}", chatId, ex);
            }
        }
    }

    @Override
    public void sendFinalStatsQuestion(Long chatId, String text) {
        PomodoroKeyboardFactory factory = new PomodoroKeyboardFactory();
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(factory.createFinalAskKeyboard())
                .build();
        try {
            client.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке вопроса о выводе статистики Pomodoro в чат chatId={}", chatId, e);
        }
    }
}
