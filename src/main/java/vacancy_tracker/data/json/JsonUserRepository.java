package vacancy_tracker.data.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vacancy_tracker.core.User;
import vacancy_tracker.data.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация {@link UserRepository}, сохраняющая пользователей в JSON-файлы.
 *
 * <p>Каждый пользователь сохраняется в отдельный файл с именем user_{id}.json.
 * Для атомарности записи используется временный файл с последующим перемещением.
 *
 * <p>Директория хранения по умолчанию: ~/.vacancy-tracker/users/
 */
public class JsonUserRepository implements UserRepository {

    private static final Logger log = LogManager.getLogger(JsonUserRepository.class);

    private final Path storageDir;
    private final ObjectMapper objectMapper;
    private final UserEntityMapper userEntityMapper;

    public JsonUserRepository() {
        this.storageDir = Path.of(getDefaultStorageDir());
        this.objectMapper = new ObjectMapper();
        this.userEntityMapper = new UserEntityMapper();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        createStorageDirectory();
    }

    /**
     * Конструктор для удобного тестирования
     */
    public JsonUserRepository(String storageDir) {
        this.storageDir = Path.of(storageDir);
        this.objectMapper = new ObjectMapper();
        this.userEntityMapper = new UserEntityMapper();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        createStorageDirectory();
    }

    /**
     * Находит пользователя по ID.
     *
     * @param userId идентификатор пользователя
     * @return Optional с пользователем или пустой Optional
     */
    @Override
    public Optional<User> findById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }

        Path filePath = getUserFilePath(userId);
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }

        try {
            UserEntity entity = objectMapper.readValue(filePath.toFile(), UserEntity.class);
            return Optional.ofNullable(userEntityMapper.toDomain(entity));
        } catch (IOException e) {
            log.error("Ошибка чтения файла пользователя {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Сохраняет пользователя в JSON-файл.
     * Использует атомарную запись через временный файл.
     *
     * @param user сохраняемый пользователь
     * @throws IllegalArgumentException если user или userId null
     * @throws RuntimeException при ошибке записи
     */
    @Override
    public void saveUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User не может быть null");
        }
        if (user.getUserId() == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }

        Path filePath = getUserFilePath(user.getUserId());
        Path tempPath = filePath.resolveSibling(filePath.getFileName() + ".tmp");

        try {
            UserEntity entity = userEntityMapper.toEntity(user);
            objectMapper.writeValue(tempPath.toFile(), entity);
            Files.move(tempPath, filePath, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignored) {
                // игнорируем ошибку удаления временного файла
            }
            log.error("Ошибка сохранения пользователя {}", user.getUserId(), e);
            throw new RuntimeException("Ошибка сохранения пользователя " + user.getUserId(), e);
        }
    }

    /**
     * Удаляет файл пользователя.
     *
     * @param userId идентификатор пользователя
     * @throws RuntimeException при ошибке удаления
     */
    @Override
    public void deleteUser(Long userId) {
        if (userId == null) {
            return;
        }

        try {
            Files.deleteIfExists(getUserFilePath(userId));
        } catch (IOException e) {
            log.error("Ошибка удаления файла пользователя {}", userId, e);
            throw new RuntimeException("Ошибка удаления файла пользователя " + userId, e);
        }
    }

    /**
     * Возвращает список всех сохранённых пользователей.
     *
     * @return список пользователей (может быть пустым)
     */
    @Override
    public List<User> findAll() {
        try {
            return Files.walk(storageDir, 1)
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(path -> {
                        try {
                            UserEntity entity = objectMapper.readValue(path.toFile(), UserEntity.class);
                            return userEntityMapper.toDomain(entity);
                        } catch (IOException e) {
                            log.error("Ошибка чтения файла: {}", path, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Ошибка при обходе директории", e);
            return List.of();
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
            log.error("Не удалось создать директорию для хранения: {}", storageDir);
            throw new RuntimeException("Не удалось создать директорию для хранения: " + storageDir, e);
        }
    }

    /**
     * Возвращает путь к файлу пользователя.
     *
     * @param userId идентификатор пользователя
     * @return путь к файлу
     */
    private Path getUserFilePath(Long userId) {
        return storageDir.resolve("user_" + userId + ".json");
    }

    /**
     * Возвращает путь к директории хранения по умолчанию.
     *
     * @return путь в домашней директории пользователя
     */
    private static String getDefaultStorageDir() {
        return System.getProperty("user.home") +
                File.separator + ".vacancy-tracker" +
                File.separator + "users";
    }
}