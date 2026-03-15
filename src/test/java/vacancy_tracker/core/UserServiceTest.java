package vacancy_tracker.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vacancy_tracker.data.repository.SessionStateRepository;
import vacancy_tracker.data.repository.UserRepository;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;
    @Mock
    private SessionStateRepository stateRepository;
    private UserService userService;
    private final Long USER_ID = 123L;

    @BeforeEach
    void setUp() {
        userService = new UserService(repository, stateRepository);
    }

    private User createTestUser() {
        User user = new User(USER_ID);
        user.updateUtcOffset(ZoneOffset.ofHours(3));
        user.updateSettingState(UserSettingState.CLEAN);
        return user;
    }

    @Test
    @DisplayName("getOrCreateUser возвращает существующего пользователя")
    void getOrCreateUser_shouldReturnExistingUser_whenUserExists() {
        User existingUser = createTestUser();
        when(repository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

        User result = userService.getOrCreateUser(USER_ID);

        assertThat(result).isSameAs(existingUser);
        verify(repository, never()).saveUser(any());
    }

    @Test
    @DisplayName("getOrCreateUser создаёт нового пользователя с UTC+3 по умолчанию")
    void getOrCreateUser_shouldCreateNewUserWithDefaultUtc_whenUserNotFound() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());

        User result = userService.getOrCreateUser(USER_ID);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getUtcOffset()).isEqualTo(ZoneOffset.ofHours(3));
        assertThat(result.getSettingState()).isEqualTo(UserSettingState.NOT_INITIALIZED);
    }

    @Test
    @DisplayName("getOrCreateUser бросает исключение при null userId")
    void getOrCreateUser_shouldThrowException_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.getOrCreateUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
    }

    @Test
    @DisplayName("updateUtcOffset обновляет часовой пояс")
    void updateUtcOffset_shouldUpdateOffset() {
        User user = createTestUser();
        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));
        ZoneOffset newOffset = ZoneOffset.ofHours(5);

        User result = userService.updateUtcOffset(USER_ID, newOffset);

        assertThat(result.getUtcOffset()).isEqualTo(newOffset);
        verify(repository).saveUser(user);
    }

    @Test
    @DisplayName("updateUtcOffset создаёт пользователя при отсутствии")
    void updateUtcOffset_shouldCreateUser_whenNotFound() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        ZoneOffset newOffset = ZoneOffset.ofHours(5);

        User result = userService.updateUtcOffset(USER_ID, newOffset);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getUtcOffset()).isEqualTo(newOffset);
        verify(repository).saveUser(any(User.class));
    }

    @Test
    @DisplayName("updateUtcOffset бросает исключение при null параметрах userId")
    void updateUtcOffset_shouldThrowException_whenParamsAreNull() {
        assertThatThrownBy(() -> userService.updateUtcOffset(null, ZoneOffset.ofHours(3)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
    }

    @Test
    @DisplayName("updateUserSettings обновляет настройки пользователя")
    void updateUserSettings_shouldUpdateSettings() {
        User user = createTestUser();
        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));
        UserSettings newSettings = new UserSettings(77, 3, 100000, "Java", LocalTime.parse("09:00"));

        User result = userService.updateUserSettings(USER_ID, newSettings);

        assertThat(result.getSettings()).isEqualTo(newSettings);
        assertThat(result.getSettingState()).isEqualTo(UserSettingState.READY);
        verify(repository).saveUser(user);
    }

    @Test
    @DisplayName("updateUserSettings создаёт пользователя при отсутствии")
    void updateUserSettings_shouldCreateUser_whenNotFound() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        UserSettings newSettings = new UserSettings(77, 3, 100000, "Java", LocalTime.parse("09:00"));

        User result = userService.updateUserSettings(USER_ID, newSettings);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getSettings()).isEqualTo(newSettings);
        verify(repository).saveUser(any(User.class));
    }

    @Test
    @DisplayName("updateUserSettings бросает исключение при null параметрах")
    void updateUserSettings_shouldThrowException_whenParamsAreNull() {
        assertThatThrownBy(() -> userService.updateUserSettings(null, mock(UserSettings.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");

        assertThatThrownBy(() -> userService.updateUserSettings(USER_ID, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserSettings не может быть null");
    }

    @Test
    @DisplayName("updateRegionCode обновляет код региона")
    void updateRegionCode_shouldUpdateRegion() {
        User user = createTestUser();
        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));

        User result = userService.updateRegionCode(USER_ID, 77);

        assertThat(result.getSettings().getRegionCode()).isEqualTo(77);
        verify(repository).saveUser(user);
    }

    @Test
    @DisplayName("updateRegionCode создаёт пользователя с настройками при отсутствии")
    void updateRegionCode_shouldCreateUserWithSettings_whenNotFound() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());

        User result = userService.updateRegionCode(USER_ID, 77);

        assertThat(result.getSettings()).isNotNull();
        assertThat(result.getSettings().getRegionCode()).isEqualTo(77);
        verify(repository).saveUser(any(User.class));
    }

    @Test
    @DisplayName("updateExperienceFrom обновляет минимальный опыт")
    void updateExperienceFrom_shouldUpdateExperience() {
        User user = createTestUser();
        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));

        User result = userService.updateExperienceFrom(USER_ID, 5);

        assertThat(result.getSettings().getExperienceFrom()).isEqualTo(5);
        verify(repository).saveUser(user);
    }

    @Test
    @DisplayName("updateSalaryFrom обновляет минимальную зарплату")
    void updateSalaryFrom_shouldUpdateSalary() {
        User user = createTestUser();
        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));

        User result = userService.updateSalaryFrom(USER_ID, 150000);

        assertThat(result.getSettings().getSalaryFrom()).isEqualTo(150000);
        verify(repository).saveUser(user);
    }

    @Test
    @DisplayName("updateWordForSearch обновляет ключевое слово")
    void updateWordForSearch_shouldUpdateKeyword() {
        User user = createTestUser();
        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));

        User result = userService.updateWordForSearch(USER_ID, "Java Developer");

        assertThat(result.getSettings().getWordForSearch()).isEqualTo("Java Developer");
        verify(repository).saveUser(user);
    }

    @Test
    @DisplayName("updateNotificationTime обновляет время уведомлений")
    void updateNotificationTime_shouldUpdateNotificationTime() {
        User user = createTestUser();
        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));
        LocalTime newTime = LocalTime.parse("10:30");

        User result = userService.updateNotificationTime(USER_ID, newTime);

        assertThat(result.getSettings().getNotificationTime()).isEqualTo(newTime);
        verify(repository).saveUser(user);
    }

    @Test
    @DisplayName("updateLastRequestTime обновляет время последнего запроса")
    void updateLastRequestTime_shouldUpdateLastRequestTime() {
        User user = createTestUser();
        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));
        Instant now = Instant.now();

        User result = userService.updateLastRequestTime(USER_ID, now);

        assertThat(result.getSettings().getLastRequestTime()).isEqualTo(now);
        verify(repository).saveUser(user);
    }

    @Test
    @DisplayName("updateLastRequestTime бросает исключение при null параметрах")
    void updateLastRequestTime_shouldThrowException_whenParamsAreNull() {
        assertThatThrownBy(() -> userService.updateLastRequestTime(null, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");

        assertThatThrownBy(() -> userService.updateLastRequestTime(USER_ID, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("lastRequestTime не может быть null");
    }

    @Test
    @DisplayName("updateSettingState обновляет состояние настройки")
    void updateSettingState_shouldUpdateSettingState() {
        User user = createTestUser();
        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));

        User result = userService.updateSettingState(USER_ID, UserSettingState.WAITING_SET_UTC);

        assertThat(result.getSettingState()).isEqualTo(UserSettingState.WAITING_SET_UTC);
        verify(repository).saveUser(user);
    }

    @Test
    @DisplayName("updateSettingState бросает исключение при null параметрах")
    void updateSettingState_shouldThrowException_whenParamsAreNull() {
        assertThatThrownBy(() -> userService.updateSettingState(null, UserSettingState.CLEAN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");

        assertThatThrownBy(() -> userService.updateSettingState(USER_ID, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("newState не может быть null");
    }

    @Test
    @DisplayName("setStateSession устанавливает состояние сессии")
    void setStateSession_shouldSetSessionState() {
        userService.setStateSession(USER_ID, VacancySessionState.ACTIVE);

        assertThat(userService.getStateSessionOrDefault(USER_ID))
                .isEqualTo(VacancySessionState.ACTIVE);
    }

    @Test
    @DisplayName("setStateSession бросает исключение при null параметрах")
    void setStateSession_shouldThrowException_whenParamsAreNull() {
        assertThatThrownBy(() -> userService.setStateSession(null, VacancySessionState.ACTIVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");

        assertThatThrownBy(() -> userService.setStateSession(USER_ID, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sessionState не может быть null");
    }

    @Test
    @DisplayName("deleteUser удаляет пользователя")
    void deleteUser_shouldDeleteUser() {
        userService.deleteUser(USER_ID);

        verify(repository).deleteUser(USER_ID);
    }

    @Test
    @DisplayName("deleteUser бросает исключение при null userId")
    void deleteUser_shouldThrowException_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.deleteUser(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
    }

    @Test
    @DisplayName("getSettingState возвращает состояние настройки")
    void getSettingState_shouldReturnSettingState() {
        User user = createTestUser();
        user.updateSettingState(UserSettingState.WAITING_SET_REGION);
        when(repository.findById(USER_ID)).thenReturn(Optional.of(user));

        UserSettingState state = userService.getSettingState(USER_ID);

        assertThat(state).isEqualTo(UserSettingState.WAITING_SET_REGION);
    }

    @Test
    @DisplayName("getSettingState возвращает NOT_INITIALIZED для нового пользователя")
    void getSettingState_shouldReturnNotInitialized_forNewUser() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());

        UserSettingState state = userService.getSettingState(USER_ID);

        assertThat(state).isEqualTo(UserSettingState.NOT_INITIALIZED);
    }

    @Test
    @DisplayName("getSettingState бросает исключение при null userId")
    void getSettingState_shouldThrowException_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.getSettingState(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
    }

    @Test
    @DisplayName("getStateSessionOrDefault возвращает INACTIVE по умолчанию")
    void getStateSessionOrDefault_shouldReturnInactive_whenNoState() {
        VacancySessionState state = userService.getStateSessionOrDefault(USER_ID);

        assertThat(state).isEqualTo(VacancySessionState.INACTIVE);
    }

    @Test
    @DisplayName("getStateSessionOrDefault возвращает установленное состояние")
    void getStateSessionOrDefault_shouldReturnSetState() {
        userService.setStateSession(USER_ID, VacancySessionState.CONFIGURING);

        VacancySessionState state = userService.getStateSessionOrDefault(USER_ID);

        assertThat(state).isEqualTo(VacancySessionState.CONFIGURING);
    }

    @Test
    @DisplayName("getStateSessionOrDefault бросает исключение при null userId")
    void getStateSessionOrDefault_shouldThrowException_whenUserIdIsNull() {
        assertThatThrownBy(() -> userService.getStateSessionOrDefault(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId не может быть null");
    }
}