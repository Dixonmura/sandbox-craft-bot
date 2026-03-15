package bot;

import bot.utils.ReplyUtils;
import command.CommandDispatcher;
import markups.PomodoroKeyboardFactory;
import markups.VacancyKeyboardKey;
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
import vacancy_tracker.bot.VacancyTelegramSender;

import static bot.RouterMessages.*;

/**
 * Маршрутизатор обновлений Telegram.
 * <p>
 * Принимает все входящие обновления от Telegram, определяет их тип
 * (команда, текстовое сообщение, callback) и перенаправляет в соответствующие
 * обработчики: {@link CommandDispatcher}, {@link MovieQuizBot},
 * {@link PomodoroBot} или {@link VacancyBot}.
 * <p>
 * Также реализует {@link PomodoroSender} для отправки уведомлений от планировщика.
 */
public class BotRouter implements LongPollingSingleThreadUpdateConsumer, PomodoroSender {

    private static final Logger log = LogManager.getLogger(BotRouter.class);

    private final TelegramClient client;
    private final CommandDispatcher commandDispatcher;
    private final MovieQuizBot movieQuizBot;
    private final PomodoroBot pomodoroBot;
    private final VacancyBot vacancyBot;
    private final VacancyTelegramSender vacancySender;

    /**
     * Основной конструктор для продакшн-использования.
     *
     * @param client         Telegram клиент
     * @param vacancyBot     бот для работы с вакансиями
     * @param vacancySender  отправитель вакансий с пагинацией
     */
    public BotRouter(TelegramClient client, VacancyBot vacancyBot, VacancyTelegramSender vacancySender) {
        this.client = client;
        this.movieQuizBot = new MovieQuizBot();
        this.pomodoroBot = new PomodoroBot(this);
        this.vacancyBot = vacancyBot;
        this.vacancySender = vacancySender;
        this.commandDispatcher = new CommandDispatcher(client, movieQuizBot, pomodoroBot, this.vacancyBot);
    }

    /**
     * Конструктор для тестирования (с возможностью подставить моки).
     */
    BotRouter(TelegramClient client,
              CommandDispatcher commandDispatcher,
              MovieQuizBot quizBot,
              PomodoroBot pomodoroBot,
              VacancyBot vacancyBot,
              VacancyTelegramSender vacancySender) {
        this.client = client;
        this.commandDispatcher = commandDispatcher;
        this.movieQuizBot = quizBot;
        this.pomodoroBot = pomodoroBot;
        this.vacancyBot = vacancyBot;
        this.vacancySender = vacancySender;
    }

    /**
     * Обрабатывает входящее обновление от Telegram.
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
            handleCallbackQuery(update);
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
        } else {
            handlePlainMessage(chatId, messageText, update);
        }
    }

    /**
     * Обрабатывает callback-запросы (нажатия на inline-кнопки).
     */
    private void handleCallbackQuery(Update update) {
        Long callbackChatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();

        log.debug("CallbackQuery received: chatId={}, data='{}'", callbackChatId, data);

        if (data.startsWith("start")) {
            commandDispatcher.dispatch(data, adaptCallbackToMessage(update));
            answerCallback(update);
            return;
        }

        if (data.startsWith("vacancy_")) {
            handleVacancyPagination(update);
            answerCallback(update);
            return;
        }

        VacancyReply reply = vacancyBot.handleAnswer(update);
        SendMessage msg = ReplyUtils.sendMessageVacancy(reply);

        InlineKeyboardMarkup inlineMarkup = null;
        if (msg != null && msg.getReplyMarkup() instanceof InlineKeyboardMarkup m) {
            inlineMarkup = m;
        }

        EditMessageText editMsg = EditMessageText.builder()
                .chatId(callbackChatId)
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .text(msg != null ? msg.getText() : "")
                .replyMarkup(inlineMarkup)
                .build();

        try {
            client.execute(editMsg);
            answerCallback(update);
        } catch (TelegramApiException e) {
            log.error("Error sending vacancy callback reply: chatId={}", callbackChatId, e);
        }
    }

    /**
     * Отправляет ответ на callback, чтобы убрать "часики" в Telegram.
     */
    private void answerCallback(Update update) {
        AnswerCallbackQuery ack = AnswerCallbackQuery.builder()
                .callbackQueryId(update.getCallbackQuery().getId())
                .build();
        try {
            client.execute(ack);
        } catch (TelegramApiException e) {
            log.error("Error answering callback", e);
        }
    }

    /**
     * Обрабатывает команды, начинающиеся с '/'.
     */
    private void handleCommand(Long chatId, String messageText, Update update) {
        if (movieQuizBot.hasSession(chatId)) {
            sendMessage(chatId, QUIZ_IS_ACTIVE);
            log.warn("Попытка запустить команду {} при активном MovieQuiz", messageText);
            return;
        }

        if (pomodoroBot.hasSession(chatId)) {
            sendMessage(chatId, POMODORO_IS_ACTIVE);
            log.warn("Попытка запустить команду {} при активном Pomodoro", messageText);
            return;
        }

        if (vacancyBot.isConfiguring(chatId)) {
            sendMessage(chatId, VACANCY_IS_CONFIGURE);
            log.warn("Попытка запустить команду {} при настройке VacancyBot", messageText);
            return;
        }

        log.info("Получена команда '{}' от chatId={}", messageText, chatId);
        commandDispatcher.dispatch(messageText, update);
    }

