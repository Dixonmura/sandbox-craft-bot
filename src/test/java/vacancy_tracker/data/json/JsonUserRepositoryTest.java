package vacancy_tracker.data.json;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vacancy_tracker.core.User;
import vacancy_tracker.core.UserSettings;
import vacancy_tracker.core.UserSettingState;

import java.nio.file.Path;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonUserRepositoryTest {

    @TempDir
    Path tempDir;

    private JsonUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JsonUserRepository(tempDir.toString());
    }

    @AfterEach
    void cleanUp() {
        repository.findAll().forEach(user ->
                repository.deleteUser(user.getUserId())
        );
    }

    private User createTestUser(Long id) {
        User user = new User(id);
        user.updateUtcOffset(ZoneOffset.ofHours(3));
        user.updateSettingState(UserSettingState.CLEAN);

        UserSettings settings = new UserSettings(
                77,
                3,
                100000,
                "Java Developer",
                LocalTime.parse("09:00")
        );
        user.updateSettings(settings);
        return user;
    }

    @Test
    @DisplayName("Сохраняет и находит пользователя по ID")
    void saveAndFindById_shouldWork_whenUserIsValid() {
        User user = createTestUser(123L);

        repository.saveUser(user);
        Optional<User> found = repository.findById(123L);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(123L);
        assertThat(found.get().getUtcOffset()).isEqualTo(ZoneOffset.ofHours(3));
        assertThat(found.get().getSettingState()).isEqualTo(UserSettingState.CLEAN);
        assertThat(found.get().getSettings()).isNotNull();
        assertThat(found.get().getSettings().getRegionCode()).isEqualTo(77);
        assertThat(found.get().getSettings().getExperienceFrom()).isEqualTo(3);
        assertThat(found.get().getSettings().getSalaryFrom()).isEqualTo(100000);
        assertThat(found.get().getSettings().getWordForSearch()).isEqualTo("Java Developer");
        assertThat(found.get().getSettings().getNotificationTime()).isEqualTo(LocalTime.parse("09:00"));
    }

    @Test
    @DisplayName("Возвращает пустой Optional для несуществующего пользователя")
    void findById_shouldReturnEmpty_whenUserDoesNotExist() {
        Optional<User> found = repository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Обновляет существующего пользователя")
    void saveUser_shouldUpdate_whenUserAlreadyExists() {
        User user = createTestUser(123L);
        repository.saveUser(user);

        user.updateRegionCode(78);
        user.updateSalaryFrom(150000);
        repository.saveUser(user);

        Optional<User> found = repository.findById(123L);
        assertThat(found).isPresent();
        assertThat(found.get().getSettings().getRegionCode()).isEqualTo(78);
        assertThat(found.get().getSettings().getSalaryFrom()).isEqualTo(150000);
    }

    @Test
    @DisplayName("Удаляет пользователя")
    void deleteUser_shouldRemoveUser() {
        User user = createTestUser(123L);
        repository.saveUser(user);

        // Проверяем, что пользователь есть
        assertThat(repository.findById(123L)).isPresent();

        repository.deleteUser(123L);

        // Проверяем, что пользователь удалён
        assertThat(repository.findById(123L)).isEmpty();
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя не вызывает ошибок")
    void deleteUser_shouldDoNothing_whenUserDoesNotExist() {
        repository.deleteUser(999L);
    }

    @Test
    @DisplayName("Возвращает всех пользователей")
    void findAll_shouldReturnAllUsers() {
        User user1 = createTestUser(1L);
        User user2 = createTestUser(2L);
        User user3 = createTestUser(3L);

        repository.saveUser(user1);
        repository.saveUser(user2);
        repository.saveUser(user3);

        List<User> allUsers = repository.findAll();
        assertThat(allUsers).hasSize(3);
        assertThat(allUsers.stream().map(User::getUserId))
                .containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    @DisplayName("Возвращает пустой список, если нет пользователей")
    void findAll_shouldReturnEmptyList_whenNoUsers() {
        List<User> allUsers = repository.findAll();
        assertThat(allUsers).isEmpty();
    }

    @Test
    @DisplayName("Сохраняет пользователя без настроек")
    void saveUser_shouldWork_whenUserHasNoSettings() {
        User user = new User(19L);
        user.updateUtcOffset(ZoneOffset.ofHours(3));
        user.updateSettingState(UserSettingState.NOT_INITIALIZED);

        repository.saveUser(user);
        Optional<User> found = repository.findById(19L);

        assertThat(found).isPresent();
        assertThat(found.get().getSettings()).isNotNull();
        assertThat(found.get().getUtcOffset()).isEqualTo(ZoneOffset.ofHours(3));
        assertThat(found.get().getSettingState()).isEqualTo(UserSettingState.NOT_INITIALIZED);
    }

    @Test
    @DisplayName("Сохраняет пользователя с null в некоторых полях настроек")
    void saveUser_shouldWork_whenSettingsHaveNulls() {
        User user = new User(123L);
        UserSettings settings = new UserSettings(
                null,
                null,
                100000,
                null,
                LocalTime.parse("09:00")
        );
        user.updateSettings(settings);

        repository.saveUser(user);
        Optional<User> found = repository.findById(123L);

        assertThat(found).isPresent();
        assertThat(found.get().getSettings().getRegionCode()).isNull();
        assertThat(found.get().getSettings().getExperienceFrom()).isNull();
        assertThat(found.get().getSettings().getSalaryFrom()).isEqualTo(100000);
        assertThat(found.get().getSettings().getWordForSearch()).isNull();
        assertThat(found.get().getSettings().getNotificationTime()).isEqualTo(LocalTime.parse("09:00"));
    }

    @Test
    @DisplayName("Бросает исключение при сохранении null пользователя")
    void saveUser_shouldThrowException_whenUserIsNull() {
        assertThatThrownBy(() -> repository.saveUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User не может быть null");
    }

    @Test
    @DisplayName("Бросает исключение при сохранении пользователя с null userId")
    void saveUser_shouldThrowException_whenUserIdIsNull() {
        assertThatThrownBy(() -> repository.saveUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User не может быть null");
    }

    @Test
    @DisplayName("Игнорирует поиск по null ID")
    void findById_shouldReturnEmpty_whenIdIsNull() {
        Optional<User> found = repository.findById(null);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Игнорирует удаление по null ID")
    void deleteUser_shouldDoNothing_whenIdIsNull() {
        repository.deleteUser(null);
    }
}