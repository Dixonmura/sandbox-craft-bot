package pomodoro.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pomodoro.core.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PomodoroManagerTest {

    PomodoroManager pomodoroManager;
    PomodoroServiceSettings settings;
    @Mock
    PomodoroSession mockSession;
    PomodoroSession session;
    Map<Phase, List<MotivationPhoto>> listPhoto;
    Long chatId;

    @BeforeEach
    void setUp() {
        chatId = 1L;
        settings = new PomodoroServiceSettings(Duration.ofMillis(250), Duration.ofMillis(50), Duration.ofMillis(150), 3);
        listPhoto = Map.of(Phase.WORK, List.of(new MotivationPhoto("assets/motivations/motiv1.png", "работа1")),
                Phase.SHORT_BREAK, List.of(new MotivationPhoto("assets/motivations/rest1.png", "отдых1")),
                Phase.LONG_BREAK, List.of(new MotivationPhoto("assets/motivations/rest2.png", "отдых2")));
        pomodoroManager = new PomodoroManager(listPhoto);
        session = new PomodoroSession(Phase.WORK, Duration.ofMillis(30));
        pomodoroManager.setSettings(chatId, settings);
    }

    @Test
    @DisplayName("Проверка создания экземпляра PomodoroBot с валидными данными")
    void constructor_shouldCreatePomodoroManager_whenDataIsValid() {
        pomodoroManager.addSession(chatId, session);
        pomodoroManager.startWorkSession(chatId, settings.workDuration());
        MotivationPhoto photo;

        photo = pomodoroManager.chooseMotivationForSession(pomodoroManager.getSession(chatId));
        assertThat(pomodoroManager)
                .isNotNull();

        assertThat(photo)
                .extracting(MotivationPhoto::pathToPhoto, MotivationPhoto::motivationTitle)
                .containsExactly("assets/motivations/motiv1.png", "работа1");


        when(mockSession.getCurrentPhase()).thenReturn(Phase.SHORT_BREAK);
        photo = pomodoroManager.chooseMotivationForSession(mockSession);
        assertThat(photo)
                .extracting(MotivationPhoto::pathToPhoto, MotivationPhoto::motivationTitle)
                .containsExactly("assets/motivations/rest1.png", "отдых1");

        when(mockSession.getCurrentPhase()).thenReturn(Phase.LONG_BREAK);
        photo = pomodoroManager.chooseMotivationForSession(mockSession);
        assertThat(photo)
                .extracting(MotivationPhoto::pathToPhoto, MotivationPhoto::motivationTitle)
                .containsExactly("assets/motivations/rest2.png", "отдых2");
    }

    @Test
    @DisplayName("Проверка корректной работы и завершения цикла WorkPhase")
    void completePhase_shouldCheckStateAndCompleteWorkPhase_whenDataIsValid() throws InterruptedException {
        pomodoroManager.addSession(chatId, session);
        pomodoroManager.startWorkSession(chatId, settings.workDuration());
        assertThat(pomodoroManager.getSession(chatId).getCompleteWorkingCycles())
                .isZero();

        assertThat(pomodoroManager.getSession(chatId).isCurrentPhaseFinished())
                .isFalse();

        Thread.sleep(400);
        assertThat(pomodoroManager.getSession(chatId).isCurrentPhaseFinished())
                .isTrue();

        pomodoroManager.getSession(chatId).completeCurrentPhase();
        assertThat(pomodoroManager.getSession(chatId).isCurrentPhaseFinished())
                .isTrue();
        assertThat(pomodoroManager.getSession(chatId).getCompleteWorkingCycles())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("проверка корректной смены фаз")
    void changePhases_shouldCorrectlyChangePhases_whenCountCyclesForPhasesIsValid() {
        when(mockSession.getCurrentPhase()).thenReturn(Phase.WORK);
        when(mockSession.getCompleteWorkingCycles()).thenReturn(2);
        assertThat(pomodoroManager.getNextPhase(mockSession, chatId))
                .isEqualTo(Phase.SHORT_BREAK);

        when(mockSession.getCompleteWorkingCycles()).thenReturn(3);
        assertThat(pomodoroManager.getNextPhase(mockSession, chatId))
                .isEqualTo(Phase.LONG_BREAK);

        when(mockSession.getCurrentPhase()).thenReturn(Phase.LONG_BREAK);
        assertThat(pomodoroManager.getNextPhase(mockSession, chatId))
                .isEqualTo(Phase.WORK);

        when(mockSession.getCurrentPhase()).thenReturn(Phase.SHORT_BREAK);
        assertThat(pomodoroManager.getNextPhase(mockSession, chatId))
                .isEqualTo(Phase.WORK);
    }

    @Test
    @DisplayName("Проверка запуска фаз отдыха и корректной смены фазы на фазу работы")
    void changeRestPhases_shouldStartBreakingSessions_whenCyclesIsValid() {

        when(mockSession.getCompleteWorkingCycles()).thenReturn(1);
        assertThat(pomodoroManager.shouldStartLongBreak(mockSession, chatId)).isFalse();

        when(mockSession.getCompleteWorkingCycles()).thenReturn(3);
        assertThat(pomodoroManager.shouldStartLongBreak(mockSession, chatId)).isTrue();

        when(mockSession.getCompleteWorkingCycles()).thenReturn(5);
        assertThat(pomodoroManager.shouldStartLongBreak(mockSession, chatId)).isFalse();

        when(mockSession.getCompleteWorkingCycles()).thenReturn(6);
        assertThat(pomodoroManager.shouldStartLongBreak(mockSession, chatId)).isTrue();

        when(mockSession.getCompleteWorkingCycles()).thenReturn(9);
        assertThat(pomodoroManager.shouldStartLongBreak(mockSession, chatId)).isTrue();
    }

    @Test
    @DisplayName("Проверка корректного расчета ранга")
    void calculateRank_shouldReturnCorrectlyRank_whenCountCyclesForCalculateIsValid() {
        when(mockSession.getCompleteWorkingCycles()).thenReturn(2);
        assertThat(pomodoroManager.calculateRank(mockSession))
                .isEqualTo(PomodoroRank.NONE.title());

        when(mockSession.getCompleteWorkingCycles()).thenReturn(4);
        assertThat(pomodoroManager.calculateRank(mockSession))
                .contains("Новичок!");

        when(mockSession.getCompleteWorkingCycles()).thenReturn(6);
        assertThat(pomodoroManager.calculateRank(mockSession))
                .contains("Настоящий трудяга!");

        when(mockSession.getCompleteWorkingCycles()).thenReturn(10);
        assertThat(pomodoroManager.calculateRank(mockSession))
                .contains("Железный дровосек!");
    }

    @Test
    @DisplayName("Таймер короткого и длинного отдыха дорабатывает до конца")
    void restTimers_shouldFinishAfterConfiguredDuration() throws InterruptedException {
        pomodoroManager.addSession(chatId, session);
        pomodoroManager.startShortRestSession(chatId, settings.shortRestDuration());
        Thread.sleep(150);
        assertThat(pomodoroManager.getSession(chatId).isCurrentPhaseFinished()).isTrue();

        pomodoroManager.startLongRestSession(chatId, settings.longRestDuration());
        Thread.sleep(250);
        assertThat(pomodoroManager.getSession(chatId).isCurrentPhaseFinished()).isTrue();
    }

    @Test
    @DisplayName("Проверка работы отчета времени и закрытия сессии по таймауту")
    void sessionLifecycle_shouldWarnAboutLimitAndRemoveSession_whenMaxTimeExceeded() {
        Long id = 11L;
        pomodoroManager.addSession(id, mockSession);
        when(mockSession.getStartTime()).thenReturn(Instant.now().minus(Duration.ofHours(15)));
        assertThat(pomodoroManager.isCloseToLimit(mockSession, Duration.ofHours(2)))
                .isTrue();
        assertThat(pomodoroManager.isOverLimit(mockSession))
                .isFalse();
        when(mockSession.getStartTime()).thenReturn(Instant.now().minus(Duration.ofHours(17)));
        assertThat(pomodoroManager.isOverLimit(mockSession))
                .isTrue();
        pomodoroManager.endSession(id);
        assertThatThrownBy(() -> pomodoroManager.getSession(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Невозможно получить ");
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения при создании экземпляра с некорректными данными")
    void constructor_shouldThrowsIllegalArgumentException_whenDataInvalid() {
        assertThatThrownBy(() -> new PomodoroManager(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("motivationPhotos не может быть пустым или null");

        assertThatThrownBy(() -> new PomodoroManager(Collections.emptyMap()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("motivationPhotos не может быть пустым или null");
    }

    @Test
    @DisplayName("Бросает IllegalStateException, если для фазы нет мотивирующих фото")
    void chooseMotivation_shouldThrowIllegalState_whenPhotosListMissing() {
        Map<Phase, List<MotivationPhoto>> photos = Map.of(
                Phase.WORK, List.of(new MotivationPhoto("p.png", "t"))
        );
        PomodoroManager manager = new PomodoroManager(photos);

        when(mockSession.getCurrentPhase()).thenReturn(Phase.SHORT_BREAK);

        assertThatThrownBy(() -> manager.chooseMotivationForSession(mockSession))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SHORT_BREAK");
    }

    @Test
    @DisplayName("Проверка выбрасывания исключении при попытке получить сессию, когда она не добавлена")
    void getSession_shouldThrowsIllegalStateException_whenSessionHasNotAdded() {
        assertThatThrownBy(() -> pomodoroManager.getSession(chatId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Невозможно получить сессию ");
    }
}