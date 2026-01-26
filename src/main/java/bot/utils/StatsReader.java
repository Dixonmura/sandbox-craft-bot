package bot.utils;

import pomodoro.core.PomodoroStats;

public interface StatsReader {
    PomodoroStats readMonthlyStats(Long chatId);
}
