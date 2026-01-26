package pomodoro.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PomodoroSessionTest {

    PomodoroSession session;

    @BeforeEach
    void setUp() {
        session = new PomodoroSession(Phase.WORK, Duration.ofMillis(3));
    }

    @Test
    @DisplayName("Проверка корректного создания экземпляра PomodoroSession")
    void constructor_shouldCreatePomodoro_whenDataIsValid() {
        assertThat(session)
                .isNotNull()
                .extracting(
                        PomodoroSession::getCurrentPhase,
                        PomodoroSession::getCompleteWorkingCycles,
                        PomodoroSession::isWarnedAboutLimit)
                .containsExactly(Phase.WORK, 0, false);
    }

    @Test
    @DisplayName("Проверка корректного создания экземпляра PomodoroSession")
    void checkCreateAndWork_shouldCreateAndCorrectlyWorking_whenDataIsValid() throws InterruptedException {
        session.startCurrentPhase(Duration.ofMillis(3));
        assertThat(session.isCurrentPhaseFinished())
                .isFalse();
        Thread.sleep(100);
        session.completeCurrentPhase();
        assertThat(session.isCurrentPhaseFinished())
                .isTrue();
        assertThat(session.getCompleteWorkingCycles())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Проверка корректного создания экземпляра PomodoroSession с пограничным значением")
    void constructor_shouldCreatePomodoro_whenDataInBoundaryValue() throws InterruptedException {
        PomodoroSession s = new PomodoroSession(Phase.WORK, Duration.ofMillis(3));
        s.startCurrentPhase(Duration.ofMillis(3));
        assertThat(s.getReminingTime())
                .isPositive();
        Thread.sleep(100);
        s.completeCurrentPhase();
        s.setFinished(true);
        assertThat(s.isFinished())
                .isTrue();
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения при создании PomodoroSession с невалидными данными")
    void constructor_shouldThrowsIllegalArgumentException_whenDataInvalid() {
        assertThatThrownBy(() -> new PomodoroSession(Phase.WORK, Duration.ofMinutes(-3)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(" должно быть позитивным");
        assertThatThrownBy(() -> new PomodoroSession(Phase.WORK, Duration.ofMinutes(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("значение Duration должно");
    }

    @Test
    @DisplayName("Проверка попытки завершить фазу до окончания работы таймера")
    void completeCurrentPhase_shouldThrowsIllegalStateException_whenTimerNotDone() {
        session.startCurrentPhase(Duration.ofMinutes(5));
        assertThatThrownBy(() -> session.completeCurrentPhase())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Фаза ещё не завершена");
    }
}