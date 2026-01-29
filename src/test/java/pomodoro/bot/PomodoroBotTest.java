package pomodoro.bot;

import bot.utils.CsvStatsReader;
import bot.utils.StatsUtils;
import bot.utils.StatsWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import pomodoro.core.*;
import pomodoro.service.PomodoroManager;
import pomodoro.service.StatsLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PomodoroBotTest {

    private static final Long CHAT_ID = 18L;

    @Mock
    private PomodoroSender senderMock;
    @Mock
    private PomodoroManager manager;
    @Mock
    private StatsLogger statsLogger;
    @Mock
    private PomodoroSession session;
    @Mock
    CsvStatsReader reader;
    private PomodoroBot pomodoroBotTest;
    private PomodoroBot pomodoroBot;

    @BeforeEach
    void setUp() {
        pomodoroBot = new PomodoroBot(senderMock);
        Map<Phase, List<MotivationPhoto>> motivationPhotos = Map.of(
                Phase.WORK, List.of(new MotivationPhoto("work1", "path")),
                Phase.SHORT_BREAK, List.of(new MotivationPhoto("rest1", "path")),
                Phase.LONG_BREAK, List.of(new MotivationPhoto("rest2", "path"))
        );
        StatsUtils statsUtils = new StatsUtils();
        ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();
        pomodoroBotTest = new PomodoroBot(
                senderMock,
                manager,
                motivationPhotos,
                statsLogger,
                reader,
                statsUtils,
                scheduled
        );
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

    @Test
    @DisplayName("Завершение сеанса: логирует финальную фазу и отправляет статистику")
    void finishSession_shouldLogAndSendStats() {

        PomodoroStats stats = new PomodoroStats();
        stats.setRestSessions(1);
        stats.setWorkSessions(2);
        stats.setWorkMinutes(Duration.ofMinutes(50));
        stats.setRestMinutes(Duration.ofMinutes(5));

        when(session.getState()).thenReturn(SessionState.RUNNING);
        when(manager.getSession(CHAT_ID)).thenReturn(session);
        when(reader.readMonthlyStats(CHAT_ID)).thenReturn(stats);

        Update endUpdate = createUpdateWithText(CHAT_ID, "Завершить сеанс ✅");
        PomodoroReply reply = pomodoroBotTest.handleAnswer(endUpdate);

        verify(senderMock).sendPomodoroReply(
                eq(CHAT_ID),
                argThat(msg ->
                        msg.text().contains("Сессия завершена. ✅")
                                && msg.isFinished()
                )
        );

        verify(senderMock).sendFinalStatsQuestion(
                eq(CHAT_ID),
                argThat(text ->
                        text.contains("📊 Хотите вывести статистику за последние 30 дней?")
                )
        );

        Update finishUpdate = createUpdateWithText(CHAT_ID, "Да 📊");
        pomodoroBotTest.handleAnswer(finishUpdate);

        verify(senderMock, atLeastOnce()).sendPomodoroReply(eq(CHAT_ID),
                argThat(msg ->
                        msg.text().contains("Статистика за последние тридцать дней")
                                && msg.text().contains("Провели 2 сессий за работой")
                                && msg.text().contains("Общее время работы: 0 час. 50 мин.")
                                && msg.isFinished()
                )
        );

        assertThat(reply.text()).isEmpty();
    }

    @Test
    @DisplayName("logCurrentPhase логирует WORK, SHORT_BREAK и LONG_BREAK с верной длительностью")
    void logCurrentPhase_shouldLogAllPhasesWithCorrectDurations() {
        PomodoroServiceSettings settings = new PomodoroServiceSettings(
                Duration.ofMinutes(25),
                Duration.ofMinutes(5),
                Duration.ofMinutes(15),
                3
        );

        when(manager.getSession(CHAT_ID)).thenReturn(session);

        when(session.getCurrentPhase()).thenReturn(Phase.WORK);
        pomodoroBotTest.logCurrentPhase(CHAT_ID, settings);

        when(session.getCurrentPhase()).thenReturn(Phase.SHORT_BREAK);
        pomodoroBotTest.logCurrentPhase(CHAT_ID, settings);

        when(session.getCurrentPhase()).thenReturn(Phase.LONG_BREAK);
        pomodoroBotTest.logCurrentPhase(CHAT_ID, settings);

        InOrder inOrder = inOrder(statsLogger);

        inOrder.verify(statsLogger).logPhase(
                eq(CHAT_ID),
                eq(Phase.WORK),
                eq(Duration.ofMinutes(25)),
                any()
        );
        inOrder.verify(statsLogger).logPhase(
                eq(CHAT_ID),
                eq(Phase.SHORT_BREAK),
                eq(Duration.ofMinutes(5)),
                any()
        );
        inOrder.verify(statsLogger).logPhase(
                eq(CHAT_ID),
                eq(Phase.LONG_BREAK),
                eq(Duration.ofMinutes(15)),
                any()
        );
    }


    @Test
    @DisplayName("onPhaseFinished: при превышении лимита обновляет статистику, шлёт финальное сообщение и завершает сессию")
    void onPhaseFinished_shouldUpdateStatsSendMessageAndEndSession_whenOverLimit() {

        PomodoroServiceSettings settings = new PomodoroServiceSettings(
                Duration.ofMinutes(25),
                Duration.ofMinutes(5),
                Duration.ofMinutes(15),
                3
        );
        when(session.getCurrentPhase()).thenReturn(Phase.WORK);
        when(manager.getSession(CHAT_ID)).thenReturn(session);
        when(manager.chooseMotivationForSession(session)).thenReturn(new MotivationPhoto(
                "path", "motivationTitle"));
        when(manager.getSettings(CHAT_ID)).thenReturn(settings);
        when(manager.isOverLimit(session)).thenReturn(true);
        when(manager.getNextPhase(session, CHAT_ID)).thenReturn(Phase.LONG_BREAK);

        pomodoroBotTest.onPhaseFinished(CHAT_ID);

        verify(senderMock).sendPomodoroReply(eq(CHAT_ID),
                argThat(msg ->
                        msg.text().contains("превысила лимит времени существования")
                                && msg.text().contains("Вам присваивается звание")
                                && msg.isFinished()
                )
        );
        verify(statsLogger).logPhase(
                eq(CHAT_ID),
                eq(Phase.WORK),
                eq(Duration.ofMinutes(25)),
                any(Instant.class)
        );
        verify(session).completeCurrentPhase();
        verify(manager).endSession(CHAT_ID);
        verify(manager).cancelFuture(CHAT_ID);
        verify(senderMock, times(2)).sendPomodoroReply(anyLong(), any());
    }

    @Test
    @DisplayName("onPhaseFinished: при следующей фазе WORK стартует рабочий цикл")
    void onPhaseFinished_shouldStartWorkPhase_whenNextPhaseIsWork() {
        PomodoroServiceSettings settings = new PomodoroServiceSettings(
                Duration.ofMinutes(25),
                Duration.ofMinutes(5),
                Duration.ofMinutes(15),
                3
        );

        when(manager.getSession(CHAT_ID)).thenReturn(session);
        when(manager.getSettings(CHAT_ID)).thenReturn(settings);
        when(manager.isOverLimit(session)).thenReturn(false);
        when(session.isWarnedAboutLimit()).thenReturn(true);
        when(manager.getNextPhase(session, CHAT_ID)).thenReturn(Phase.WORK);
        when(manager.chooseMotivationForSession(session))
                .thenReturn(new MotivationPhoto("path", "motivationTitle"));

        pomodoroBotTest.onPhaseFinished(CHAT_ID);

        verify(senderMock).sendPomodoroReply(
                eq(CHAT_ID),
                argThat(msg ->
                        msg.text().contains("Перерыв окончен, поехали дальше!")
                                && !msg.isFinished()
                )
        );
    }

    @Test
    @DisplayName("onPhaseFinished: при приближении к лимиту предупреждает пользователя и ставит флаг")
    void onPhaseFinished_shouldWarnUserAboutLimit_whenCloseToLimitAndNotWarned() {
        PomodoroServiceSettings settings = new PomodoroServiceSettings(
                Duration.ofMinutes(25),
                Duration.ofMinutes(5),
                Duration.ofMinutes(15),
                3
        );

        when(manager.getSession(CHAT_ID)).thenReturn(session);
        when(manager.getSettings(CHAT_ID)).thenReturn(settings);
        when(manager.isOverLimit(session)).thenReturn(false);
        when(session.isWarnedAboutLimit()).thenReturn(false);
        when(manager.isCloseToLimit(eq(session), any())).thenReturn(true);
        when(manager.getNextPhase(session, CHAT_ID)).thenReturn(Phase.SHORT_BREAK);
        when(manager.chooseMotivationForSession(session))
                .thenReturn(new MotivationPhoto("path", "motivationTitle"));

        pomodoroBotTest.onPhaseFinished(CHAT_ID);
        verify(session).setWantedAboutLimit(true);
        verify(senderMock, times(2)).sendPomodoroReply(anyLong(), any());

        verify(senderMock, atLeastOnce()).sendPomodoroReply(eq(CHAT_ID),
                argThat(msg ->
                        msg.text().contains("Уважаемый пользователь, с момента первого запуска")
                                && msg.text().contains("сессия будет закрыта по достижению лимита")
                                && !msg.isFinished()
                )
        );
        verify(senderMock, atLeastOnce()).sendPomodoroReply(
                eq(CHAT_ID),
                argThat(msg ->
                        msg.text().contains("Пора сделать короткий перерыв! \uD83E\uDDD8\u200D♂\uFE0F☕")
                )
        );
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
