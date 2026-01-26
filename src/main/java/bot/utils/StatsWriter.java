package bot.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class StatsWriter {
    public void append(Path file, String line) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(
                    file,
                    line + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка записи статистики в " + file, e);
        }
    }
}
