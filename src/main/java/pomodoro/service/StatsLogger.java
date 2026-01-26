package pomodoro.service;

import bot.utils.StatsWriter;
import pomodoro.core.Phase;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

public class StatsLogger {

    private final StatsWriter writer;
    private final Path baseDir;


    public StatsLogger(StatsWriter writer, Path baseDir) {
        this.writer = writer;
        this.baseDir = baseDir;
    }

    public void logPhase(Long chatId, Phase phase, Duration duration, Instant finishedAt) {
        Path file = baseDir.resolve("stats_" + chatId + ".csv");
        String line = phase.name() + "," + duration.toMinutes() + "," + finishedAt.getEpochSecond();
        writer.append(file, line);
    }
}
