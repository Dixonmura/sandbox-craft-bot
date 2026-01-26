package pomodoro.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PomodoroBotTest {

    private static final Long CHAT_ID = 18L;

    @Mock
    private PomodoroSender senderMock;
    private PomodoroBot pomodoroBot;

    @BeforeEach
    void setUp() {
        pomodoroBot = new PomodoroBot(senderMock);
    }

    @Test
    @DisplayName("startPomodoro устанавливает начальное состояние настройки")
    void startPomodoro_shouldSetInitialSetupState() {
        Update update = createUpdateWithText(CHAT_ID, "/start");

        PomodoroReply reply = pomodoroBot.startPomodoro(update);

        assertThat(reply.text()).contains("Напиши, на сколько минут поставить");
        assertThat(reply.isFinished()).isTrue();
        assertThat(pomodoroBot.hasSession(CHAT_ID)).isTrue();
    }

    @Test
    @DisplayName("checkUserSetupState - полный happy path настройки")
    void checkUserSetupState_shouldCompleteFullSetup() {
        pomodoroBot.startPomodoro(createUpdateWithText(CHAT_ID, "/start"));
        assertThat(pomodoroBot.hasSession(CHAT_ID)).isTrue();

        Update update1 = createUpdateWithText(CHAT_ID, "25");
        PomodoroReply reply1 = pomodoroBot.handleAnswer(update1);
        assertThat(reply1.text()).contains("Период рабочего цикла определён ✅");
        assertThat(reply1.isFinished()).isTrue();

        Update update2 = createUpdateWithText(CHAT_ID, "5");
        PomodoroReply reply2 = pomodoroBot.handleAnswer(update2);
        assertThat(reply2.text()).contains("Период короткого отдыха определён ✅");
        assertThat(reply2.isFinished()).isTrue();

        Update update3 = createUpdateWithText(CHAT_ID, "15");
        PomodoroReply reply3 = pomodoroBot.handleAnswer(update3);
        assertThat(reply3.text()).contains("Период длинного отдыха определён ✅");
        assertThat(reply3.isFinished()).isTrue();

        Update update4 = createUpdateWithText(CHAT_ID, "3");
        PomodoroReply reply4 = pomodoroBot.handleAnswer(update4);
        assertThat(reply4.text()).contains("Количество циклов работы до длинного отдыха определено");
        assertThat(reply4.isFinished()).isFalse();

        pomodoroBot.handleAnswer(createUpdateWithText(CHAT_ID, "Старт"));

    }

    @Test
    @DisplayName("checkUserSetupState - валидация ввода")
    void checkUserSetupState_shouldHandleInvalidInput() {
        Update invalidText = createUpdateWithText(CHAT_ID, "abc");
        Update zeroValue = createUpdateWithText(CHAT_ID, "0");
        Update negativeValue = createUpdateWithText(CHAT_ID, "-5");

        pomodoroBot.startPomodoro(createUpdateWithText(CHAT_ID, "/start"));
        PomodoroReply replyText = pomodoroBot.handleAnswer(invalidText);
        PomodoroReply replyZero = pomodoroBot.handleAnswer(zeroValue);
        PomodoroReply replyNegative = pomodoroBot.handleAnswer(negativeValue);

        assertThat(replyText.text()).contains("Нужно ввести целое число");
        assertThat(replyZero.text()).contains("Число должно быть больше 0");
        assertThat(replyNegative.text()).contains("Число должно быть больше 0");
    }

    @Test
    @DisplayName("команда Старт запускает сессию и отправляет мотивационное фото")
    void handleAnswer_startCommand_shouldStartWorkSession() {

        pomodoroBot.startPomodoro(createUpdateWithText(CHAT_ID, "/start"));

        pomodoroBot.handleAnswer(createUpdateWithText(CHAT_ID, "25"));
        pomodoroBot.handleAnswer(createUpdateWithText(CHAT_ID, "5"));
        pomodoroBot.handleAnswer(createUpdateWithText(CHAT_ID, "15"));
        pomodoroBot.handleAnswer(createUpdateWithText(CHAT_ID, "3"));

        Update startUpdate = createUpdateWithText(CHAT_ID, "Старт \uD83D\uDE80");

        PomodoroReply reply = pomodoroBot.handleAnswer(startUpdate);
        verify(senderMock).sendPomodoroReply(eq(CHAT_ID),
                argThat(r -> r.text().contains("Отличный настрой! Отсчет пошел!") && !r.isFinished()));
        assertThat(reply.text()).isEmpty();

    }

    @Test
    @DisplayName("повторный Старт не перезапускает сессию, а предупреждает пользователя")
    void handleAnswer_secondStart_shouldWarnUser() {
        setupCompleteSettings(CHAT_ID);

        pomodoroBot.handleAnswer(createUpdateWithText(CHAT_ID, "Старт \uD83D\uDE80"));
        pomodoroBot.handleAnswer(createUpdateWithText(CHAT_ID, "Старт \uD83D\uDE80"));

        verify(senderMock).sendPomodoroReply(eq(CHAT_ID),
                argThat(r -> r.text().contains("Тссс… сессия уже идёт \uD83E\uDD2B")));
    }

    @Test
    @DisplayName("команда Стоп отменяет текущую фазу")
    void handleAnswer_stopCommand_shouldCancelCurrentPhase() {
        setupCompleteSettings(CHAT_ID);

        Update stopUpdate = createUpdateWithText(CHAT_ID, "Пауза ⏸\uFE0F");
        PomodoroReply reply = pomodoroBot.handleAnswer(stopUpdate);

        verify(senderMock).sendPomodoroReply(eq(CHAT_ID),
                argThat(r -> r.text().contains("Текущий цикл отменён ⏹\uFE0F")));
        assertThat(reply.text()).isEmpty();
    }

    @Test
    @DisplayName("команда Завершить сеанс формирует финальную статистику")
    void handleAnswer_finishSessionCommand_shouldSendStats() {
        setupCompleteSettings(CHAT_ID);

        pomodoroBot.handleAnswer(createUpdateWithText(CHAT_ID, "Старт \uD83D\uDE80"));

        Update finishUpdate = createUpdateWithText(CHAT_ID, "Завершить сеанс ✅");
        PomodoroReply reply = pomodoroBot.handleAnswer(finishUpdate);

        verify(senderMock).sendPomodoroReply(eq(CHAT_ID),
                argThat(r -> r.text().contains("Сессия завершена") && r.isFinished()));
        assertThat(reply.text()).isEmpty();
    }

    private void setupCompleteSettings(Long chatId) {
        pomodoroBot.startPomodoro(createUpdateWithText(CHAT_ID, "/start"));
        pomodoroBot.handleAnswer(createUpdateWithText(chatId, "25"));
        pomodoroBot.handleAnswer(createUpdateWithText(chatId, "5"));
        pomodoroBot.handleAnswer(createUpdateWithText(chatId, "15"));
        pomodoroBot.handleAnswer(createUpdateWithText(chatId, "3"));
    }

    private static Update createUpdateWithText(Long chatId, String text) {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat(chatId, "");
        message.setChat(chat);
        message.setText(text);
        update.setMessage(message);
        return update;
    }
}
