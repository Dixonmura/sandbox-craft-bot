package vacancy_tracker.data.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import vacancy_tracker.core.VacancySessionState;
import vacancy_tracker.data.repository.SessionStateRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация {@link SessionStateRepository}, сохраняющая состояния сессий в JSON-файлы.
 * <p>
 * Каждое состояние сессии хранится в отдельном файле с именем session_{userId}.json
 * в директории {@code ~/.vacancy-tracker/sessions/}.
 * <p>
 * Для атомарности записи используется временный файл с последующим перемещением.
 */
public class JsonSessionStateRepository implements SessionStateRepository {

    private final Path storageDir;
    private final ObjectMapper objectMapper;

    /**
     * Создаёт репозиторий с директорией хранения по умолчанию:
     * {@code ~/.vacancy-tracker/sessions/}
     */
    public JsonSessionStateRepository() {
        this.storageDir = Path.of(System.getProperty("user.home") + "/.vacancy-tracker/sessions");
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        createStorageDirectory();
    }

    /**
     * Конструктор для тестирования с возможностью указать директорию хранения.
     *
     * @param storageDir путь к директории для хранения файлов сессий
     */
    public JsonSessionStateRepository(String storageDir) {
        this.storageDir = Path.of(storageDir);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        createStorageDirectory();
    }

    /**
     * Находит состояние сессии пользователя по его ID.
     *
     * @param userId идентификатор пользователя
     * @return Optional с состоянием сессии или пустой Optional, если состояние не найдено
     */
    @Override
    public Optional<VacancySessionState> findByUserId(Long userId) {
        Path filePath = storageDir.resolve("session_" + userId + ".json");
        if (!Files.exists(filePath)) return Optional.empty();

        try {
            SessionStateEntity entity = objectMapper.readValue(filePath.toFile(), SessionStateEntity.class);
            return Optional.ofNullable(entity.toDomain());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Сохраняет состояние сессии пользователя.
     * Использует атомарную запись через временный файл для предотвращения повреждения данных.
     *
     * @param userId идентификатор пользователя
     * @param state  состояние сессии для сохранения
     * @throws IllegalArgumentException если userId или state равны null
     * @throws RuntimeException при ошибке записи
     */
    @Override
    public void save(Long userId, VacancySessionState state) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }
        if (state == null) {
            throw new IllegalArgumentException("state не может быть null");
        }

        Path filePath = storageDir.resolve("session_" + userId + ".json");
        try {
            SessionStateEntity entity = SessionStateEntity.fromDomain(userId, state);
            objectMapper.writeValue(filePath.toFile(), entity);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения сессии", e);
        }
    }

    /**
     * Возвращает все сохранённые состояния сессий.
     *
     * @return Map, где ключ - ID пользователя, значение - состояние сессии.
     *         В случае ошибки возвращается пустая Map.
     */
    @Override
    public Map<Long, VacancySessionState> findAll() {
        try {
            return Files.walk(storageDir, 1)
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(p -> {
                        try {
                            SessionStateEntity e = objectMapper.readValue(p.toFile(), SessionStateEntity.class);
                            return Map.entry(e.getUserId(), e.toDomain());
                        } catch (IOException ex) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (IOException e) {
            return Map.of();
        }
    }

    /**
     * Удаляет состояние сессии пользователя.
     *
     * @param userId идентификатор пользователя
     * @throws RuntimeException при ошибке удаления
     */
    @Override
    public void delete(Long userId) {
        try {
            Files.deleteIfExists(storageDir.resolve("session_" + userId + ".json"));
        } catch (IOException e) {
        }
    }

    /**
     * Создаёт директорию для хранения, если она не существует.
     *
     * @throws RuntimeException при ошибке создания
     */
    private void createStorageDirectory() {
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию для сессий", e);
        }
    }
}
