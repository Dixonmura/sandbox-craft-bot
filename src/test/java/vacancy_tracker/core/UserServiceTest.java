package vacancy_tracker.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.data.InMemoryUserRepository;

import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {

    Long USER_ID = 17L;
    UserRepository repository;
    UserService userService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        userService = new UserService(repository);
        userService.getOrCreateUser(USER_ID);

    }

    @Test
    @DisplayName("Проверка получения или создания User, если он не добавлен в репозиторий")
    void getOrCreateUser_shouldReturnUser_whenUserExistOrNotExist() {
        userService.updateUtcOffset(USER_ID, "+03:00");
        User createdUser = userService.getOrCreateUser(USER_ID);
        User notExistUser = userService.getOrCreateUser(98L);

        assertThat(createdUser)
                .isNotNull()
                .extracting(User::getUserId, User::getUtcOffset)
                .containsExactly(USER_ID, ZoneOffset.of("+03:00"));

        assertThat(notExistUser.getUserId())
                .isNotNull()
                .isEqualTo(98L);
    }

    @Test
    @DisplayName("Проверка корректного сдвига часового пояса")
    void updateUtcOffset_shouldAcceptNewZoneOffset_whenDataIsValid() {
        User user = userService.updateUtcOffset(USER_ID, "+07:00");

        assertThat(user)
                .isNotNull()
                .extracting(User::getUserId, User::getUtcOffset)
                .containsExactly(USER_ID, ZoneOffset.of("+07:00"));
    }

    @Test
    @DisplayName("Проверка обновления фильтров поиска вакансий")
    void updateUserSettings_shouldUpdateUserSettings_whenDataIsValid() {
        userService.updateUserSettings(
                USER_ID,
                new UserSettings(null, null, null, null, Instant.now()));

        assertThat(userService.getOrCreateUser(USER_ID).hasSettings())
                .isTrue();
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения при попытке создания экземпляра, когда Repository null")
    void constructor_shouldThrowsIllegalArgumentException_whenRepositoryIsNull() {
        assertThatThrownBy(() -> new UserService(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Repository не может быть null");
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения, userId null")
    void getOrCreateUser_shouldThrowsIllegalArgumentException_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.getOrCreateUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения при попытке обновить ZoneOffset с некорректными данными")
    void updateUtcOffset_shouldThrowsIllegalArgumentException_whenUserIdIsNullOrZoneOffsetInvalid() {
        assertThatThrownBy(() -> userService.updateUtcOffset(null, "+02:00"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");

        assertThatThrownBy(() -> userService.updateUtcOffset(USER_ID, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный формат ввода зоны времени");

        assertThatThrownBy(() -> userService.updateUtcOffset(USER_ID, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ожидается: UTC+3, UTC+3:30, UTC-5");

        assertThatThrownBy(() -> userService.updateUtcOffset(USER_ID, "-5:-25"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный формат ввода зоны времени UTC");
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения, когда UserId или UserSettings null")
    void updateUserSettings_shouldThrowsIllegalArgumentException_whenUserIdOrUserSettingsIsNull() {
        assertThatThrownBy(() -> userService.updateUserSettings(
                null,
                new UserSettings(null, null, null, null, Instant.now())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");

        assertThatThrownBy(() -> userService.updateUserSettings(
                USER_ID,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserSettings не может быть null");
    }
}