    /**
     * Обрабатывает обычные текстовые сообщения (не команды).
     */
    private void handlePlainMessage(Long chatId, String messageText, Update update) {
        if (movieQuizBot.hasSession(chatId)) {
            handleMovieQuizMessage(chatId, update);
            return;
        }

        if (pomodoroBot.hasSession(chatId)) {
            handlePomodoroMessage(chatId, update);
            return;
        }

        if (vacancyBot.isConfiguring(chatId)) {
            handleVacancyConfigMessage(chatId, update);
            return;
        }

        if (vacancyBot.isActive(chatId)) {
            handleVacancyActiveMessage(chatId);
            return;
        }

        log.info("Получено обычное сообщение без активных сессий, chatId={}, text={}", chatId, messageText);
        sendMessage(chatId, COMMAND_UNDERSTAND_MESSAGE);
    }

    private void handleMovieQuizMessage(Long chatId, Update update) {
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
            log.error("Ошибка при отправке ответа квиза", e);
            sendFallbackMessage(chatId, sendMessage.getText());
        }
    }

    private void handlePomodoroMessage(Long chatId, Update update) {
        log.info("Обработка ответа Pomodoro от chatId={}", chatId);

        PomodoroReply reply = pomodoroBot.handleAnswer(update);
        SendPhoto sendPhoto = reply.imagePath() != null
                ? ReplyUtils.sendPhotoPomodoro(reply, chatId, getClass().getClassLoader())
                : null;
        SendMessage sendMessage = ReplyUtils.sendMessagePomodoro(reply, chatId);

        try {
            if (sendPhoto != null) {
                client.execute(sendPhoto);
            }
            if (sendMessage != null && !sendMessage.getText().isBlank()) {
                client.execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа Pomodoro", e);
        }
    }

    private void handleVacancyConfigMessage(Long chatId, Update update) {
        log.info("Обработка ответа VacancyBot от chatId={}", chatId);

        VacancyReply reply = vacancyBot.handleAnswer(update);
        SendMessage sendMessage = ReplyUtils.sendMessageVacancy(reply);

        log.debug("Vacancy: chatId={}, text='{}', key='{}'",
                reply.userId(), reply.text(), reply.keyboardKey());

        try {
            if (sendMessage != null) {
                client.execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа VacancyBot", e);
        }
    }

    private void handleVacancyActiveMessage(Long chatId) {
        VacancyReply reply = new VacancyReply(chatId, VACANCY_IS_ACTIVE, VacancyKeyboardKey.STOP_KEYBOARD);
        SendMessage message = ReplyUtils.sendMessageVacancy(reply);
        try {
            if (message != null) {
                client.execute(message);
            }
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение пользователю", e);
        }
    }

    /**
     * Отправляет простое текстовое сообщение.
     */
    private void sendMessage(Long chatId, String text) {
        try {
            client.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Не удалось отправить сообщение пользователю {}", chatId, e);
        }
    }

    /**
     * Отправляет fallback-сообщение при ошибке отправки фото.
     */
    private void sendFallbackMessage(Long chatId, String originalText) {
        SendMessage fallback = SendMessage.builder()
                .chatId(chatId)
                .text(originalText + "\n\n(⚠️ Картинку отправить не удалось из-за ошибки соединения.)")
                .build();
        try {
            client.execute(fallback);
        } catch (TelegramApiException ex) {
            log.error("Ошибка при отправке fallback-сообщения", ex);
        }
    }

    /**
     * Адаптирует callback-запрос под обычное сообщение для CommandDispatcher.
     */
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

    /**
     * Обрабатывает пагинацию вакансий.
     */
    private void handleVacancyPagination(Update update) {
        if (vacancySender == null) return;

        CallbackQuery callback = update.getCallbackQuery();
        Long chatId = callback.getMessage().getChatId();
        String data = callback.getData();

        if ("vacancy_next".equals(data)) {
            vacancySender.nextPage(chatId);
        } else if ("vacancy_prev".equals(data)) {
            vacancySender.prevPage(chatId);
        }
    }

    // PomodoroSender implementation

    @Override
    public void sendPomodoroReply(Long chatId, PomodoroReply reply) {
        log.info("Отправка сигнала планировщика Pomodoro для chatId={}", chatId);

        SendPhoto sendPhoto = reply.imagePath() != null
                ? ReplyUtils.sendPhotoPomodoro(reply, chatId, getClass().getClassLoader())
                : null;
        SendMessage sendMessage = ReplyUtils.sendMessagePomodoro(reply, chatId);

        try {
            if (sendPhoto != null) {
                client.execute(sendPhoto);
            }
            if (sendMessage != null && !sendMessage.getText().isBlank()) {
                client.execute(sendMessage);
            }
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке ответа Pomodoro", e);
            sendPomodoroFallback(chatId, sendMessage);
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
            log.error("Ошибка при отправке вопроса о статистике", e);
        }
    }

    private void sendPomodoroFallback(Long chatId, SendMessage originalMessage) {
        if (originalMessage == null) return;

        SendMessage fallback = SendMessage.builder()
                .chatId(chatId)
                .text(originalMessage.getText() + "\n\n(⚠️ Мотивашку с картинкой отправить не удалось)")
                .build();
        try {
            client.execute(fallback);
        } catch (TelegramApiException ex) {
            log.error("Ошибка при отправке fallback-сообщения", ex);
        }
    }
}