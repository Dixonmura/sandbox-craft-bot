package bot.utils;

import pomodoro.core.Phase;
import pomodoro.core.PomodoroStats;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Считает статистику Pomodoro за последние 30 дней по CSV-файлу пользователя.
 * Агрегирует длительность работы и отдыха и количество завершённых рабочих сессий и
 * сессий отдыха.
 */
public class CsvStatsReader implements StatsReader {

    private final Path baseDir;
    private final Clock clock;
    private final CsvResourceReader reader;

    public CsvStatsReader(Path baseDir, Clock clock, CsvResourceReader reader) {
        this.baseDir = baseDir;
        this.clock = clock;
        this.reader = reader;
    }

    @Override
    public PomodoroStats readMonthlyStats(Long chatId) {
        PomodoroStats stats = new PomodoroStats();
        stats.setWorkMinutes(Duration.ZERO);
        stats.setRestMinutes(Duration.ZERO);
        stats.setWorkSessions(0);
        stats.setRestSessions(0);

        Path file = baseDir.resolve("stats_" + chatId + ".csv");
        List<RawEvent> rawEvents;

        try (InputStream inputStream = Files.newInputStream(file)) {
            rawEvents = reader.read(inputStream, ',', raw -> new RawEvent(raw[0], raw[1], raw[2]));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения CSV", e);
        }

        Instant now = Instant.now(clock);
        Instant monthAgo = now.minus(Duration.ofDays(30));

        for (RawEvent event : rawEvents) {
            Phase currentPhase = (Phase.valueOf(event.phase()));
            Instant finishedAt = Instant.ofEpochSecond(Long.parseLong(event.finishedAt()));
            Duration duration = Duration.ofMinutes(Long.parseLong(event.durationMinutes()));
            if (finishedAt.isAfter(monthAgo)) {
                switch (currentPhase) {
                    case WORK -> {
                        Duration allDuration = stats.getWorkMinutes();
                        int allSessions = stats.getWorkSessions();

                        stats.setWorkMinutes(allDuration.plus(duration));
                        stats.setWorkSessions(allSessions + 1);
                    }
                    case SHORT_BREAK, LONG_BREAK -> {
                        Duration allDuration = stats.getRestMinutes();
                        int allSessions = stats.getRestSessions();

                        stats.setRestMinutes(allDuration.plus(duration));
                        stats.setRestSessions(allSessions + 1);
                    }
                }
            }
        }

        return stats;
    }
}

record RawEvent(String phase, String durationMinutes, String finishedAt) {
}
