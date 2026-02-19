package bot.utils;

import markups.VacancyKeyboardKey;
import movie_quiz.bot.BotReply;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import pomodoro.bot.PomodoroReply;
import vacancy_tracker.bot.CallbackPrefixes;
import vacancy_tracker.bot.VacancyReply;
import vacancy_tracker.bot.types.SettingOptions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplyUtilsTest {

    private final ClassLoader classLoader = mock(ClassLoader.class);
    private final Long CHAT_ID = 11L;

    @Test
    @DisplayName("sendMessageQuiz возвращает финальное сообщение с удалением клавиатуры")
    void sendMessageQuiz_shouldReturnFinalMessageWithKeyboardRemove_whenFinished() {
        BotReply reply = new BotReply("text", List.of(), true, null);

        SendMessage message = ReplyUtils.sendMessageQuiz(reply, CHAT_ID);

        assertThat(message.getText())
                .isNotNull()
                .isNotBlank()
                .contains("text");
        assertThat(message.getChatId())
                .contains("11");
        assertThat(message.getReplyMarkup())
                .isInstanceOf(ReplyKeyboardRemove.class);
        ReplyKeyboardRemove remove = (ReplyKeyboardRemove) message.getReplyMarkup();
        assertThat(remove.getRemoveKeyboard())
                .isTrue();
    }

    @Test
    @DisplayName("sendMessageQuiz возвращает игровое сообщение с клавиатурой")
    void sendMessageQuiz_shouldReturnMessageWithKeyboardMarkup_whenGameMessage() {
        BotReply reply = new BotReply("gameMessage",
                List.of("Фильм 1",
                        "Фильм 2",
                        "Фильм 3",
                        "Фильм 4"), false, null);

        SendMessage message = ReplyUtils.sendMessageQuiz(reply, CHAT_ID);

        assertThat(message.getText())
                .isNotNull()
                .isNotBlank()
                .contains("gameMessage");
        assertThat(message.getChatId())
                .contains("11");

        ReplyKeyboard keyboard = message.getReplyMarkup();
        assertThat(keyboard)
                .isNotNull()
                .asInstanceOf(InstanceOfAssertFactories.type(ReplyKeyboardMarkup.class))
                .satisfies(
                        k -> {
                            assertThat(k.getKeyboard()).hasSize(3);
                            assertThat(k.getKeyboard().getFirst())
                                    .extracting(KeyboardButton::getText)
                                    .containsExactly("Фильм 1",
                                            "Фильм 2");
                        }
                );
    }

    @Test
    @DisplayName("sendPhotoQuiz создаёт SendPhoto при валидном imagePath")
    void sendPhotoQuiz_shouldCreateSendPhoto_whenImageExists() {
        BotReply reply = new BotReply("text", List.of(), false, "img.png");
        InputStream is = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        when(classLoader.getResourceAsStream("img.png")).thenReturn(is);

        SendPhoto photo = ReplyUtils.sendPhotoQuiz(reply, CHAT_ID, classLoader);

        assertThat(photo).isNotNull();
        assertThat(photo.getChatId()).isEqualTo("11");
        assertThat(photo.getPhoto()).isNotNull();
    }

    @Test
    @DisplayName("sendPhotoPomodoro создает SendPhoto при валидном imagePath")
    void sendPhotoPomodoro_shouldCreateSendPhoto_whenImagePathIsValid() {
        PomodoroReply reply = new PomodoroReply("text", "img.png", true);

        InputStream is = new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));
        when(classLoader.getResourceAsStream("img.png")).thenReturn(is);
        SendPhoto photo = ReplyUtils.sendPhotoPomodoro(
                reply,
                CHAT_ID,
                classLoader);

        assertThat(photo).isNotNull();
        assertThat(photo.getChatId()).isEqualTo("11");
        assertThat(photo.getPhoto()).isNotNull();
    }

    @Test
    @DisplayName("Проверка формирования сообщения с клавиатурой для выбора региона")
    void sendMessage_shouldCreateSendMessageWithRegionKeyboard() {
        SendMessage messageWithRegionKeyboard = ReplyUtils.sendMessageVacancy(
                new VacancyReply(76L, SettingOptions.REGION.getTitle(), VacancyKeyboardKey.REGION_KEYBOARD)
        );
        assertThat(messageWithRegionKeyboard.getChatId())
                .isEqualTo("76");

        assertThat(messageWithRegionKeyboard.getText())
                .isEqualTo(SettingOptions.REGION.getTitle());

        assertThat(messageWithRegionKeyboard.getReplyMarkup())
                .isInstanceOf(InlineKeyboardMarkup.class);

        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) messageWithRegionKeyboard.getReplyMarkup();
        assertThat(markup.getKeyboard())
                .isNotEmpty()
                .hasSize(6);

        InlineKeyboardRow row = markup.getKeyboard().getFirst();
        assertThat(row)
                .isNotEmpty()
                .hasSize(1);

        assertThat(row.getFirst().getCallbackData())
                .isNotNull()
                .isEqualTo(CallbackPrefixes.REGION_CODE_PREFIX + "1");

    }

    @Test
    @DisplayName("Проверка формирования сообщения с клавиатурой для выбора запуска планировщика")
    void sendMessage_shouldCreateSendMessageWithReadyKeyboard() {
        SendMessage messageWithRegionKeyboard = ReplyUtils.sendMessageVacancy(
                new VacancyReply(45L, SettingOptions.MIN_SALARY.getTitle(), VacancyKeyboardKey.MIN_SALARY_KEYBOARD)
        );
        assertThat(messageWithRegionKeyboard.getChatId())
                .isEqualTo("45");

        assertThat(messageWithRegionKeyboard.getText())
                .isEqualTo(SettingOptions.MIN_SALARY.getTitle());

        assertThat(messageWithRegionKeyboard.getReplyMarkup())
                .isInstanceOf(InlineKeyboardMarkup.class);

        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) messageWithRegionKeyboard.getReplyMarkup();
        assertThat(markup.getKeyboard())
                .isNotEmpty()
                .hasSize(6);

        InlineKeyboardRow row = markup.getKeyboard().getFirst();
        assertThat(row)
                .isNotEmpty()
                .hasSize(1);

        assertThat(row.getFirst().getCallbackData())
                .isNotNull()
                .isEqualTo(CallbackPrefixes.SALARY_PREFIX + "25000");

    }

    @Test
    @DisplayName("Проверка формирования сообщения с клавиатурой для старта VacancyBot")
    void sendMessage_shouldCreateSendMessageWithStartKeyboard() {
        SendMessage messageWithStartKeyboard = ReplyUtils.sendMessageVacancy(
                new VacancyReply(77L, "startVacancyBot", VacancyKeyboardKey.START_KEYBOARD)
        );

        assertThat(messageWithStartKeyboard.getText())
                .isEqualTo("startVacancyBot");

        ReplyKeyboard keyboardMarkup = messageWithStartKeyboard.getReplyMarkup();

        assertThat(keyboardMarkup)
                .isNotNull()
                .isInstanceOf(InlineKeyboardMarkup.class);
    }

    @Test
    @DisplayName("Если ключ NONE, сообщение создаётся без клавиатуры")
    void sendMessage_shouldNotSetKeyboard_whenKeyIsNone() {
        VacancyReply reply = new VacancyReply(76L, "text", VacancyKeyboardKey.NONE);

        SendMessage message = ReplyUtils.sendMessageVacancy(reply);

        assertThat(message.getReplyMarkup()).isNull();
    }

    @Test
    @DisplayName("Проверка не создания SendPhoto, когда BotReply null")
    void sendPhotoQuiz_shouldReturnNull_whenReplyIsNull() {
        SendPhoto photo = ReplyUtils.sendPhotoQuiz(null, 123L, classLoader);
        assertThat(photo).isNull();
    }

    @Test
    @DisplayName("Проверка не создания SendPhoto, когда chatId null")
    void sendPhotoQuiz_shouldReturnNull_whenChatIdIsNull() {
        BotReply reply = new BotReply("text", List.of(), false, "img.png");

        SendPhoto photo = ReplyUtils.sendPhotoQuiz(reply, null, classLoader);

        assertThat(photo).isNull();
    }

    @Test
    @DisplayName("Проверка не создания SendPhoto, когда imagePath empty")
    void sendPhotoQuiz_shouldReturnNull_whenImagePathBlank() {
        BotReply reply = new BotReply("text", List.of(), false, "   ");

        SendPhoto photo = ReplyUtils.sendPhotoQuiz(reply, 123L, classLoader);

        assertThat(photo).isNull();
    }

    @Test
    @DisplayName("Проверка не создания SendPhoto")
    void sendPhotoQuiz_shouldReturnNull_whenResourceNotFound() {
        BotReply reply = new BotReply("text", List.of(), false, "img.png");
        when(classLoader.getResourceAsStream("img.png")).thenReturn(null);

        SendPhoto photo = ReplyUtils.sendPhotoQuiz(reply, 123L, classLoader);

        assertThat(photo).isNull();
    }


    @Test
    @DisplayName("Проверка не создания SendPhoto, когда PomodoroReply null")
    void sendPhotoPomodoro_shouldReturnNull_whenReplyIsNull() {
        SendPhoto photo = ReplyUtils.sendPhotoPomodoro(null, CHAT_ID, classLoader);
        assertThat(photo).isNull();
    }

    @Test
    @DisplayName("Проверка не создания SendPhoto, когда chatId PomodoroReply null")
    void sendPhotoPomodoro_shouldReturnNull_whenChatIdIsNull() {
        PomodoroReply reply = new PomodoroReply("text", "img.png", false);

        SendPhoto photo = ReplyUtils.sendPhotoPomodoro(reply, null, classLoader);

        assertThat(photo).isNull();
    }

    @Test
    @DisplayName("Проверка не создания SendPhoto, когда imagePath PomodoroReply empty")
    void sendPhotoPomodoro_shouldReturnNull_whenImagePathBlank() {
        PomodoroReply reply = new PomodoroReply("text", "    ", false);

        SendPhoto photo = ReplyUtils.sendPhotoPomodoro(reply, CHAT_ID, classLoader);

        assertThat(photo).isNull();
    }

    @Test
    @DisplayName("Проверка не создания SendPhoto")
    void sendPhotoPomodoro_shouldReturnNull_whenResourceNotFound() {
        PomodoroReply reply = new PomodoroReply("text", "img.png", false);
        when(classLoader.getResourceAsStream("img.png")).thenReturn(null);

        SendPhoto photo = ReplyUtils.sendPhotoPomodoro(reply, CHAT_ID, classLoader);

        assertThat(photo).isNull();
    }

    @Test
    @DisplayName("Проверка не создания SendPhoto")
    void sendMessagePomodoro_shouldReturnNull_whenPomodoroReplyNull() {
        assertThat(ReplyUtils.sendMessagePomodoro(null, CHAT_ID)).isNull();
    }

    @Test
    @DisplayName("Проверка не создания SendMessage")
    void sendMessageQuiz_shouldReturnNull_whenBotReplyNull() {
        assertThat(ReplyUtils.sendMessageQuiz(null, CHAT_ID)).isNull();
    }
}