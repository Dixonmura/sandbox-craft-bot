package pomodoro.core;

import lombok.Data;

import java.time.Duration;

@Data
public class PomodoroStats {
    private Duration workMinutes;
    private Duration restMinutes;
    private int workSessions;
    private int restSessions;
}
