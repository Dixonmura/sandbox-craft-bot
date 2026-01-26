package bot.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Универсальный ридер CSV из ресурсов. Парсит в List<String[]>, маппинг — снаружи.
 */
public class CsvResourceReader {

    /**
     * Читает CSV и применяет mapper к каждой строке (пропускает заголовок).
     *
     * @param inputStream поток CSV
     * @param delimiter разделитель (',' или ';')
     * @param mapper преобразует String[] в T
     * @return список объектов
     */
    public <T> List<T> read(InputStream inputStream, char delimiter, Function<String[], T> mapper) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream не может быть null");
        }
        if (mapper == null) {
            throw new IllegalArgumentException("Mapper не может быть null");
        }

        List<String[]> rows = parseRows(inputStream, delimiter);
        return rows.stream().map(mapper).toList();
    }

    /**
     * Только парсит CSV в сырые строки, без заголовка.
     */
    private List<String[]> parseRows(InputStream inputStream, char delimiter) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isBlank()) continue;

                String[] fields = line.split(String.valueOf(delimiter));

                if (fields.length < 2) {
                    throw new IllegalArgumentException("Некорректный формат в строке " + lineNumber + ": '" + line + "'");
                }
                rows.add(fields);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения CSV", e);
        }
        return rows;
    }
}
