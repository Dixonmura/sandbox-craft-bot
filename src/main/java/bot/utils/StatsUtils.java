package bot.utils;

import pomodoro.core.PomodoroStats;

public class StatsUtils {

    public String getStatsMessage(PomodoroStats stats) {
        long allWorkMinutes = stats.getWorkMinutes().toMinutes();
        long allRestMinutes = stats.getRestMinutes().toMinutes();

        long workHours = allWorkMinutes / 60;
        long workMinutes = allWorkMinutes % 60;
        long restHours = allRestMinutes / 60;
        long restMinutes = allRestMinutes % 60;

        return """
                📊 Статистика за последние тридцать дней:
                -----------------------------------------
                📌 Провели %d сессий за работой
                ☕ Провели %d сессий за отдыхом
                -----------------------------------------
                💼 Общее время работы: %d час. %d мин.
                🛋 Общее время отдыха: %d час. %d мин.
                -----------------------------------------
                """.formatted(
                stats.getWorkSessions(),
                stats.getRestSessions(),
                workHours, workMinutes,
                restHours, restMinutes
        );
    }
}
