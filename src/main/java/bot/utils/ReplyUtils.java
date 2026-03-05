package bot.utils;

import markups.*;
import movie_quiz.bot.BotReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import pomodoro.bot.PomodoroReply;
import vacancy_tracker.bot.VacancyReply;

import java.io.InputStream;

import static vacancy_tracker.bot.VacancyMessages.*;

/**
 * Утилитарный класс для преобразования {@link BotReply}
 * в Telegram API объекты {@link SendMessage} и {@link SendPhoto}.
 * <p>
 * Содержит только статические методы и не предполагает создание экземпляров.
 */
public class ReplyUtils {

    private static final MovieQuizKeyboardFactory keyboardFactoryQuiz = new MovieQuizKeyboardFactory();
    private static final PomodoroKeyboardFactory keyboardFactoryPomodoro = new PomodoroKeyboardFactory();
    private static final VacancyTrackerKeyboardFactory keyboardFactoryVacancyTracker = new VacancyTrackerKeyboardFactory();
    private static final BotRouterKeyboardFactory keyboardFactoryRouter = new BotRouterKeyboardFactory();
    private static final Logger log = LogManager.getLogger(ReplyUtils.class);

    /**
     * Приватный конструктор предотвращает создание экземпляров утилитарного класса.
     */
    private ReplyUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Создаёт объект {@link SendPhoto} по данным ответа бота MovieQuiz.
     * <p>
     * Ожидается, что {@link BotReply#imagePath()} содержит путь к ресурсу в classpath.
     * В случае проблем (пустой путь или ресурс не найден) возвращает {@code null}.
     *
     * @param reply       доменный ответ бота, содержащий путь к изображению
     * @param chatId      идентификатор чата, в который необходимо отправить фото
     * @param classLoader загрузчик классов, из которого будет читаться ресурс-изображение
     * @return настроенный {@link SendPhoto} или {@code null}, если фото отправить нельзя
     */
    public static SendPhoto sendPhotoQuiz(BotReply reply, Long chatId, ClassLoader classLoader) {
        log.info("Вызов sendPhotoQuiz для chatId={}", chatId);

        if (reply == null) {
            log.error("sendPhoto вызван, когда BotReply null");
            return null;
        }
        if (chatId == null) {
            log.error("sendPhoto вызван, когда chatId null");
            return null;
        }

        String imagePath = reply.imagePath();
        if (imagePath == null || imagePath.isBlank()) {
            log.warn("ImagePath null или пустой для chatId={}", chatId);
            return null;
        }

        InputStream is = classLoader.getResourceAsStream(imagePath);
        if (is == null) {
            log.warn("Ресурс с изображением не найден по пути '{}' для chatId={}", imagePath, chatId);
            return null;
        }

        log.debug("Создание SendPhoto для chatId={} с imagePath='{}'", chatId, imagePath);
        InputFile inputFile = new InputFile(is, imagePath);

        return SendPhoto.builder()
                .chatId(chatId)
                .photo(inputFile)
                .build();
    }

    /**
     * Создаёт объект {@link SendMessage} по данным ответа бота.
     * <p>
     * Если квиз завершён ({@link BotReply#isFinished()} == true),
     * клавиатура удаляется с помощью {@link ReplyKeyboardRemove}.
     * В противном случае создаётся клавиатура с вариантами фильмов.
     *
     * @param reply  доменный ответ бота, содержащий текст и состояние квиза
     * @param chatId идентификатор чата, в который необходимо отправить сообщение
     * @return настроенный {@link SendMessage}
     */
    public static SendMessage sendMessageQuiz(BotReply reply, Long chatId) {
        log.info("Вызов sendMessageQuiz для chatId={}", chatId);

        if (reply == null) {
            log.error("sendMessage вызван, когда BotReply null");
            return null;
        }
        if (chatId == null) {
            log.error("sendMessage вызван, когда chatId null");
            return null;
        }

        if (reply.isFinished()) {
            log.info("Отправка финального сообщения в чат chatId={}", chatId);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(reply.text())
                    .replyMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }

        log.info("Отправка игрового сообщения в чат chatId={}", chatId);
        return SendMessage.builder()
                .chatId(chatId)
                .text(reply.text())
                .replyMarkup(keyboardFactoryQuiz.createOptionsKeyboard(reply.movieTitles()))
                .build();
    }

    /**
     * Создаёт объект {@link SendPhoto} по данным ответа бота Pomodoro.
     * <p>
     * Ожидается, что {@link PomodoroReply#imagePath()} содержит путь к ресурсу в classpath.
     * В случае проблем (пустой путь или ресурс не найден) возвращает {@code null}.
     *
     * @param reply       доменный ответ бота, содержащий путь к изображению
     * @param chatId      идентификатор чата, в который необходимо отправить фото
     * @param classLoader загрузчик классов, из которого будет читаться ресурс-изображение
     * @return настроенный {@link SendPhoto} или {@code null}, если фото отправить нельзя
     */
    public static SendPhoto sendPhotoPomodoro(PomodoroReply reply, Long chatId, ClassLoader classLoader) {
        log.info("Вызов sendPhotoPomodoro для chatId={}", chatId);

        if (reply == null) {
            log.error("sendPhoto вызван, когда Pomodoro null");
            return null;
        }
        if (chatId == null) {
            log.error("sendPhoto вызван, когда chatId null");
            return null;
        }

        String imagePath = reply.imagePath();
        if (imagePath == null || imagePath.isBlank()) {
            log.warn("ImagePath null или пустой для chatId={}", chatId);
            return null;
        }

        InputStream is = classLoader.getResourceAsStream(imagePath);
        if (is == null) {
            log.warn("Ресурс с изображением не найден по пути '{}' для chatId={}", imagePath, chatId);
            return null;
        }

        log.debug("Создание SendPhoto для chatId={} с imagePath='{}'", chatId, imagePath);
        InputFile inputFile = new InputFile(is, imagePath);

        return SendPhoto.builder()
                .chatId(chatId)
                .photo(inputFile)
                .build();
    }

    /**
     * Создаёт объект {@link SendMessage} по данным ответа бота.
     * <p>
     * Если сессия завершена ({@link PomodoroReply#isFinished()} == true),
     * клавиатура удаляется с помощью {@link ReplyKeyboardRemove}.
     * В противном случае создаётся клавиатура с тремя вариантами ответа.
     *
     * @param reply  доменный ответ бота, содержащий текст и состояние Pomodoro
     * @param chatId идентификатор чата, в который необходимо отправить сообщение
     * @return настроенный {@link SendMessage}
     */
    public static SendMessage sendMessagePomodoro(PomodoroReply reply, Long chatId) {
        log.info("Вызов sendMessagePomodoro для chatId={}", chatId);

        if (reply == null) {
            log.error("sendMessage вызван, когда PomodoroReply null");
            return null;
        }
        if (chatId == null) {
            log.error("sendMessage вызван, когда chatId null");
            return null;
        }

        if (reply.isFinished()) {
            log.info("Отправка финального сообщения в чат chatId={}", chatId);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(reply.text())
                    .replyMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }

        log.info("Отправка игрового сообщения в чат chatId={}", chatId);
        return SendMessage.builder()
                .chatId(chatId)
                .text(reply.text())
                .replyMarkup(keyboardFactoryPomodoro.createButtonsKeyboard())
                .build();
    }

    /**
     * Создаёт объект {@link SendMessage} на основе {@link VacancyReply}.
     * <p>
     * В качестве текста используется {@link VacancyReply#text()}, а идентификатор чата берётся из {@link VacancyReply#userId()}.
     * Если {@link VacancyReply#keyboardKey()} не равен {@code null} и не равен {@link VacancyKeyboardKey#NONE},
     * к сообщению добавляется соответствующая клавиатура из {@link VacancyTrackerKeyboardFactory}.
     * Иначе сообщение отправляется без клавиатуры.
     *
     * @param reply доменный ответ бота с текстом и типом клавиатуры
     * @return настроенный {@link SendMessage} или {@code null}, если reply равен {@code null}
     */
    public static SendMessage sendMessageVacancy(VacancyReply reply) {
        if (reply == null) {
            log.error("sendMessage вызван, когда VacancyReply null");
            return null;
        }

        SendMessage.SendMessageBuilder builder = SendMessage.builder()
                .chatId(reply.userId())
                .text(reply.text());

        VacancyKeyboardKey key = reply.keyboardKey();
        if (key == null || key == VacancyKeyboardKey.NONE) {
            return builder.build();
        }


        switch (key) {
            case START_KEYBOARD -> builder.replyMarkup(keyboardFactoryVacancyTracker.createStartKeyboard());
            case SETTING_KEYBOARD -> builder.replyMarkup(keyboardFactoryVacancyTracker.createSettingKeyboard());
            case UTC_KEYBOARD -> {
                try {
                    int page = Integer.parseInt(reply.text());
                    createPaginationText(reply, builder, page, ENTER_UTC_OFFSET);
                    builder.replyMarkup(keyboardFactoryVacancyTracker.createUtcOffsetsKeyboard(page));
                } catch (NumberFormatException e) {
                    builder.replyMarkup(keyboardFactoryVacancyTracker.createUtcOffsetsKeyboard(0));
                }
            }
            case REGION_KEYBOARD -> {
                try {
                    int page = Integer.parseInt(reply.text());
                    createPaginationText(reply, builder, page, REGION_MESSAGE);
                    builder.replyMarkup(keyboardFactoryVacancyTracker.createRegionKeyboard(page));
                } catch (NumberFormatException e) {
                    builder.replyMarkup(keyboardFactoryVacancyTracker.createRegionKeyboard(0));
                }
            }
            case MIN_EXPERIENCE_KEYBOARD ->
                    builder.replyMarkup(keyboardFactoryVacancyTracker.createExperienceKeyboard());
            case MIN_SALARY_KEYBOARD -> builder.replyMarkup(keyboardFactoryVacancyTracker.createSalaryKeyboard());
            case KEY_WORD_KEYBOARD -> builder.replyMarkup(keyboardFactoryVacancyTracker.createKeyWordKeyboard());
            case NOTIFY_TIME_KEYBOARD -> {
                try {
                    int page = Integer.parseInt(reply.text());
                    createPaginationText(reply, builder, page, NOTIFY_TIME_MESSAGE);
                    builder.replyMarkup(keyboardFactoryVacancyTracker.createNotifyTimeKeyboard(page));
                } catch (NumberFormatException e) {
                    builder.replyMarkup(keyboardFactoryVacancyTracker.createNotifyTimeKeyboard(0));
                }
            }
            case YES_OR_NO_KEYBOARD -> builder.replyMarkup(keyboardFactoryVacancyTracker.createYesOrNoKeyboard());
            case READY_KEYBOARD -> builder.replyMarkup(keyboardFactoryVacancyTracker.createReadyKeyboard());
            case STOP_KEYBOARD -> builder.replyMarkup(keyboardFactoryVacancyTracker.createBotStopKeyboard());
            case ROUTER_MENU_KEYBOARD -> builder.replyMarkup(keyboardFactoryRouter.createMainMenuKeyboard());
            case START_AND_STOP_KEYBOARD -> builder.replyMarkup(keyboardFactoryVacancyTracker.createStartAndStopKeyboard());
            default -> log.warn("Неизвестный key: {}", key);
        }

        SendMessage message = builder.build();
        log.info("Отправка Vacancy сообщения в чат chatId={}", reply.userId());
        log.debug("Built vacancy message: userId={}, key={}, hasReplyMarkup={}, markupType={}",
                reply.userId(), key, message.getReplyMarkup() != null,
                message.getReplyMarkup() != null ? message.getReplyMarkup().getClass().getSimpleName() : "null");
        return message;
    }

    private static void createPaginationText(
            VacancyReply reply,
            SendMessage.SendMessageBuilder builder,
            int pageNum,
            String baseMessage) {
        StringBuilder paginationTextBuilder = new StringBuilder();

        paginationTextBuilder
                .append(baseMessage)
                .append("\n")
                .append("Стр. ");

        if (reply.text() == null || reply.text().isBlank()) {
            paginationTextBuilder.append("1");
        } else {
            paginationTextBuilder.append(pageNum + 1);
        }

        builder.text(paginationTextBuilder.toString());
    }
}
