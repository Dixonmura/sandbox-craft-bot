package pomodoro.core;

import lombok.Data;

import java.time.Duration;

@Data
public class UserSetupState {
    private SetupStep step;
    private Duration workDuration;
    private Duration shortRestDuration;
    private Duration longRestDuration;
    private int sessionsBeforeLongBreak;
}
