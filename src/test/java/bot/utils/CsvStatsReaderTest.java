package bot.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pomodoro.core.PomodoroStats;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CsvStatsReader")
class CsvStatsReaderTest {

    private static final Long CHAT_ID = 77L;
    private static final Path BASE_DIR = Path.of("/dev/temp");
    private static final long nowEpoch = 1700000000L;
    private static final Instant NOW = Instant.ofEpochSecond(nowEpoch);

    @Mock
    private Clock clock;
    @Mock
    private CsvResourceReader reader;
    private CsvStatsReader statsReader;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(BASE_DIR);
        Path statsFile = BASE_DIR.resolve("stats_" + CHAT_ID + ".csv");
        if (Files.notExists(statsFile)) {
            Files.createFile(statsFile);
        }
        statsReader = new CsvStatsReader(BASE_DIR, clock, reader);
    }

    @Test
    @DisplayName("агрегирует статистику из RawEvent")
    void aggregatesStats_fromRawEvents() throws IOException {
        when(clock.instant()).thenReturn(NOW);
        when(reader.read(any(InputStream.class), eq(','), any()))
                .thenReturn(List.of(
                        new RawEvent("WORK", "15", String.valueOf(nowEpoch - 60)),
                        new RawEvent("SHORT_BREAK", "5", String.valueOf(nowEpoch - 60)),
                        new RawEvent("WORK", "25", String.valueOf(nowEpoch - 60))
                ));

        PomodoroStats stats = statsReader.readMonthlyStats(CHAT_ID);

        assertThat(stats.getWorkMinutes()).isEqualTo(Duration.ofMinutes(40));
        assertThat(stats.getWorkSessions()).isEqualTo(2);
        assertThat(stats.getRestMinutes()).isEqualTo(Duration.ofMinutes(5));
        assertThat(stats.getRestSessions()).isEqualTo(1);
    }

    @Test
    @DisplayName("Возвращает пустую статистику при отсутствии файла")
    void returnsEmptyStats_whenFileNotFound() {
        CsvStatsReader readerNoFile =
                new CsvStatsReader(Path.of("/definitely/no/such/dir"), clock, reader);

        PomodoroStats stats = readerNoFile.readMonthlyStats(CHAT_ID);

        assertThat(stats.getWorkSessions()).isZero();
        assertThat(stats.getWorkMinutes()).isZero();
        assertThat(stats.getRestSessions()).isZero();
    }
}
