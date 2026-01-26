package pomodoro.core;

import java.time.Duration;

public record PomodoroServiceSettings(
        Duration workDuration,
        Duration shortRestDuration,
        Duration longRestDuration,
        int sessionsBeforeLongBreak) {

    public PomodoroServiceSettings {
        if (workDuration == null || !workDuration.isPositive()) {
            throw new IllegalArgumentException("workDuration не может быть null и должен быть положительным");
        }
        if (shortRestDuration == null || !shortRestDuration.isPositive()) {
            throw new IllegalArgumentException("shortRestDuration не может быть null и должен быть положительным");
        }
        if (longRestDuration == null || !longRestDuration.isPositive()) {
            throw new IllegalArgumentException("longRestDuration не может быть null и должен быть положительным");
        }
        if (sessionsBeforeLongBreak < 1) {
            throw new IllegalArgumentException("sessionsBeforeLongBreak должен быть положительным");
        }
    }
}
