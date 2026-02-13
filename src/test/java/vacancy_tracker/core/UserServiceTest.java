package vacancy_tracker.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vacancy_tracker.data.InMemoryUserRepository;

import java.time.LocalTime;
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
        userService.updateUtcOffset(USER_ID, ZoneOffset.of("+03:00"));
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
        User user = userService.updateUtcOffset(USER_ID, ZoneOffset.of("+07:00"));

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
                new UserSettings(null, null, null, null, LocalTime.now()));

        assertThat(userService.getOrCreateUser(USER_ID).hasSettings())
                .isTrue();
    }

    @Test
    @DisplayName("Проверка обновления отдельных полей настроек поиска вакансий")
    void updateSomeSettings_shouldUpdateUserSettings_whenDataIsValid() {
        userService.getOrCreateUser(USER_ID);
        userService.updateUserSettings(
                USER_ID,
                new UserSettings(null, null, null, null, LocalTime.now()));
        User region = userService.updateRegionCode(USER_ID, 65);
        User salary = userService.updateSalaryFrom(USER_ID, 90000);
        User experience = userService.updateExperienceFrom(USER_ID, 1);
        User keyWord = userService.updateWordForSearch(USER_ID, "Kotlin");
        User notificationTime = userService.updateNotificationTime(USER_ID, LocalTime.of(11, 55));

        assertThat(region.getSettings().getRegionCode()).isEqualTo(65);
        assertThat(salary.getSettings().getSalaryFrom()).isEqualTo(90000);
        assertThat(experience.getSettings().getExperienceFrom()).isEqualTo(1);
        assertThat(keyWord.getSettings().getWordForSearch()).contains("Kotlin");
        assertThat(notificationTime.getSettings().getNotificationTime()).isEqualTo(LocalTime.of(11, 55));
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения при попытке создания экземпляра, когда Repository null")
    void constructor_shouldThrowsIllegalArgumentException_whenRepositoryIsNull() {
        assertThatThrownBy(() -> new UserService(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Repository не может быть null");
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения при получении или создании User, когда userId null")
    void getOrCreateUser_shouldThrowsIllegalArgumentException_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.getOrCreateUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения при попытке обновить какое-либо поле настроек, когда userId null")
    void updateSomeSetting_shouldThrowsIllegalArgumentException_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.updateUtcOffset(null, ZoneOffset.of("+02:00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
        assertThatThrownBy(() -> userService.updateRegionCode(null, 65))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
        assertThatThrownBy(() -> userService.updateSalaryFrom(null, 90000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
        assertThatThrownBy(() -> userService.updateExperienceFrom(null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
        assertThatThrownBy(() -> userService.updateWordForSearch(null, "Kotlin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
        assertThatThrownBy(() -> userService.updateNotificationTime(null, LocalTime.of(11, 55)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
        assertThatThrownBy(() -> userService.updateSettingState(null, UserSettingState.WAITING_SET_KEYWORD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
        assertThatThrownBy(() -> userService.getSettingState(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
    }

    @Test
    @DisplayName("Проверка выбрасывания исключения, когда UserId или UserSettings null")
    void updateUserSettings_shouldThrowsIllegalArgumentException_whenUserIdOrUserSettingsIsNull() {
        assertThatThrownBy(() -> userService.updateUserSettings(
                null,
                new UserSettings(null, null, null, null, LocalTime.now())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");

        assertThatThrownBy(() -> userService.updateUserSettings(
                USER_ID,
                null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserSettings не может быть null");
    }
}