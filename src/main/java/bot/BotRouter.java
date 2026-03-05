package bot;

import bot.utils.ReplyUtils;
import command.CommandDispatcher;
import markups.PomodoroKeyboardFactory;
import markups.VacancyKeyboardKey;
import markups.VacancyTrackerKeyboardFactory;
import movie_quiz.bot.BotReply;
import movie_quiz.bot.MovieQuizBot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import pomodoro.bot.PomodoroBot;
import pomodoro.bot.PomodoroReply;
import pomodoro.bot.PomodoroSender;
import vacancy_tracker.bot.VacancyBot;
import vacancy_tracker.bot.VacancyReply;

import static bot.RouterMessages.*;

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
    private VacancyBot vacancyBot;

    /**
     * Создаёт маршрутизатор с новым экземпляром квиз-бота, помодоро бота
     * и инициализацией бота вакансий
     *
     * @param client Telegram-клиент для отправки сообщений
     */
    public BotRouter(TelegramClient client, VacancyBot vacancyBot) {
        this.client = client;
        this.movieQuizBot = new MovieQuizBot();
        this.pomodoroBot = new PomodoroBot(this);
        this.vacancyBot = vacancyBot;
        this.commandDispatcher = new CommandDispatcher(client, movieQuizBot, pomodoroBot, this.vacancyBot);
    }

    /**
     * Специальный конструктор для удобного тестирования
     */
    BotRouter(TelegramClient client,
              CommandDispatcher commandDispatcher,
              MovieQuizBot quizBot,
              PomodoroBot pomodoroBot,
              VacancyBot vacancyBot) {
        this.client = client;
        this.commandDispatcher = commandDispatcher;
        this.movieQuizBot = quizBot;
        this.pomodoroBot = pomodoroBot;
        this.vacancyBot = vacancyBot;
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

        if (update.hasCallbackQuery()) {
            Long callbackChatId = update.getCallbackQuery().getMessage().getChatId();
            String data = update.getCallbackQuery().getData();

            log.debug("CallbackQuery received: chatId={}, data='{}'", callbackChatId, data);

            if (data.startsWith("start")) {
                commandDispatcher.dispatch(data, adaptCallbackToMessage(update));

                AnswerCallbackQuery ack = AnswerCallbackQuery.builder()
                        .callbackQueryId(update.getCallbackQuery().getId())
                        .build();
                try {
                    client.execute(ack);
                } catch (TelegramApiException e) {
                    log.error("Error sending vacancy callback reply: chatId={}", callbackChatId, e);
                }
                return;
            }


            VacancyReply reply = vacancyBot.handleAnswer(update);
            SendMessage msg = ReplyUtils.sendMessageVacancy(reply);

            InlineKeyboardMarkup inlineMarkup = null;
            if (msg != null && msg.getReplyMarkup() instanceof InlineKeyboardMarkup m) {
                inlineMarkup = m;
            }

            EditMessageText.EditMessageTextBuilder editBuilder = EditMessageText.builder()
                    .chatId(callbackChatId)
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .text(msg != null ? msg.getText() : "");

            if (inlineMarkup != null) {
                editBuilder.replyMarkup(inlineMarkup);
            }

            EditMessageText editMsg = editBuilder.build();

            AnswerCallbackQuery ack = AnswerCallbackQuery.builder()
                    .callbackQueryId(update.getCallbackQuery().getId())
                    .build();
            try {
                client.execute(editMsg);
                client.execute(ack);
            } catch (TelegramApiException e) {
                log.error("Error sending vacancy callback reply: chatId={}", callbackChatId, e);
            }
            return;
        }


        if (!update.hasMessage() || !update.getMessage().hasText()) {
            log.debug("Пропуск обновления без текстового сообщения: updateId={}", update.getUpdateId());
            return;
        }

        String messageText = update.getMessage().getText().stripLeading();
        Long chatId = update.getMessage().getChatId();

        if (messageText.startsWith("/")) {
            handleCommand(chatId, messageText, update);
            return;
        }

        handlePlainMessage(chatId, messageText, update);
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

    private void handleCommand(Long chatId, String messageText, Update update) {

        if (movieQuizBot.hasSession(chatId)) {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(QUIZ_IS_ACTIVE)
                    .build();
            log.warn("Попытка запустить commandText={} для chatId={}, когда movieQuizBot уже запущен.", messageText, chatId);
            try {
                client.execute(message);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке ответа movieQuizBot в чат chatId={}", chatId, e);
            }
            return;
        }

        if (pomodoroBot.hasSession(chatId)) {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(POMODORO_IS_ACTIVE)
                    .build();
            log.warn("Попытка запустить commandText={} для chatId={}, когда PomodoroBot уже запущен.", messageText, chatId);

            try {
                client.execute(message);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке ответа Pomodoro в чат chatId={}", chatId, e);
            }
            return;
        }

        if (vacancyBot.isConfiguring(chatId)) {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(VACANCY_IS_CONFIGURE)
                    .build();
            log.warn("Попытка запустить commandText={} для chatId={}, когда VacancyBot уже запущен.", messageText, chatId);

            try {
                client.execute(message);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке ответа Pomodoro в чат chatId={}", chatId, e);
            }
            return;
        }

        log.info("Получена команда '{}' от chatId={}", messageText, chatId);
        commandDispatcher.dispatch(messageText, update);
    }

    private void handlePlainMessage(Long chatId, String messageText, Update update) {

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
            return;
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
            return;
        }

        if (vacancyBot.isConfiguring(chatId)) {
            log.info("Обработка ответа VacancyBot от chatId={}", chatId);

            VacancyReply reply = vacancyBot.handleAnswer(update);
            SendMessage sendMessage = ReplyUtils.sendMessageVacancy(reply);

            log.debug(
                    "Vacancy handlePlainMessage: chatId={}, text='{}', keyboardKey='{}'",
                    reply.userId(),
                    reply.text(),
                    reply.keyboardKey());

            try {
                client.execute(sendVacancyReplyWithControlKeyboard(sendMessage, chatId));
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке ответа VacancyBot в чат chatId={}", chatId, e);
            }
            return;
        }

        if (vacancyBot.isActive(chatId)) {

            VacancyReply reply = new VacancyReply(chatId, VACANCY_IS_ACTIVE, VacancyKeyboardKey.STOP_KEYBOARD);

            SendMessage message = ReplyUtils.sendMessageVacancy(reply);

            try {
                client.execute(sendVacancyReplyWithControlKeyboard(message, chatId));
            } catch (TelegramApiException e) {
                log.error("Не удалось отправить системное сообщение пользователю, chatId={}", chatId, e);
            }
            return;
        }

        log.info("Получено обычное сообщение без активных сессий, chatId={}, text={}", chatId, messageText);
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(COMMAND_UNDERSTAND_MESSAGE)
                .build();
        try {
            client.execute(message);
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить системное сообщение пользователю, chatId={}", chatId, e);
        }
    }

    private SendMessage sendVacancyReplyWithControlKeyboard(SendMessage message, Long chatId) {
        if (message == null) {
            log.warn("ReplyUtils вернул null для VacancyBot, chatId={}", chatId);
            return null;
        }

        return message;
    }

    private Update adaptCallbackToMessage(Update original) {
        CallbackQuery cb = original.getCallbackQuery();
        Message fakeMsg = new Message();

        fakeMsg.setMessageId(cb.getMessage().getMessageId());
        fakeMsg.setChat(cb.getMessage().getChat());
        fakeMsg.setText(cb.getMessage().toString());
        fakeMsg.setDate(cb.getMessage().getDate());

        Update adapted = new Update();
        adapted.setMessage(fakeMsg);
        return adapted;
    }
}
