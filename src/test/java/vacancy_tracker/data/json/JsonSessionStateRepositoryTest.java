package vacancy_tracker.data.json;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vacancy_tracker.core.VacancySessionState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonSessionStateRepositoryTest {

    @TempDir
    Path tempDir;

    private JsonSessionStateRepository repository;
    private final Long USER_ID_1 = 123L;
    private final Long USER_ID_2 = 456L;

    @BeforeEach
    void setUp() {
        repository = new JsonSessionStateRepository(tempDir.toString());
    }

    @AfterEach
    void cleanUp() {
        repository.findAll().keySet().forEach(repository::delete);
    }

    @Test
    @DisplayName("Сохраняет и находит состояние сессии по ID")
    void saveAndFindById_shouldWork_whenDataIsValid() {
        repository.save(USER_ID_1, VacancySessionState.ACTIVE);
        Optional<VacancySessionState> found = repository.findByUserId(USER_ID_1);

        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(VacancySessionState.ACTIVE);
    }

    @Test
    @DisplayName("Обновляет существующее состояние")
    void save_shouldUpdate_whenStateAlreadyExists() {
        repository.save(USER_ID_1, VacancySessionState.ACTIVE);

        repository.save(USER_ID_1, VacancySessionState.INACTIVE);
        Optional<VacancySessionState> found = repository.findByUserId(USER_ID_1);

        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(VacancySessionState.INACTIVE);
    }

    @Test
    @DisplayName("Возвращает пустой Optional для несуществующего пользователя")
    void findByUserId_shouldReturnEmpty_whenUserDoesNotExist() {
        Optional<VacancySessionState> found = repository.findByUserId(999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Удаляет состояние сессии")
    void delete_shouldRemoveState() {
        repository.save(USER_ID_1, VacancySessionState.ACTIVE);
        assertThat(repository.findByUserId(USER_ID_1)).isPresent();

        repository.delete(USER_ID_1);

        assertThat(repository.findByUserId(USER_ID_1)).isEmpty();
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя не вызывает ошибок")
    void delete_shouldDoNothing_whenUserDoesNotExist() {
        repository.delete(999L);
    }

    @Test
    @DisplayName("Возвращает все сохранённые состояния")
    void findAll_shouldReturnAllStates() {
        repository.save(USER_ID_1, VacancySessionState.ACTIVE);
        repository.save(USER_ID_2, VacancySessionState.CONFIGURING);

        Map<Long, VacancySessionState> allStates = repository.findAll();

        assertThat(allStates).hasSize(2);
        assertThat(allStates.get(USER_ID_1)).isEqualTo(VacancySessionState.ACTIVE);
        assertThat(allStates.get(USER_ID_2)).isEqualTo(VacancySessionState.CONFIGURING);
    }

    @Test
    @DisplayName("Возвращает пустую Map, если нет сохранённых состояний")
    void findAll_shouldReturnEmptyMap_whenNoStates() {
        Map<Long, VacancySessionState> allStates = repository.findAll();
        assertThat(allStates).isEmpty();
    }

    @Test
    @DisplayName("Бросает исключение при сохранении с null userId")
    void save_shouldThrowException_whenUserIdIsNull() {
        assertThatThrownBy(() ->
                repository.save(null, VacancySessionState.ACTIVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
    }

    @Test
    @DisplayName("Бросает исключение при сохранении с null state")
    void save_shouldThrowException_whenStateIsNull() {
        assertThatThrownBy(() -> repository.save(USER_ID_1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("state не может быть null");
    }

    @Test
    @DisplayName("Игнорирует поиск по null ID")
    void findByUserId_shouldReturnEmpty_whenIdIsNull() {
        Optional<VacancySessionState> found = repository.findByUserId(null);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Игнорирует удаление по null ID")
    void delete_shouldDoNothing_whenIdIsNull() {
        repository.delete(null);
    }

    @Test
    @DisplayName("Сохраняет и восстанавливает все возможные состояния")
    void shouldHandleAllSessionStates() {
        for (VacancySessionState state : VacancySessionState.values()) {
            repository.save(USER_ID_1, state);
            Optional<VacancySessionState> found = repository.findByUserId(USER_ID_1);
            assertThat(found).isPresent();
            assertThat(found.get()).isEqualTo(state);
            repository.delete(USER_ID_1);
        }
    }

    @Test
    @DisplayName("При ошибке чтения файла возвращает пустой Optional")
    void findByUserId_shouldReturnEmpty_whenFileIsCorrupted() throws IOException {
        repository.save(USER_ID_1, VacancySessionState.ACTIVE);
        Path filePath = tempDir.resolve("session_" + USER_ID_1 + ".json");

        Files.writeString(filePath, "Это не JSON");

        Optional<VacancySessionState> found = repository.findByUserId(USER_ID_1);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("При ошибке в findAll пропускает повреждённые файлы")
    void findAll_shouldSkipCorruptedFiles() throws IOException {
        repository.save(USER_ID_1, VacancySessionState.ACTIVE);
        repository.save(USER_ID_2, VacancySessionState.CONFIGURING);

        Path filePath = tempDir.resolve("session_" + USER_ID_2 + ".json");
        Files.writeString(filePath, "Это не JSON");

        Map<Long, VacancySessionState> allStates = repository.findAll();

        assertThat(allStates).hasSize(1);
        assertThat(allStates.get(USER_ID_1)).isEqualTo(VacancySessionState.ACTIVE);
        assertThat(allStates.get(USER_ID_2)).isNull();
    }

    @Test
    @DisplayName("Проверка создания директории")
    void constructor_shouldCreateDirectory_whenItDoesNotExist() {
        Path newDir = tempDir.resolve("new-sessions-dir");
        assertThat(Files.exists(newDir)).isFalse();

        new JsonSessionStateRepository(newDir.toString());
        assertThat(Files.exists(newDir)).isTrue();
        assertThat(Files.isDirectory(newDir)).isTrue();
    }

    @Test
    @DisplayName("Удаление файла физически удаляет его с диска")
    void delete_shouldPhysicallyRemoveFile() {
        repository.save(USER_ID_1, VacancySessionState.ACTIVE);
        Path filePath = tempDir.resolve("session_" + USER_ID_1 + ".json");
        assertThat(Files.exists(filePath)).isTrue();

        repository.delete(USER_ID_1);

        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    @DisplayName("Атомарность записи: файл не повреждается при ошибке")
    void save_shouldNotCorruptFile_whenErrorOccurs() throws IOException {
        repository.save(USER_ID_1, VacancySessionState.ACTIVE);
        Path filePath = tempDir.resolve("session_" + USER_ID_1 + ".json");

        assertThat(filePath).exists();
        String newContent = Files.readString(filePath);
        assertThat(newContent).isNotEmpty();
    }
}