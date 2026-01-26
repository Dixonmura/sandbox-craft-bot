package pomodoro.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimerTest {

    Timer timer;

    @BeforeEach
    void setUp() {
        timer = new Timer(Phase.WORK, Duration.ofMillis(20));
    }

    @Test
    @DisplayName("Проверка работы фазы таймера")
    void shouldChangePhaseWhenSetPhaseCalled() {
        assertThat(timer.getPhaseTimer())
                .isEqualByComparingTo(Phase.WORK);
        timer.setPhaseTimer(Phase.SHORT_BREAK);
        assertThat(timer.getPhaseTimer())
                .isEqualByComparingTo(Phase.SHORT_BREAK);
        timer.setPhaseTimer(Phase.LONG_BREAK);
        assertThat(timer.getPhaseTimer())
                .isEqualByComparingTo(Phase.LONG_BREAK);
    }

    @Test
    @DisplayName("Проверка корректной работы метода remaining")
    void remaining_shouldReturnCorrectlyValue() {
        timer.startTimer();
        assertThat(timer.isFinished())
                .isFalse();
        assertThat(timer.remaining())
                .isNotNull()
                .isPositive();
    }

    @Test
    @DisplayName("Проверка работы метода, возвращающего результат завершения отсчета")
    void isFinished_shouldFinishedIsTrue_whenTimeIsOut() throws InterruptedException {
        timer.startTimer();
        assertThat(timer.isFinished())
                .isFalse();
        Thread.sleep(200);
        assertThat(timer.isFinished())
                .isTrue();
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения, когда startTime null")
    void remaining_shoildTrowsIllegalStateExeption_whenStartTimeIsNull() throws IllegalStateException {
        assertThatThrownBy(() ->
                timer.remaining())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("невозможно рассчитать оставшееся время до вызова команды startTimer()");
    }
}