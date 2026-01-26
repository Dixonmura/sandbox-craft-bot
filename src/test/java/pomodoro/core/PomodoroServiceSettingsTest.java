package pomodoro.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PomodoroServiceSettingsTest {

    @Test
    @DisplayName("Проверка корректного создания экземпляра класса")
    void constructor_shouldCreatePomodoroServiceSettings_whenDataIsValid() {
        PomodoroServiceSettings settings = new PomodoroServiceSettings(
                Duration.ofMinutes(25),
                Duration.ofMinutes(5),
                Duration.ofMinutes(15),
                3
        );

        assertThat(settings)
                .isNotNull()
                .extracting(PomodoroServiceSettings::workDuration,
                        PomodoroServiceSettings::shortRestDuration,
                        PomodoroServiceSettings::longRestDuration,
                        PomodoroServiceSettings::sessionsBeforeLongBreak)
                .containsExactly(
                        Duration.ofMinutes(25),
                        Duration.ofMinutes(5),
                        Duration.ofMinutes(15),
                        3);
    }

    @Test
    @DisplayName("Проверка корректного создания экземпляра класса при пограничных значениях")
    void constructor_shouldCreatePomodoroServiceSettings_whenDataInBoundaryValues() {
        PomodoroServiceSettings settings = new PomodoroServiceSettings(
                Duration.ofMinutes(1),
                Duration.ofMinutes(1),
                Duration.ofMinutes(1),
                1
        );

        assertThat(settings)
                .isNotNull()
                .extracting(PomodoroServiceSettings::workDuration,
                        PomodoroServiceSettings::shortRestDuration,
                        PomodoroServiceSettings::longRestDuration,
                        PomodoroServiceSettings::sessionsBeforeLongBreak)
                .containsExactly(
                        Duration.ofMinutes(1),
                        Duration.ofMinutes(1),
                        Duration.ofMinutes(1),
                        1);
    }

    @Test
    @DisplayName("Проверка выбрасывания исключений, если данные не корректны")
    void constructor_shouldThrowsIllegalArgumentException_whenDataInvalid() {
        assertThatThrownBy(
                () -> new PomodoroServiceSettings(null, Duration.ofMinutes(5), Duration.ofMinutes(15), 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("workDuration не может быть");
        assertThatThrownBy(
                () -> new PomodoroServiceSettings(Duration.ofMinutes(0), Duration.ofMinutes(5), Duration.ofMinutes(15), 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("workDuration не может быть null и должен");
        assertThatThrownBy(
                () -> new PomodoroServiceSettings(Duration.ofMinutes(25), null, Duration.ofMinutes(15), 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("shortRestDuration не ");
        assertThatThrownBy(
                () -> new PomodoroServiceSettings(Duration.ofMinutes(25), Duration.ofMinutes(0), Duration.ofMinutes(15), 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("shortRestDuration")
                .hasMessageContaining("и должен быть положительным");
        assertThatThrownBy(
                () -> new PomodoroServiceSettings(Duration.ofMinutes(25), Duration.ofMinutes(5), null, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("longRestDuration не может");
        assertThatThrownBy(
                () -> new PomodoroServiceSettings(Duration.ofMinutes(25), Duration.ofMinutes(5), Duration.ofMinutes(0), 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("быть положительным");
        assertThatThrownBy(
                () -> new PomodoroServiceSettings(Duration.ofMinutes(25), Duration.ofMinutes(5), Duration.ofMinutes(15), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sessionsBeforeLongBreak должен");
    }
}